# Resumen de Implementación OSRM

## ✅ Implementación Completa

Se ha integrado exitosamente **OSRM (Open Source Routing Machine)** en el microservicio `ms-rutas-transportistas` para cálculo de distancias y tiempos de viaje.

## 📁 Archivos Creados

### DTOs (Data Transfer Objects)
- `dtos/osrm/OSRMRouteResponse.java` - Respuesta completa de OSRM
- `dtos/osrm/OSRMRoute.java` - Datos de una ruta individual
- `dtos/osrm/OSRMLeg.java` - Segmento de ruta entre dos waypoints
- `dtos/osrm/OSRMStep.java` - Paso individual en las instrucciones
- `dtos/osrm/OSRMWaypoint.java` - Punto de parada en la ruta
- `dtos/osrm/RutaCalculadaDTO.java` - DTO simplificado para respuesta al cliente
- `dtos/osrm/CoordenadaDTO.java` - Representa una coordenada geográfica (lat, lon)

### Servicios
- `services/OSRMService.java` - Servicio para comunicación con OSRM API
  - `calcularRuta(origen, destino)` - Calcula ruta entre dos puntos
  - `calcularRutaMultiple(coordenadas...)` - Calcula ruta con múltiples waypoints

### Controladores
- `controllers/OSRMController.java` - Endpoints REST para cálculo de rutas
  - `POST /api/v1/osrm/ruta` - Calcular ruta entre dos puntos
  - `POST /api/v1/osrm/ruta-multiple` - Ruta con múltiples waypoints
  - `GET /api/v1/osrm/ruta-simple` - Alternativa GET con query params

### Documentación
- `ms-rutas-transportistas/README-OSRM.md` - Guía completa de uso
- `scripts/test-osrm.ps1` - Script de pruebas automatizadas

## 📝 Archivos Modificados

### Configuración
- `ms-rutas-transportistas/src/main/resources/application.yml`
  - Agregado: `app.osrm.base-url: https://router.project-osrm.org`

### Servicios Existentes
- `services/MapsService.java` - Agregado método `getDistanciaConOSRM()`
- `controllers/MapsController.java` - Agregado endpoint `GET /api/v1/maps/distancia-osrm`
- `dtos/DistanciaResponseDTO.java` - Agregado campo `duracion`

### Documentación General
- `README.md` - Agregada sección de integración OSRM
- `postman/README-APIS.md` - Agregados ejemplos de endpoints OSRM
- `scripts/smoke-test.ps1` - Agregado test de OSRM

## 🚀 Endpoints Disponibles

### 1. Calcular Ruta Completa (POST)
```
POST http://localhost:8080/api/v1/osrm/ruta
Body: { "origen": {...}, "destino": {...} }
```
**Retorna**: Distancia, duración, geometría, resumen

### 2. Ruta Múltiple (POST)
```
POST http://localhost:8080/api/v1/osrm/ruta-multiple
Body: { "coordenadas": [{...}, {...}, {...}] }
```

### 3. Ruta Simple (GET)
```
GET http://localhost:8080/api/v1/osrm/ruta-simple?origenLat=...&origenLon=...&destinoLat=...&destinoLon=...
```

### 4. Solo Distancia (GET)
```
GET http://localhost:8080/api/v1/maps/distancia-osrm?origenLat=...&origenLon=...&destinoLat=...&destinoLon=...
```

## 🧪 Cómo Probar

### Opción 1: Script Automatizado
```powershell
.\scripts\test-osrm.ps1
```

### Opción 2: Smoke Test General
```powershell
.\scripts\smoke-test.ps1
```
*(Ahora incluye prueba de OSRM)*

### Opción 3: Manual con PowerShell
```powershell
# 1. Obtener token
$body = @{grant_type='password';client_id='postman-test';client_secret='secret-postman-123';username='tester';password='1234'}
$resp = Invoke-RestMethod -Uri "http://localhost:8089/realms/tpi-backend/protocol/openid-connect/token" -Method Post -Body $body
$token = $resp.access_token

# 2. Calcular ruta (Obelisco a Casa Rosada)
$headers = @{Authorization="Bearer $token";'Content-Type'='application/json'}
$ruta = '{"origen":{"latitud":-34.603722,"longitud":-58.381592},"destino":{"latitud":-34.608147,"longitud":-58.370226}}'
$resultado = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/osrm/ruta" -Method Post -Headers $headers -Body $ruta

# 3. Ver resultado
Write-Host "Distancia: $($resultado.distanciaKm) km"
Write-Host "Duración: $($resultado.duracionMinutos) minutos"
```

## 📊 Ejemplo de Respuesta

```json
{
  "distanciaKm": 1.23,
  "duracionHoras": 0.05,
  "duracionMinutos": 3.2,
  "geometry": "oav~FltazGZhAv@tBb@pA\\~@Vr@Xl@Xj@",
  "resumen": "Av. Corrientes",
  "exitoso": true,
  "mensaje": "Ruta calculada exitosamente"
}
```

