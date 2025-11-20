# Integración OSRM - Cálculo de Rutas

Este microservicio ahora incluye integración con **OSRM (Open Source Routing Machine)** para cálculo de distancias y tiempos de viaje.

## 🌍 ¿Qué es OSRM?

OSRM es un motor de routing open-source de alto rendimiento diseñado para encontrar las rutas más cortas en redes viales. 

- **Servidor público**: `https://router.project-osrm.org`
- **Datos**: OpenStreetMap
- **Ventajas**: Gratuito, rápido, sin límites de API (servidor público)

## 📡 Endpoints Disponibles

### 1. Calcular Ruta Simple (POST)

Calcula distancia, duración y geometría entre dos puntos.

```http
POST http://localhost:8080/api/v1/osrm/ruta
Authorization: Bearer {{token}}
Content-Type: application/json

{
  "origen": {
    "latitud": -34.603722,
    "longitud": -58.381592
  },
  "destino": {
    "latitud": -34.609722,
    "longitud": -58.371592
  }
}
```

**Respuesta**:
```json
{
  "distanciaKm": 1.23,
  "duracionHoras": 0.05,
  "duracionMinutos": 3.2,
  "geometry": "encoded_polyline_string",
  "resumen": "Av. Corrientes",
  "exitoso": true,
  "mensaje": "Ruta calculada exitosamente"
}
```

### 2. Calcular Ruta Simple (GET)

Alternativa GET para cálculos simples.

```http
GET http://localhost:8080/api/v1/osrm/ruta-simple?origenLat=-34.603722&origenLon=-58.381592&destinoLat=-34.609722&destinoLon=-58.371592
Authorization: Bearer {{token}}
```

### 3. Calcular Ruta con Múltiples Waypoints (POST)

Calcula una ruta que pasa por múltiples puntos.

```http
POST http://localhost:8080/api/v1/osrm/ruta-multiple
Authorization: Bearer {{token}}
Content-Type: application/json

{
  "coordenadas": [
    {
      "latitud": -34.603722,
      "longitud": -58.381592
    },
    {
      "latitud": -34.609722,
      "longitud": -58.371592
    },
    {
      "latitud": -34.615722,
      "longitud": -58.365592
    }
  ]
}
```

### 4. Obtener Solo Distancia y Duración (GET)

Endpoint simplificado que retorna solo distancia y duración.

```http
GET http://localhost:8080/api/v1/maps/distancia-osrm?origenLat=-34.603722&origenLon=-58.381592&destinoLat=-34.609722&destinoLon=-58.371592
Authorization: Bearer {{token}}
```

**Respuesta**:
```json
{
  "distancia": 1.23,
  "duracion": 0.05
}
```

## 🗺️ Coordenadas de Ejemplo (Argentina)

### Buenos Aires
- **Obelisco**: `-34.603722, -58.381592`
- **Casa Rosada**: `-34.608147, -58.370226`
- **Puerto Madero**: `-34.611667, -58.361944`
- **Palermo**: `-34.588889, -58.421944`

### Córdoba
- **Centro**: `-31.420083, -64.188776`
- **Nueva Córdoba**: `-31.423889, -64.188889`

### Rosario
- **Monumento a la Bandera**: `-32.947368, -60.630589`

## 🔧 Configuración

El servicio está configurado en `application.yml`:

```yaml
app:
  osrm:
    base-url: https://router.project-osrm.org
```

Para usar un servidor OSRM propio:
```yaml
app:
  osrm:
    base-url: http://tu-servidor-osrm:5000
```

## 🧪 Ejemplos con PowerShell

### 1. Calcular ruta Buenos Aires - Palermo

```powershell
# Obtener token
$body = @{
    grant_type='password'
    client_id='postman-test'
    client_secret='secret-postman-123'
    username='tester'
    password='1234'
}
$resp = Invoke-RestMethod -Uri "http://localhost:8089/realms/tpi-backend/protocol/openid-connect/token" -Method Post -Body $body
$token = $resp.access_token

# Calcular ruta
$headers = @{ Authorization = "Bearer $token"; 'Content-Type' = 'application/json' }
$ruta = @{
    origen = @{ latitud = -34.603722; longitud = -58.381592 }
    destino = @{ latitud = -34.588889; longitud = -58.421944 }
} | ConvertTo-Json

$resultado = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/osrm/ruta" -Method Post -Headers $headers -Body $ruta
Write-Host "Distancia: $($resultado.distanciaKm) km"
Write-Host "Duración: $($resultado.duracionMinutos) minutos"
```

### 2. Ruta con múltiples paradas

```powershell
$rutaMultiple = @{
    coordenadas = @(
        @{ latitud = -34.603722; longitud = -58.381592 },  # Obelisco
        @{ latitud = -34.608147; longitud = -58.370226 },  # Casa Rosada
        @{ latitud = -34.611667; longitud = -58.361944 }   # Puerto Madero
    )
} | ConvertTo-Json -Depth 5

$resultado = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/osrm/ruta-multiple" -Method Post -Headers $headers -Body $rutaMultiple
Write-Host "Distancia total: $($resultado.distanciaKm) km"
Write-Host "Duración total: $($resultado.duracionMinutos) minutos"
```

## 📊 Formato de Respuesta

### RutaCalculadaDTO

| Campo | Tipo | Descripción |
|-------|------|-------------|
| `distanciaKm` | Double | Distancia en kilómetros (2 decimales) |
| `duracionHoras` | Double | Duración en horas (2 decimales) |
| `duracionMinutos` | Double | Duración en minutos (2 decimales) |
| `geometry` | String | Polyline codificada para visualización en mapas |
| `resumen` | String | Descripción breve de la ruta |
| `exitoso` | Boolean | Indica si el cálculo fue exitoso |
| `mensaje` | String | Mensaje descriptivo del resultado |

## 🔐 Roles Requeridos

- **OPERADOR**: Puede calcular rutas
- **TRANSPORTISTA**: Puede calcular rutas
- **ADMIN**: Acceso completo

## 🌐 Visualización de Geometría

La respuesta incluye un campo `geometry` con la ruta codificada en formato Polyline. Puedes decodificarla y visualizarla en Google Maps, Leaflet, etc.

**Ejemplo con Leaflet**:
```javascript
var polyline = L.Polyline.fromEncoded(geometry);
map.addLayer(polyline);
map.fitBounds(polyline.getBounds());
```

## ⚡ Rendimiento

- **Latencia típica**: 50-200ms para rutas simples
- **Sin límites de API** (servidor público)
- **Caché recomendado**: Para rutas frecuentes, considera cachear resultados

## 🔄 Migración desde Google Maps

Si actualmente usas Google Maps a través de `ms-gestion-calculos`, OSRM ofrece:

- ✅ **Sin costos de API**
- ✅ **Menor latencia**
- ✅ **Datos actualizados de OpenStreetMap**
- ⚠️ **Menos información de tráfico en tiempo real**

El endpoint legacy `/api/v1/maps/distancia` sigue disponible para compatibilidad.

## 📝 Notas

- Las coordenadas deben estar en formato **decimal** (no grados/minutos/segundos)
- OSRM usa **longitud, latitud** internamente, pero nuestros DTOs usan **latitud, longitud** (formato común)
- Para Argentina, el servidor público de OSRM tiene buenos datos de OpenStreetMap
