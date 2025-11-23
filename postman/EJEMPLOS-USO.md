# Ejemplos de Uso - Colección Postman TPI Backend

Este documento contiene ejemplos prácticos de uso de la colección de Postman para los diferentes requerimientos funcionales.

## 📖 Tabla de Contenidos

1. [Ejemplo Completo: Solicitud de Transporte](#ejemplo-completo-solicitud-de-transporte)
2. [Ejemplo: Validación de Capacidad de Camión](#ejemplo-validación-de-capacidad-de-camión)
3. [Ejemplo: Cálculo de Estadía en Depósitos](#ejemplo-cálculo-de-estadía-en-depósitos)
4. [Ejemplo: Flujo de Transportista](#ejemplo-flujo-de-transportista)
5. [Ejemplo: Configuración de Tarifas](#ejemplo-configuración-de-tarifas)

---

## Ejemplo Completo: Solicitud de Transporte

Este ejemplo muestra el flujo completo desde que un cliente solicita un transporte hasta la entrega final.

### Paso 1: Cliente - Crear Solicitud

```http
POST {{baseUrl}}/api/v1/solicitudes
Authorization: Bearer {{access_token_cliente}}
Content-Type: application/json

{
  "direccionOrigen": "Av. Santa Fe 1234, CABA, Argentina",
  "direccionDestino": "Av. Corrientes 5678, CABA, Argentina",
  "clienteEmail": "cliente@example.com",
  "clienteNombre": "Juan Pérez",
  "clienteTelefono": "+54911123456",
  "contenedorPeso": 8000.0,
  "contenedorVolumen": 25.0
}
```

**Response:**
```json
{
  "id": 123,
  "estado": "BORRADOR",
  "contenedorId": 456,
  "clienteId": 789,
  "direccionOrigen": "Av. Santa Fe 1234, CABA, Argentina",
  "direccionDestino": "Av. Corrientes 5678, CABA, Argentina"
}
```

### Paso 2: Operador - Generar Opciones de Rutas

```http
POST {{baseUrl}}/api/v1/rutas/solicitudes/123/opciones
Authorization: Bearer {{access_token_operador}}
```

**Response:**
```json
[
  {
    "id": 1,
    "solicitudId": 123,
    "distanciaTotal": 45.5,
    "tiempoEstimado": 90,
    "costoTotal": 4500.00,
    "tramos": [
      {
        "origen": "Av. Santa Fe 1234, CABA",
        "destino": "Depósito Central",
        "transportista": "Carlos Ramirez",
        "camion": "ABC123",
        "distancia": 25.0,
        "tiempo": 45,
        "costo": 2500.00
      },
      {
        "origen": "Depósito Central",
        "destino": "Av. Corrientes 5678, CABA",
        "transportista": "Laura Fernandez",
        "camion": "XYZ789",
        "distancia": 20.5,
        "tiempo": 45,
        "costo": 2000.00
      }
    ]
  }
]
```

### Paso 3: Operador - Confirmar Ruta

```http
POST {{baseUrl}}/api/v1/solicitudes/123/opciones/1/confirmar
Authorization: Bearer {{access_token_operador}}
```

**Response:**
```json
{
  "id": 123,
  "estado": "PROGRAMADA",
  "rutaId": 50,
  "costoTotal": 4500.00,
  "mensaje": "Ruta asignada exitosamente"
}
```

### Paso 4: Operador - Asignar Camiones a Tramos

```http
POST {{baseUrl}}/api/v1/tramos/1/asignar-transportista?dominio=ABC123
Authorization: Bearer {{access_token_operador}}
```

**Response Exitoso:**
```json
{
  "id": 1,
  "camionDominio": "ABC123",
  "nombreTransportista": "Carlos Ramirez",
  "estado": "PENDIENTE"
}
```

### Paso 5: Transportista - Iniciar Primer Tramo

```http
POST {{baseUrl}}/api/v1/rutas/50/tramos/1/iniciar
Authorization: Bearer {{access_token_transportista}}
```

**Response:**
```json
{
  "id": 1,
  "estado": "EN_PROCESO",
  "fechaHoraInicioReal": "2025-11-23T08:00:00",
  "mensaje": "Tramo iniciado exitosamente"
}
```

### Paso 6: Transportista - Finalizar Primer Tramo

```http
POST {{baseUrl}}/api/v1/rutas/50/tramos/1/finalizar
Authorization: Bearer {{access_token_transportista}}
```

**Response:**
```json
{
  "id": 1,
  "estado": "COMPLETADO",
  "fechaHoraInicioReal": "2025-11-23T08:00:00",
  "fechaHoraFinReal": "2025-11-23T08:45:00",
  "tiempoRealMinutos": 45,
  "mensaje": "Tramo finalizado exitosamente"
}
```

### Paso 7: Operador - Cálculo Final con Estadía

```http
POST {{baseUrl}}/api/v1/solicitudes/123/calcular-precio
Authorization: Bearer {{access_token_operador}}
```

**Response:**
```json
{
  "solicitudId": 123,
  "costoTotal": 5100.00,
  "distanciaTotal": 45.5,
  "tiempoRealTotal": 180,
  "detalles": {
    "costoDistancia": 3000.00,
    "costoPesoVolumen": 1500.00,
    "costoEstadia": 600.00,
    "estadiaDepositos": [
      {
        "depositoId": 1,
        "nombre": "Depósito Central",
        "fechaEntrada": "2025-11-23T08:45:00",
        "fechaSalida": "2025-11-23T14:45:00",
        "horasEstadia": 6,
        "tarifaPorHora": 100.00,
        "costoEstadia": 600.00
      }
    ]
  }
}
```

---

## Ejemplo: Validación de Capacidad de Camión

Este ejemplo muestra cómo el sistema valida la capacidad del camión al asignarlo a un tramo.

### Escenario 1: Asignación Exitosa

**Datos:**
- Contenedor: Peso = 8000 kg, Volumen = 25 m³
- Camión ABC123: Capacidad Peso = 15000 kg, Capacidad Volumen = 40 m³

**Request:**
```http
POST {{baseUrl}}/api/v1/tramos/1/asignar-transportista?dominio=ABC123
Authorization: Bearer {{access_token_operador}}
```

**Response (200 OK):**
```json
{
  "id": 1,
  "camionDominio": "ABC123",
  "capacidadPeso": 15000.0,
  "capacidadVolumen": 40.0,
  "contenedorPeso": 8000.0,
  "contenedorVolumen": 25.0,
  "validacion": "OK",
  "mensaje": "Camión asignado exitosamente"
}
```

### Escenario 2: Excede Capacidad de Peso

**Datos:**
- Contenedor: Peso = 18000 kg, Volumen = 25 m³
- Camión ABC123: Capacidad Peso = 15000 kg, Capacidad Volumen = 40 m³

**Request:**
```http
POST {{baseUrl}}/api/v1/tramos/1/asignar-transportista?dominio=ABC123
Authorization: Bearer {{access_token_operador}}
```

**Response (400 Bad Request):**
```json
{
  "error": "Validación de capacidad fallida",
  "mensaje": "El peso del contenedor (18000.0 kg) supera la capacidad del camión (15000.0 kg)",
  "detalles": {
    "contenedorPeso": 18000.0,
    "camionCapacidadPeso": 15000.0,
    "excedente": 3000.0
  }
}
```

### Escenario 3: Excede Capacidad de Volumen

**Datos:**
- Contenedor: Peso = 8000 kg, Volumen = 50 m³
- Camión ABC123: Capacidad Peso = 15000 kg, Capacidad Volumen = 40 m³

**Request:**
```http
POST {{baseUrl}}/api/v1/tramos/1/asignar-transportista?dominio=ABC123
Authorization: Bearer {{access_token_operador}}
```

**Response (400 Bad Request):**
```json
{
  "error": "Validación de capacidad fallida",
  "mensaje": "El volumen del contenedor (50.0 m³) supera la capacidad del camión (40.0 m³)",
  "detalles": {
    "contenedorVolumen": 50.0,
    "camionCapacidadVolumen": 40.0,
    "excedente": 10.0
  }
}
```

---

## Ejemplo: Cálculo de Estadía en Depósitos

Este ejemplo ilustra cómo se calcula la estadía en depósitos basándose en fechas reales.

### Contexto

Ruta con 3 tramos:
1. **Tramo 1**: Origen → Depósito A
2. **Tramo 2**: Depósito A → Depósito B
3. **Tramo 3**: Depósito B → Destino

### Paso 1: Finalizar Tramo 1 (Llegada a Depósito A)

```http
POST {{baseUrl}}/api/v1/rutas/50/tramos/1/finalizar
Authorization: Bearer {{access_token_transportista}}
```

**Sistema registra:**
- Fecha de llegada a Depósito A: `2025-11-23T10:00:00`

### Paso 2: Iniciar Tramo 2 (Salida de Depósito A)

```http
POST {{baseUrl}}/api/v1/rutas/50/tramos/2/iniciar
Authorization: Bearer {{access_token_transportista}}
```

**Sistema registra:**
- Fecha de salida de Depósito A: `2025-11-23T16:00:00`

**Cálculo de estadía en Depósito A:**
```
Estadía = 16:00:00 - 10:00:00 = 6 horas
Costo = 6 horas × $100/hora = $600
```

### Paso 3: Finalizar Tramo 2 (Llegada a Depósito B)

```http
POST {{baseUrl}}/api/v1/rutas/50/tramos/2/finalizar
Authorization: Bearer {{access_token_transportista}}
```

**Sistema registra:**
- Fecha de llegada a Depósito B: `2025-11-23T18:00:00`

### Paso 4: Iniciar Tramo 3 (Salida de Depósito B)

```http
POST {{baseUrl}}/api/v1/rutas/50/tramos/3/iniciar
Authorization: Bearer {{access_token_transportista}}
```

**Sistema registra:**
- Fecha de salida de Depósito B: `2025-11-24T08:00:00` (día siguiente)

**Cálculo de estadía en Depósito B:**
```
Estadía = 08:00:00 (día 24) - 18:00:00 (día 23) = 14 horas
Costo = 14 horas × $120/hora = $1,680
```

### Cálculo Total de Estadía

```http
POST {{baseUrl}}/api/v1/solicitudes/123/calcular-precio
Authorization: Bearer {{access_token_operador}}
```

**Response:**
```json
{
  "solicitudId": 123,
  "costoTotal": 8280.00,
  "detalles": {
    "costoDistancia": 4000.00,
    "costoPesoVolumen": 2000.00,
    "costoEstadia": 2280.00,
    "estadiaDepositos": [
      {
        "depositoId": 1,
        "nombre": "Depósito A",
        "fechaEntrada": "2025-11-23T10:00:00",
        "fechaSalida": "2025-11-23T16:00:00",
        "horasEstadia": 6,
        "tarifaPorHora": 100.00,
        "costoEstadia": 600.00
      },
      {
        "depositoId": 2,
        "nombre": "Depósito B",
        "fechaEntrada": "2025-11-23T18:00:00",
        "fechaSalida": "2025-11-24T08:00:00",
        "horasEstadia": 14,
        "tarifaPorHora": 120.00,
        "costoEstadia": 1680.00
      }
    ]
  }
}
```

---

## Ejemplo: Flujo de Transportista

Este ejemplo muestra cómo un transportista gestiona sus tramos asignados.

### Paso 1: Autenticación

```http
POST {{keycloakUrl}}/realms/tpi-backend/protocol/openid-connect/token
Content-Type: application/x-www-form-urlencoded

grant_type=password
&client_id=postman-test
&client_secret=secret-postman-123
&username=carlos.ramirez
&password=1234
```

### Paso 2: Ver Tramos Asignados

```http
GET {{baseUrl}}/api/v1/tramos/por-ruta/50
Authorization: Bearer {{access_token_transportista}}
```

**Response:**
```json
[
  {
    "id": 1,
    "rutaId": 50,
    "origen": "Av. Santa Fe 1234, CABA",
    "destino": "Depósito Central",
    "camionDominio": "ABC123",
    "nombreTransportista": "Carlos Ramirez",
    "estado": "PENDIENTE",
    "distanciaEstimada": 25.0,
    "tiempoEstimado": 45
  },
  {
    "id": 2,
    "rutaId": 50,
    "origen": "Depósito Central",
    "destino": "Av. Corrientes 5678, CABA",
    "camionDominio": "ABC123",
    "nombreTransportista": "Carlos Ramirez",
    "estado": "PENDIENTE",
    "distanciaEstimada": 20.5,
    "tiempoEstimado": 45
  }
]
```

### Paso 3: Iniciar Primer Tramo

```http
POST {{baseUrl}}/api/v1/rutas/50/tramos/1/iniciar
Authorization: Bearer {{access_token_transportista}}
```

**Hora actual del sistema:** `2025-11-23T08:00:00`

**Response:**
```json
{
  "id": 1,
  "estado": "EN_PROCESO",
  "fechaHoraInicioReal": "2025-11-23T08:00:00",
  "mensaje": "Tramo iniciado. Buen viaje!"
}
```

### Paso 4: Finalizar Primer Tramo

```http
POST {{baseUrl}}/api/v1/rutas/50/tramos/1/finalizar
Authorization: Bearer {{access_token_transportista}}
```

**Hora actual del sistema:** `2025-11-23T08:50:00`

**Response:**
```json
{
  "id": 1,
  "estado": "COMPLETADO",
  "fechaHoraInicioReal": "2025-11-23T08:00:00",
  "fechaHoraFinReal": "2025-11-23T08:50:00",
  "tiempoRealMinutos": 50,
  "tiempoEstimadoMinutos": 45,
  "diferencia": 5,
  "mensaje": "Tramo completado exitosamente"
}
```

### Paso 5: Iniciar Segundo Tramo

```http
POST {{baseUrl}}/api/v1/rutas/50/tramos/2/iniciar
Authorization: Bearer {{access_token_transportista}}
```

**Hora actual:** `2025-11-23T14:00:00` (después de estadía)

**Response:**
```json
{
  "id": 2,
  "estado": "EN_PROCESO",
  "fechaHoraInicioReal": "2025-11-23T14:00:00",
  "estadiaDepositoHoras": 5.17,
  "mensaje": "Tramo iniciado desde Depósito Central"
}
```

### Paso 6: Finalizar Segundo Tramo (Último)

```http
POST {{baseUrl}}/api/v1/rutas/50/tramos/2/finalizar
Authorization: Bearer {{access_token_transportista}}
```

**Hora actual:** `2025-11-23T14:45:00`

**Response:**
```json
{
  "id": 2,
  "estado": "COMPLETADO",
  "fechaHoraInicioReal": "2025-11-23T14:00:00",
  "fechaHoraFinReal": "2025-11-23T14:45:00",
  "tiempoRealMinutos": 45,
  "esUltimoTramo": true,
  "solicitudEstado": "ENTREGADA",
  "mensaje": "¡Entrega completada! La solicitud ha sido marcada como ENTREGADA."
}
```

---

## Ejemplo: Configuración de Tarifas

Este ejemplo muestra cómo configurar tarifas con rangos de peso y volumen.

### Paso 1: Crear Tarifa Base

```http
POST {{baseUrl}}/api/v1/tarifas
Authorization: Bearer {{access_token_operador}}
Content-Type: application/json

{
  "nombre": "Tarifa Premium",
  "descripcion": "Tarifa para contenedores de alto valor",
  "costoBase": 1000.0,
  "costoPorKm": 15.0,
  "activa": true
}
```

**Response:**
```json
{
  "id": 5,
  "nombre": "Tarifa Premium",
  "descripcion": "Tarifa para contenedores de alto valor",
  "costoBase": 1000.0,
  "costoPorKm": 15.0,
  "activa": true,
  "rangos": []
}
```

### Paso 2: Agregar Rango Pequeño (Multiplicador Normal)

```http
POST {{baseUrl}}/api/v1/tarifas/5/rango
Authorization: Bearer {{access_token_operador}}
Content-Type: application/json

{
  "pesoMin": 0.0,
  "pesoMax": 5000.0,
  "volumenMin": 0.0,
  "volumenMax": 15.0,
  "multiplicador": 1.0
}
```

**Response:**
```json
{
  "id": 5,
  "nombre": "Tarifa Premium",
  "rangos": [
    {
      "id": 10,
      "pesoMin": 0.0,
      "pesoMax": 5000.0,
      "volumenMin": 0.0,
      "volumenMax": 15.0,
      "multiplicador": 1.0
    }
  ]
}
```

### Paso 3: Agregar Rango Mediano (Multiplicador +20%)

```http
POST {{baseUrl}}/api/v1/tarifas/5/rango
Authorization: Bearer {{access_token_operador}}
Content-Type: application/json

{
  "pesoMin": 5000.0,
  "pesoMax": 15000.0,
  "volumenMin": 15.0,
  "volumenMax": 40.0,
  "multiplicador": 1.2
}
```

### Paso 4: Agregar Rango Grande (Multiplicador +50%)

```http
POST {{baseUrl}}/api/v1/tarifas/5/rango
Authorization: Bearer {{access_token_operador}}
Content-Type: application/json

{
  "pesoMin": 15000.0,
  "pesoMax": 30000.0,
  "volumenMin": 40.0,
  "volumenMax": 80.0,
  "multiplicador": 1.5
}
```

### Resultado Final: Ver Tarifa Completa

```http
GET {{baseUrl}}/api/v1/tarifas/5
Authorization: Bearer {{access_token_operador}}
```

**Response:**
```json
{
  "id": 5,
  "nombre": "Tarifa Premium",
  "descripcion": "Tarifa para contenedores de alto valor",
  "costoBase": 1000.0,
  "costoPorKm": 15.0,
  "activa": true,
  "rangos": [
    {
      "id": 10,
      "pesoMin": 0.0,
      "pesoMax": 5000.0,
      "volumenMin": 0.0,
      "volumenMax": 15.0,
      "multiplicador": 1.0,
      "descripcion": "Contenedores pequeños - Precio normal"
    },
    {
      "id": 11,
      "pesoMin": 5000.0,
      "pesoMax": 15000.0,
      "volumenMin": 15.0,
      "volumenMax": 40.0,
      "multiplicador": 1.2,
      "descripcion": "Contenedores medianos - +20%"
    },
    {
      "id": 12,
      "pesoMin": 15000.0,
      "pesoMax": 30000.0,
      "volumenMin": 40.0,
      "volumenMax": 80.0,
      "multiplicador": 1.5,
      "descripcion": "Contenedores grandes - +50%"
    }
  ]
}
```

### Ejemplo de Cálculo con Tarifa Premium

**Contenedor:** Peso = 12000 kg, Volumen = 30 m³  
**Distancia:** 50 km  
**Rango aplicable:** Mediano (multiplicador 1.2)

```
Costo Base = $1000
Costo por Distancia = 50 km × $15/km = $750
Subtotal = $1000 + $750 = $1750
Costo Final = $1750 × 1.2 = $2100
```

---

## 💡 Consejos y Mejores Prácticas

### 1. Gestión de Tokens

- Los tokens expiran después de cierto tiempo
- Siempre ejecuta el endpoint de autenticación antes de iniciar trabajo
- Los scripts de test guardan el token automáticamente

### 2. Validación de Capacidad

- Verifica capacidades del camión antes de asignar
- Usa el endpoint `7.2 Ver Detalle de Camión` para confirmar capacidades
- Si falla la asignación, busca un camión con mayor capacidad

### 3. Cálculo de Estadía

- La estadía solo se calcula cuando hay tramos intermedios en depósitos
- Asegúrate de que los transportistas marquen inicio/fin de tramos correctamente
- El sistema usa la diferencia entre fecha de llegada y fecha de salida

### 4. Flujo de Estados

- Respeta las transiciones de estado válidas
- Usa `6.1 Cambiar Estado` solo cuando sea necesario
- Los cambios automáticos (al iniciar/finalizar tramos) son preferibles

### 5. Gestión de Errores

- Lee los mensajes de error descriptivos
- Los errores 400 indican validación fallida
- Los errores 404 indican recurso no encontrado
- Los errores 403 indican falta de permisos

---

**Documento creado:** Noviembre 2025  
**Propósito:** Guía práctica de uso de la colección Postman
