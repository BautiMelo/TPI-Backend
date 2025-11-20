#!/usr/bin/env python3
"""
Simulación en memoria del flujo: crear Solicitud -> generar Ruta/Tramos -> asignar Camión -> iniciar y finalizar tramos -> cálculo de costos.

Este script no requiere base de datos ni levantar servicios: modela los objetos Java como dataclasses y reproduce la lógica de cálculo usada por los servicios.

Ejecución (PowerShell):
python .\scripts\simulate_lifecycle.py

El script imprime el estado paso a paso y hace comprobaciones para asegurar que los campos críticos quedan asignados.
"""
from dataclasses import dataclass, field, asdict
from typing import List, Optional
from decimal import Decimal
from datetime import datetime, timedelta
import json
import os
import urllib.request
import urllib.parse
import ssl

# --- Modelos (similares a las entidades JPA) ---

@dataclass
class Contenedor:
    id: int
    peso: Optional[Decimal]
    volumen: Optional[Decimal]

@dataclass
class Solicitud:
    id: int
    contenedor: Contenedor
    cliente_id: Optional[int]
    origen_lat: Optional[Decimal] = None
    origen_long: Optional[Decimal] = None
    destino_lat: Optional[Decimal] = None
    destino_long: Optional[Decimal] = None
    direccion_origen: Optional[str] = None
    direccion_destino: Optional[str] = None
    costo_estimado: Optional[Decimal] = None
    costo_final: Optional[Decimal] = None
    tiempo_estimado: Optional[Decimal] = None
    tiempo_real: Optional[Decimal] = None
    ruta_id: Optional[int] = None
    tarifa_id: Optional[int] = None

@dataclass
class Camion:
    id: int
    dominio: str
    capacidad_peso_max: Optional[Decimal]
    capacidad_volumen_max: Optional[Decimal]
    costo_por_km: Optional[Decimal]
    consumo_combustible_promedio: Optional[Decimal]  # litros por km
    disponible: bool = True
    activo: bool = True

@dataclass
class Tramo:
    id: int
    orden: int
    origen_deposito_id: int
    destino_deposito_id: int
    origen_lat: Optional[Decimal]
    origen_long: Optional[Decimal]
    destino_lat: Optional[Decimal]
    destino_long: Optional[Decimal]
    distancia: Optional[float] = None
    duracion_horas: Optional[float] = None
    camion_dominio: Optional[str] = None
    costo_aproximado: Optional[Decimal] = None
    costo_real: Optional[Decimal] = None
    fecha_hora_inicio_real: Optional[datetime] = None
    fecha_hora_fin_real: Optional[datetime] = None

@dataclass
class RutaOpcion:
    id: int
    ruta_id: int
    opcion_index: int
    distancia_total: float
    duracion_total_horas: float
    tramos_json: str

@dataclass
class Ruta:
    id: int
    id_solicitud: int
    fecha_creacion: datetime
    opcion_seleccionada_id: Optional[int] = None
    tramos: List[Tramo] = field(default_factory=list)

@dataclass
class Tarifa:
    id: int
    costo_base_gestion_fijo: Decimal
    valor_litro_combustible: Decimal

# --- Valores de ejemplo (simulan respuestas de ms-gestion-calculos) ---
TARIFA_EJEMPLO = Tarifa(id=1, costo_base_gestion_fijo=Decimal('50.00'), valor_litro_combustible=Decimal('1.20'))

# --- Helper de cálculo (replicar la lógica del servicio) ---

