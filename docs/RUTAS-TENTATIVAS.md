# Rutas Tentativas - Documentación

## 📋 Descripción General

El sistema ahora soporta el cálculo de **rutas tentativas** que consideran depósitos intermedios y calculan distancias reales usando OSRM (Open Source Routing Machine).

## 🔄 Mejoras Implementadas

### 1. Cálculo de Distancias con Depósitos

El servicio `CalculoService` ahora detecta automáticamente cuando se proporcionan **IDs de depósitos** en lugar de nombres de ciudades:

**Formatos soportados:**
- **ID de depósito** (número): `"1"`, `"2"`, `"15"` → Consulta coordenadas desde la BD
- **Nombre de ciudad**: `"Buenos Aires"`, `"Córdoba"` → Usa mapa estático de ciudades
- **Coordenadas directas**: `"-34.6037,-58.3816"` → Parsea latitud y longitud

**Ejemplo de llamada:**
```bash
POST /api/v1/gestion/distancia
{
  "origen": "1",      # ID de depósito
  "destino": "5"      # ID de depósito
}
```

### 2. Endpoint de Coordenadas de Depósitos

Nuevo endpoint para consultar coordenadas específicas:

```bash
GET /api/v1/depositos/{id}/coordenadas
```

**Respuesta:**
```json
{
  "depositoId": 1,
  "nombre": "Depósito Central Buenos Aires",
  "latitud": -34.6037,
  "longitud": -58.3816
}
```

### 3. Cálculo de Rutas Tentativas

Nuevo servicio y endpoint para calcular rutas completas con múltiples depósitos:

```bash
GET /api/v1/rutas/tentativa?origenDepositoId=1&destinoDepositoId=5
```

**Con depósitos intermedios:**
```bash
GET /api/v1/rutas/tentativa?origenDepositoId=1&destinoDepositoId=5&intermedios=2,3,4
```

**Respuesta:**
```json
{
  "depositosIds": [1, 2, 3, 4, 5],
  "depositosNombres": [
    "Depósito Central Buenos Aires",
    "Depósito Rosario",
    "Depósito Córdoba",
    "Depósito Mendoza",
    "Depósito Salta"
  ],
  "distanciaTotal": 1523.45,
  "duracionTotalHoras": 18.5,
  "numeroTramos": 4,
  "tramos": [
    {
      "orden": 1,
      "origenDepositoId": 1,
      "origenDepositoNombre": "Depósito Central Buenos Aires",
      "destinoDepositoId": 2,
      "destinoDepositoNombre": "Depósito Rosario",
      "distanciaKm": 298.12,
      "duracionHoras": 3.2
    },
    {
      "orden": 2,
      "origenDepositoId": 2,
      "origenDepositoNombre": "Depósito Rosario",
      "destinoDepositoId": 3,
      "destinoDepositoNombre": "Depósito Córdoba",
      "distanciaKm": 401.23,
      "duracionHoras": 4.8
    }
    // ... más tramos
  ],
  "exitoso": true,
  "mensaje": "Ruta tentativa calculada exitosamente con 4 tramos"
}
```

## 🚀 Casos de Uso

### Caso 1: Calcular Distancia Entre Depósitos

```bash
# Usando el endpoint de distancia con IDs de depósitos
curl -X POST "http://localhost:8080/api/v1/gestion/distancia" \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "origen": "1",
    "destino": "5"
  }'
```

### Caso 2: Crear Tramo con Depósitos

```bash
# Al crear un tramo, el sistema ahora calculará correctamente la distancia
curl -X POST "http://localhost:8080/api/v1/rutas/1/tramos" \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "idRuta": 1,
    "origenDepositoId": 1,
    "destinoDepositoId": 5
  }'
```

### Caso 3: Planificar Ruta Completa

```bash
# Calcular ruta tentativa con depósitos intermedios
curl -X GET "http://localhost:8080/api/v1/rutas/tentativa?origenDepositoId=1&destinoDepositoId=5&intermedios=2,3" \
  -H "Authorization: Bearer ${TOKEN}"
```

## 📊 Flujo de Trabajo Recomendado

### Para Crear una Ruta de Transporte:

1. **Calcular Ruta Tentativa** (opcional, para planificación):
   ```bash
   GET /api/v1/rutas/tentativa?origenDepositoId=1&destinoDepositoId=5
   ```

2. **Crear la Ruta** en el sistema:
   ```bash
   POST /api/v1/rutas
   {
     "idSolicitud": 123
   }
   ```

3. **Agregar Tramos** basados en la ruta tentativa:
   ```bash
   POST /api/v1/rutas/{rutaId}/tramos
   {
     "origenDepositoId": 1,
     "destinoDepositoId": 2
   }
   ```

4. **Asignar Transportistas** a cada tramo:
   ```bash
   POST /api/v1/tramos/{tramoId}/asignar-transportista?camionId=10
   ```

## ⚙️ Configuración de Depósitos

Para que el sistema funcione correctamente, los depósitos deben tener coordenadas configuradas:

```bash
# Actualizar depósito con coordenadas
PATCH /api/v1/depositos/1
{
  "latitud": -34.6037,
  "longitud": -58.3816
}
```

## 🔍 Validaciones

El sistema realiza las siguientes validaciones:

- ✅ Verifica que los depósitos existan en la base de datos
- ✅ Valida que los depósitos tengan coordenadas configuradas
- ✅ Calcula distancias reales usando OSRM con datos de OpenStreetMap
- ✅ Usa Haversine como fallback si OSRM no está disponible
- ✅ Evita rutas con depósitos origen y destino iguales

## 🐛 Troubleshooting

### Error: "Depósito no tiene coordenadas configuradas"

**Solución:** Actualizar el depósito con sus coordenadas:
```bash
PATCH /api/v1/depositos/{id}
{
  "latitud": -34.6037,
  "longitud": -58.3816
}
```

### Error: Distancia incorrecta (siempre ~300km)

**Causa:** Versión antigua del código que no consultaba depósitos.

**Solución:** Las mejoras implementadas ahora consultan correctamente las coordenadas de los depósitos.

### OSRM no disponible

Si el servicio OSRM no está disponible, el sistema automáticamente usa la fórmula de Haversine como fallback. Las distancias serán aproximadas (línea recta) en lugar de rutas reales.

## 📝 Notas Técnicas

- Las distancias se calculan usando OSRM con datos de OpenStreetMap de Argentina
- El servicio OSRM se ejecuta en un contenedor Docker separado
- Las coordenadas se almacenan como BigDecimal en la BD para mayor precisión
- El sistema soporta rutas con múltiples tramos y depósitos intermedios
- Todas las distancias se redondean a 2 decimales

## 🔐 Permisos Requeridos

- **CLIENTE, RESPONSABLE, ADMIN, TRANSPORTISTA**: Pueden consultar rutas tentativas
- **RESPONSABLE, ADMIN**: Pueden crear rutas y tramos
- **ADMIN, OPERADOR, RESPONSABLE**: Pueden actualizar depósitos