## 🔐 Seguridad

- **Roles permitidos**: RESPONSABLE, TRANSPORTISTA, ADMIN
- **Autenticación**: JWT Bearer token (OAuth2)
- **Gateway**: Todas las peticiones pasan por el API Gateway (puerto 8080)

## ⚙️ Configuración

### Opción 1: Servidor Público (Por Defecto)
```yaml
app:
  osrm:
    base-url: https://router.project-osrm.org
```
**Ventaja**: No requiere instalación local  
**Desventaja**: Limitado a datos globales de OpenStreetMap

### Opción 2: Servidor Local con Docker (Recomendado para Argentina)

#### Paso 1: Descargar datos de Argentina
Los archivos de datos de OSRM son demasiado grandes para Git (>100MB). Descárgalos desde:

**Opción A - Geofabrik (Recomendado)**:
```powershell
# Crear directorio si no existe
New-Item -ItemType Directory -Force -Path osrm-data

# Descargar datos de Argentina (más recientes)
Invoke-WebRequest -Uri "http://download.geofabrik.de/south-america/argentina-latest.osm.pbf" `
  -OutFile "osrm-data/argentina-latest.osm.pbf"
```

**Opción B - Usar datos incluidos**:
Si ya tienes los archivos `argentina-251114.osm.*` en `osrm-data/`, puedes usarlos directamente.

#### Paso 2: Procesar datos con OSRM (solo la primera vez)
```powershell
# Extraer datos
docker run -t -v "${PWD}/osrm-data:/data" osrm/osrm-backend osrm-extract -p /opt/car.lua /data/argentina-latest.osm.pbf

# Particionar datos
docker run -t -v "${PWD}/osrm-data:/data" osrm/osrm-backend osrm-partition /data/argentina-latest.osrm

# Personalizar datos
docker run -t -v "${PWD}/osrm-data:/data" osrm/osrm-backend osrm-customize /data/argentina-latest.osrm
```

**Nota**: Estos comandos solo se ejecutan UNA vez. Los archivos procesados (`.osrm`, `.osrm.hsgr`, etc.) se guardan en `osrm-data/` y se reutilizan.

#### Paso 3: Levantar servidor OSRM local
```powershell
docker-compose -f docker-compose.osrm.yml up -d
```

#### Paso 4: Actualizar configuración del microservicio
```yaml
# ms-rutas-transportistas/src/main/resources/application.yml
app:
  osrm:
    base-url: http://osrm:5000  # Para Docker Compose
    # O http://localhost:5000 para acceso local directo
```

### Verificar instalación local
```powershell
# Probar servidor OSRM directamente
Invoke-RestMethod "http://localhost:5000/route/v1/driving/-58.381592,-34.603722;-58.370226,-34.608147?overview=false"
```

### 📦 Archivos OSRM Generados

Después del procesamiento, tendrás estos archivos en `osrm-data/`:
- `argentina-latest.osm.pbf` - Datos originales de OpenStreetMap (~500MB)
- `argentina-latest.osrm` - Datos procesados base
- `argentina-latest.osrm.hsgr` - Grafo de jerarquía de contracción
- `argentina-latest.osrm.fileIndex` - Índice de archivos
- `argentina-latest.osrm.geometry` - Geometría de rutas
- `argentina-latest.osrm.names` - Nombres de calles
- Y otros archivos auxiliares...

**⚠️ Importante**: Estos archivos NO se suben a Git debido a su tamaño. Cada desarrollador debe descargarlos y procesarlos localmente.

## 🌍 Coordenadas de Prueba (Argentina)

**Buenos Aires**:
- Obelisco: `-34.603722, -58.381592`
- Casa Rosada: `-34.608147, -58.370226`
- Puerto Madero: `-34.611667, -58.361944`
- Palermo: `-34.588889, -58.421944`

**Córdoba**:
- Centro: `-31.420083, -64.188776`
- Nueva Córdoba: `-31.423889, -64.188889`

**Rosario**:
- Monumento a la Bandera: `-32.947368, -60.630589`

## ✨ Ventajas vs Google Maps

- ✅ **Gratuito** (sin costos de API)
- ✅ **Sin límites de requests** (en servidor público)
- ✅ **Más rápido** (50-200ms típico)
- ✅ **Open Source** (datos de OpenStreetMap)
- ✅ **Self-hosteable** (puedes montar tu propio servidor)
- ⚠️ **Sin tráfico en tiempo real** (solo datos de red vial)

## 📚 Documentación Completa

Ver: `ms-rutas-transportistas/README-OSRM.md` para:
- Ejemplos detallados con PowerShell
- Visualización de geometría en mapas
- Mejores prácticas y optimizaciones
- Comparativa con Google Maps

## ✅ Estado de Compilación

```
[INFO] BUILD SUCCESS
[INFO] Total time: 10.974 s
```

Todos los archivos compilados correctamente sin errores.