def calcular_costo_real_tramo(tramo: Tramo, camion: Optional[Camion], tarifa: Tarifa, depositos_info: dict, tramos_ruta: List[Tramo]) -> Decimal:
    distancia = tramo.distancia or 0.0
    costo_km_camion = Decimal('0.0')
    costo_combustible = Decimal('0.0')
    costo_estadia = Decimal('0.0')

    if camion and camion.costo_por_km is not None:
        costo_km_camion = Decimal(str(camion.costo_por_km)) * Decimal(str(distancia))

    if camion and camion.consumo_combustible_promedio is not None:
        consumo_l = Decimal(str(camion.consumo_combustible_promedio)) * Decimal(str(distancia))
        costo_combustible = consumo_l * tarifa.valor_litro_combustible

    # Estadía: si destino tiene costoEstadiaDiario y existe siguiente tramo, calcular noches
    if tramo.destino_deposito_id and str(tramo.destino_deposito_id) in depositos_info:
        deposito = depositos_info[str(tramo.destino_deposito_id)]
        costo_estadia_diario = Decimal(str(deposito.get('costoEstadiaDiario', 0)))
        # buscar fecha inicio siguiente tramo
        inicio_siguiente = None
        tramos_sorted = sorted(tramos_ruta, key=lambda t: t.orden)
        for i, t in enumerate(tramos_sorted):
            if t.id == tramo.id and i+1 < len(tramos_sorted):
                siguiente = tramos_sorted[i+1]
                inicio_siguiente = siguiente.fecha_hora_inicio_real or None
                break
        fin_actual = tramo.fecha_hora_fin_real
        if fin_actual and inicio_siguiente:
            noches = max((inicio_siguiente.date() - fin_actual.date()).days, 0)
            costo_estadia = costo_estadia_diario * Decimal(noches)

    total = (costo_km_camion + costo_combustible + costo_estadia).quantize(Decimal('0.01'))
    return total

# --- Simulación del flujo ---

def simulate():
    print('\n--- Simulación: creación de solicitud y ciclo completo de tramos ---\n')

    # Preparar carpeta de logs
    logs_dir = os.path.join(os.path.dirname(__file__), '..', 'logs')
    os.makedirs(logs_dir, exist_ok=True)
    snapshots_file = os.path.join(logs_dir, 'simulate_snapshots.json')
    snapshots = []

    def take_snapshot(step: str, solicitud_obj: Optional[Solicitud] = None, ruta_obj: Optional[Ruta] = None, ruta_opciones_obj: Optional[List[RutaOpcion]] = None, camion_obj: Optional[Camion] = None):
        snap = {
            'step': step,
            'timestamp': datetime.now().isoformat(),
            'solicitud': asdict(solicitud_obj) if solicitud_obj is not None else None,
            'ruta': {'id': ruta_obj.id, 'tramos': [asdict(t) for t in ruta_obj.tramos]} if ruta_obj is not None else None,
            'ruta_opciones': [asdict(o) for o in ruta_opciones_obj] if ruta_opciones_obj is not None else None,
            'camion': asdict(camion_obj) if camion_obj is not None else None
        }
        snapshots.append(snap)
        # Guardar inmediatamente para inspección externa
        try:
            with open(snapshots_file, 'w', encoding='utf-8') as fh:
                json.dump(snapshots, fh, default=str, indent=2)
        except Exception as e:
            print('Warning: no se pudo escribir snapshot:', e)

    # 1) Crear solicitud SIN contenedor (contenedor se crea/adjunta después)
    solicitud = Solicitud(id=1, contenedor=None, cliente_id=10)
    # Añadir datos de ejemplo que a veces no se guardan en otros flujos
    # Según la nueva política, NO enviar IDs de depósito en las solicitudes,
    # sólo direcciones de texto o coordenadas "lat,lon". Simularemos ambos casos.
    solicitud.direccion_origen = 'Av. Ejemplo 123, CABA'  # dirección textual válida
    # Usar dirección real conocida para pruebas de geocodificación externa
    solicitud.direccion_destino = 'Av. Santa Fe 567, CABA'  # dirección textual válida resolvible
    # ejemplo alternativo inválido (deposito id) para demostrar rechazo en el flujo
    solicitud_invalida = Solicitud(id=2, contenedor=None, cliente_id=11)
    solicitud_invalida.direccion_origen = '101'  # ID de depósito (no permitido)
    solicitud_invalida.direccion_destino = '103'  # ID de depósito (no permitido)

    def is_deposito_id(s: Optional[str]) -> bool:
        if s is None: return False
        s = s.strip()
        if s == '': return False
        try:
            int(s)
            return True
        except Exception:
            return False

    def simulate_geocode(direccion: str):
        if direccion is None:
            return None, None
        # Si la dirección viene en formato "lat,lon" la parseamos y retornamos
        try:
            if ',' in direccion:
                parts = [p.strip() for p in direccion.split(',')]
                if len(parts) == 2:
                    # intentar parseo numérico directo
                    try:
                        lat = Decimal(parts[0])
                        lon = Decimal(parts[1])
                        return lat, lon
                    except Exception:
                        # no son coordenadas numéricas: seguir a geocodificador externo
                        pass
        except Exception:
            pass

        # Si no son coordenadas explícitas, llamar al endpoint de geocodificación
        # Usamos gateway en localhost:8080 que enruta a ms-gestion-calculos
        def get_kc_token():
            # Valores por defecto, se pueden sobreescribir con variables de entorno
            kc_url = os.environ.get('KEYCLOAK_URL', 'http://localhost:8089')
            realm = os.environ.get('KEYCLOAK_REALM', 'tpi-backend')
            client_id = os.environ.get('KC_CLIENT_ID', 'postman-test')
            client_secret = os.environ.get('KC_CLIENT_SECRET', 'secret-postman-123')
            username = os.environ.get('KC_USERNAME', 'cliente1')
            password = os.environ.get('KC_PASSWORD', '1234')
            token_url = f"{kc_url}/realms/{realm}/protocol/openid-connect/token"
            data = {
                'grant_type': 'password',
                'client_id': client_id,
                'client_secret': client_secret,
                'username': username,
                'password': password
            }
            try:
                encoded = urllib.parse.urlencode(data).encode('utf-8')
                req = urllib.request.Request(token_url, data=encoded, method='POST')
                req.add_header('Content-Type', 'application/x-www-form-urlencoded')
                # allow self-signed TLS if needed (local dev)
                ctx = ssl.create_default_context()
                ctx.check_hostname = False
                ctx.verify_mode = ssl.CERT_NONE
                with urllib.request.urlopen(req, context=ctx, timeout=10) as resp:
                    body = resp.read().decode('utf-8')
                    obj = json.loads(body)
                    return obj.get('access_token')
            except Exception as e:
                print('Warning: no se pudo obtener token Keycloak:', e)
                return None

        token = get_kc_token()
        if not token:
            # no token => no llamada autorizada; devolver (None,None)
            return None, None

        try:
            q = urllib.parse.quote(direccion, safe='')
            url = f"http://localhost:8080/api/v1/gestion/geocode?direccion={q}"
            req = urllib.request.Request(url, method='GET')
            req.add_header('Authorization', f'Bearer {token}')
            req.add_header('Accept', 'application/json')
            ctx = ssl.create_default_context()
            ctx.check_hostname = False
            ctx.verify_mode = ssl.CERT_NONE
            with urllib.request.urlopen(req, context=ctx, timeout=10) as resp:
                if resp.status == 200:
                    body = resp.read().decode('utf-8')
                    obj = json.loads(body)
                    lat = obj.get('latitud') or obj.get('latitude') or obj.get('lat')
                    lon = obj.get('longitud') or obj.get('longitude') or obj.get('lon')
                    if lat is not None and lon is not None:
                        try:
                            return Decimal(str(lat)), Decimal(str(lon))
                        except Exception:
                            return None, None
        except Exception as e:
            print('Warning: error al llamar geocodificador externo:', e)

        return None, None
    solicitud.costo_estimado = Decimal('2000.00')
    solicitud.tiempo_estimado = Decimal('5.0')
    solicitud.tarifa_id = TARIFA_EJEMPLO.id
    # Simular validación: rechazar solicitudes que contengan IDs de depósito
    print('\nValidando solicitud inválida (contiene IDs de depósito):')
    if is_deposito_id(solicitud_invalida.direccion_origen) or is_deposito_id(solicitud_invalida.direccion_destino):
        print('  ERROR: La solicitud contiene IDs de depósito en las direcciones. No se permiten. Ejemplo:', solicitud_invalida.direccion_origen, solicitud_invalida.direccion_destino)
    else:
        print('  OK: solicitud válida')

    # Intentar geocodificar las direcciones textuales (simulación local)
    lat_o, lon_o = simulate_geocode(solicitud.direccion_origen)
    lat_d, lon_d = simulate_geocode(solicitud.direccion_destino)
    if lat_o and lon_o:
        solicitud.origen_lat = lat_o
        solicitud.origen_long = lon_o
    if lat_d and lon_d:
        solicitud.destino_lat = lat_d
        solicitud.destino_long = lon_d

    print('\nSolicitud creada (con datos de ejemplo y geocodificación simulada):', json.dumps(asdict(solicitud), default=str, indent=2))
    take_snapshot('solicitud_creada', solicitud_obj=solicitud)
    print('Solicitud creada (con datos de ejemplo):', json.dumps(asdict(solicitud), default=str, indent=2))
    take_snapshot('solicitud_creada', solicitud_obj=solicitud)

    # Crear contenedor posteriormente y asignarlo a la solicitud
    cont = Contenedor(id=1, peso=Decimal('1000.0'), volumen=Decimal('10.5'))
    solicitud.contenedor = cont
    print('\nContenedor creado y asignado a la solicitud:', json.dumps(asdict(cont), default=str, indent=2))
    take_snapshot('contenedor_asignado', solicitud_obj=solicitud)

    # 2) Generar opciones tentativas de ruta (persistir variantes) con costos aproximados
    ruta = Ruta(id=1, id_solicitud=solicitud.id, fecha_creacion=datetime.now())
    solicitud.ruta_id = ruta.id

    # Opción A (más corta)
    optA_tramos = [
        Tramo(id=1, orden=1, origen_deposito_id=101, destino_deposito_id=102,
              origen_lat=Decimal('-34.6'), origen_long=Decimal('-58.4'),
              destino_lat=Decimal('-34.7'), destino_long=Decimal('-58.5'),
              distancia=100.0, duracion_horas=1.8),
        Tramo(id=2, orden=2, origen_deposito_id=102, destino_deposito_id=103,
              origen_lat=Decimal('-34.7'), origen_long=Decimal('-58.5'),
              destino_lat=Decimal('-35.0'), destino_long=Decimal('-58.7'),
              distancia=180.0, duracion_horas=3.0)
    ]

    # Opción B (más rápida pero más larga)
    optB_tramos = [
        Tramo(id=3, orden=1, origen_deposito_id=101, destino_deposito_id=104,
              origen_lat=Decimal('-34.6'), origen_long=Decimal('-58.4'),
              destino_lat=Decimal('-34.8'), destino_long=Decimal('-58.45'),
              distancia=140.0, duracion_horas=1.6),
        Tramo(id=4, orden=2, origen_deposito_id=104, destino_deposito_id=103,
              origen_lat=Decimal('-34.8'), origen_long=Decimal('-58.45'),
              destino_lat=Decimal('-35.0'), destino_long=Decimal('-58.7'),
              distancia=220.0, duracion_horas=2.7)
    ]

    # Calcular costo aproximado por tramo (regla tentativa: costo_base_por_km = 8.0)
    COSTO_BASE_POR_KM = Decimal('8.00')
    def calcular_costo_aproximado(tramo: Tramo) -> Decimal:
        return (Decimal(str(tramo.distancia)) * COSTO_BASE_POR_KM).quantize(Decimal('0.01'))

    for t in optA_tramos:
        t.costo_aproximado = calcular_costo_aproximado(t)
    for t in optB_tramos:
        t.costo_aproximado = calcular_costo_aproximado(t)

    ruta_opciones = [
        RutaOpcion(id=1, ruta_id=ruta.id, opcion_index=0, distancia_total=sum(t.distancia for t in optA_tramos), duracion_total_horas=sum(t.duracion_horas for t in optA_tramos), tramos_json=json.dumps([asdict(t) for t in optA_tramos], default=str)),
        RutaOpcion(id=2, ruta_id=ruta.id, opcion_index=1, distancia_total=sum(t.distancia for t in optB_tramos), duracion_total_horas=sum(t.duracion_horas for t in optB_tramos), tramos_json=json.dumps([asdict(t) for t in optB_tramos], default=str)),
    ]

    print('\nOpciones tentativas de ruta generadas (con costos aproximados por tramo):')
    for op in ruta_opciones:
        print(f"- Opción {op.opcion_index+1}: distancia={op.distancia_total} km, duracion={op.duracion_total_horas} h")
        tramos_list = json.loads(op.tramos_json)
        for tr in tramos_list:
            print(f"   Tramo orden={tr['orden']}, distancia={tr['distancia']}, costo_aproximado={tr.get('costo_aproximado')}")
    take_snapshot('opciones_generadas', solicitud_obj=solicitud, ruta_opciones_obj=ruta_opciones)

    # 3) Simular que el usuario elige una opción (por ejemplo, la opción 2)
    opcion_elegida = ruta_opciones[1]
    ruta.opcion_seleccionada_id = opcion_elegida.id
    # Deserializar tramos seleccionados y convertir a objetos Tramo reales para la ruta
    selected_tramos_raw = json.loads(opcion_elegida.tramos_json)
    ruta.tramos = []
    next_tramo_id = 100
    for raw in selected_tramos_raw:
        t = Tramo(id=raw.get('id', next_tramo_id), orden=raw['orden'], origen_deposito_id=raw['origen_deposito_id'], destino_deposito_id=raw['destino_deposito_id'], origen_lat=Decimal(str(raw.get('origen_lat'))) if raw.get('origen_lat') is not None else None, origen_long=Decimal(str(raw.get('origen_long'))) if raw.get('origen_long') is not None else None, destino_lat=Decimal(str(raw.get('destino_lat'))) if raw.get('destino_lat') is not None else None, destino_long=Decimal(str(raw.get('destino_long'))) if raw.get('destino_long') is not None else None, distancia=raw.get('distancia'), duracion_horas=raw.get('duracion_horas'))
        t.costo_aproximado = calcular_costo_aproximado(t)
        ruta.tramos.append(t)
        next_tramo_id += 1

    print(f"\nUsuario selecciona la opción {opcion_elegida.opcion_index+1} (id={opcion_elegida.id}). Tramos asignados a la ruta:")
    for t in ruta.tramos:
        print(asdict(t))
    take_snapshot('opcion_seleccionada', solicitud_obj=solicitud, ruta_obj=ruta)

    # 3) Crear camion disponible
    camion = Camion(id=1, dominio='ABC123', capacidad_peso_max=Decimal('20000'), capacidad_volumen_max=Decimal('50'),
                    costo_por_km=Decimal('10.00'), consumo_combustible_promedio=Decimal('0.2'))
    print('\nCamión disponible:', asdict(camion))
    take_snapshot('camion_creado', solicitud_obj=solicitud, ruta_obj=ruta, camion_obj=camion)

    # 4) Asignar camion a cada tramo (validación de capacidad)
    # Simular consulta a ms-solicitudes -> contenedor info (ya lo tenemos) y validar
    for tramo in ruta.tramos:
        peso = solicitud.contenedor.peso
        volumen = solicitud.contenedor.volumen
        assert peso is not None and volumen is not None
        if peso > camion.capacidad_peso_max:
            raise RuntimeError('Camión no soporta peso')
        if volumen > camion.capacidad_volumen_max:
            raise RuntimeError('Camión no soporta volumen')
        tramo.camion_dominio = camion.dominio
    print('\nCamión asignado a todos los tramos (dominio asignado en campo camion_dominio).')
    take_snapshot('camion_asignado', solicitud_obj=solicitud, ruta_obj=ruta, camion_obj=camion)

    # 5) Simular inicio y fin de cada tramo, calcular costo real al finalizar
    # Simulación depositos info con costoEstadiaDiario (claves como strings)
    depositos_info = {
        str(t.destino_deposito_id): {'costoEstadiaDiario': '20.00'} for t in ruta.tramos
    }

    # Simular tiempos de forma iterativa para N tramos
    now = datetime.now()
    previous_end = None
    gap_hours_between_tramos = 4  # tiempo de espera/hallazgo entre tramos
    for idx, tramo in enumerate(sorted(ruta.tramos, key=lambda x: x.orden)):
        if idx == 0:
            tramo.fecha_hora_inicio_real = now
        else:
            tramo.fecha_hora_inicio_real = previous_end + timedelta(hours=gap_hours_between_tramos)
        # asignar fin según duracion_horas
        tramo.fecha_hora_fin_real = tramo.fecha_hora_inicio_real + timedelta(hours=tramo.duracion_horas or 0)
        # asignar camion (ya debía estar asignado antes, simular asignación si falta)
        if not tramo.camion_dominio:
            tramo.camion_dominio = camion.dominio
        # calcular costo real usando la función de la simulación
        tramo.costo_real = calcular_costo_real_tramo(tramo, camion, TARIFA_EJEMPLO, depositos_info, ruta.tramos)
        print(f"\nTramo {tramo.orden} finalizado: inicio={tramo.fecha_hora_inicio_real}, fin={tramo.fecha_hora_fin_real}, costo_real={tramo.costo_real}")
        previous_end = tramo.fecha_hora_fin_real
        take_snapshot(f'tramo_{tramo.orden}_finalizado', solicitud_obj=solicitud, ruta_obj=ruta, camion_obj=camion)

    # 6) Al finalizar todos, calcular costo final de ruta (suma de tramos + gestion)
    suma_tramos = sum((t.costo_real or Decimal('0.0')) for t in ruta.tramos)
    costo_gestion_total = TARIFA_EJEMPLO.costo_base_gestion_fijo * Decimal(len(ruta.tramos))
    costo_final_ruta = (suma_tramos + costo_gestion_total).quantize(Decimal('0.01'))

    tiempo_real_total = Decimal(str(sum((t.duracion_horas or 0.0) for t in ruta.tramos)))

    solicitud.costo_final = costo_final_ruta
    solicitud.tiempo_real = tiempo_real_total
    take_snapshot('ruta_finalizada', solicitud_obj=solicitud, ruta_obj=ruta, camion_obj=camion)

    print('\nResumen final de la ruta y solicitud:')
    print(json.dumps({
        'ruta_id': ruta.id,
        'suma_tramos': str(suma_tramos),
        'costo_gestion_total': str(costo_gestion_total),
        'costo_final_ruta': str(costo_final_ruta),
        'tiempo_real_total_horas': str(tiempo_real_total)
    }, indent=2))

    print('\nSolicitud actualizado con costo_final y tiempo_real:')
    print(json.dumps(asdict(solicitud), default=str, indent=2))

    # Comprobaciones finales: asegurarse que todos los campos esperados están asignados
    missing = []
    # Check solicitud fields
    for field_name in ['contenedor', 'costo_final', 'tiempo_real', 'ruta_id']:
        if getattr(solicitud, field_name) is None:
            missing.append(f"Solicitud.{field_name}")
    # Check tramos
    for t in ruta.tramos:
        for f in ['camion_dominio', 'fecha_hora_inicio_real', 'fecha_hora_fin_real', 'costo_real']:
            if getattr(t, f) is None:
                missing.append(f"Tramo[{t.id}].{f}")

    if missing:
        print('\nERROR: Campos faltantes detectados:')
        for m in missing:
            print(' -', m)
    else:
        print('\nOK: Todos los campos críticos quedaron asignados en la simulación.')

    # Análisis adicional: detectar campos que NUNCA se llenaron en ninguna snapshot
    def fields_never_set(snapshots_list):
        # Recolectar keys de solicitud y tramos
        solicitud_fields = set()
        tramo_fields = set()
        for s in snapshots_list:
            sol = s.get('solicitud')
            if sol:
                for k, v in sol.items():
                    solicitud_fields.add(k)
            ruta = s.get('ruta')
            if ruta and ruta.get('tramos'):
                for tr in ruta['tramos']:
                    for k, v in tr.items():
                        tramo_fields.add(k)
        # Ahora ver cuáles estuvieron siempre None
        never_set_sol = []
        never_set_tramo = []
        # check solicitud fields
        for key in sorted(solicitud_fields):
            always_none = all((s.get('solicitud') is None) or (s['solicitud'].get(key) is None) for s in snapshots_list)
            if always_none:
                never_set_sol.append(key)
        # check tramo fields
        for key in sorted(tramo_fields):
            always_none = True
            for s in snapshots_list:
                ruta = s.get('ruta')
                if ruta and ruta.get('tramos'):
                    for tr in ruta['tramos']:
                        if tr.get(key) is not None:
                            always_none = False
                            break
                if not always_none:
                    break
            if always_none:
                never_set_tramo.append(key)
        return never_set_sol, never_set_tramo

    never_sol, never_tr = fields_never_set(snapshots)
    print('\n--- Informe: campos que NUNCA se llenaron en ninguna snapshot ---')
    print('Solicitud: ', never_sol)
    print('Tramo:     ', never_tr)

if __name__ == '__main__':
    simulate()
