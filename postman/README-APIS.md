# Guía de Uso - APIs TPI Backend

## 📋 Información General

Este proyecto contiene 3 microservicios principales accesibles a través del API Gateway:

| Componente | Puerto | Descripción |
|------------|--------|-------------|
| **API Gateway** | 8080 | **Punto de entrada único** - Spring Cloud Gateway |
| **ms-solicitudes** | 8083 | Gestión de solicitudes de transporte |
| **ms-gestion-calculos** | 8081 | Cálculo de precios y tarifas |
| **ms-rutas-transportistas** | 8082 | Gestión de rutas y transportistas |
| **Keycloak** | 8089 | Servidor de autenticación OAuth2/OIDC |

> **⚠️ IMPORTANTE**: Todas las peticiones de API deben realizarse a través del **Gateway (puerto 8080)**.  
> Los microservicios individuales (8081-8083) **no deben** accederse directamente en producción.

## 🔑 Autenticación

### Usuarios de Prueba

| Usuario | Password | Roles | Uso |
|---------|----------|-------|-----|
| **tester** | 1234 | CLIENTE, RESPONSABLE, TRANSPORTISTA, OPERADOR | Acceso completo |
| **cliente1** | 1234 | CLIENTE | Solo crear solicitudes |
| **responsable1** | 1234 | RESPONSABLE | Gestión de solicitudes |
| **transportista1** | 1234 | TRANSPORTISTA | Solo rutas y transportes |
| **operador1** | 1234 | OPERADOR | Operaciones internas |

### Obtener Token (Opción 1: PowerShell)
```powershell
$body = @{
    grant_type='password'
    client_id='postman-test'
    client_secret='secret-postman-123'
    username='tester'
    password='1234'
}
$response = Invoke-RestMethod -Uri "http://localhost:8089/realms/tpi-backend/protocol/openid-connect/token" -Method Post -Body $body
$token = $response.access_token
$refreshToken = $response.refresh_token
Write-Host "Access Token: $token"
Write-Host "Refresh Token: $refreshToken"
Write-Host "Expira en: $($response.expires_in) segundos"
```

### Renovar Token (cuando el access_token expire)
```powershell
# Usar el refresh_token para obtener un nuevo access_token
$body = @{
    grant_type='refresh_token'
    client_id='postman-test'
    client_secret='secret-postman-123'
    refresh_token=$refreshToken
}
$response = Invoke-RestMethod -Uri "http://localhost:8089/realms/tpi-backend/protocol/openid-connect/token" -Method Post -Body $body
$token = $response.access_token
$refreshToken = $response.refresh_token
Write-Host "Nuevo Access Token: $token"
```

### Obtener Token (Opción 2: cURL)
```bash
curl -X POST http://localhost:8089/realms/tpi-backend/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=postman-test" \
  -d "client_secret=secret-postman-123" \
  -d "username=tester" \
  -d "password=1234"
```

## 📦 Importar Colección en Postman

### 1. Importar Colección
1. **Abrir Postman**
2. **Import** → Seleccionar `postman/TPI-Backend-APIs.postman_collection.json`

### 2. Importar Entorno (Environment)
Postman permite separar las variables de configuración en **Environments**, lo cual es útil para cambiar entre entornos (local, staging, producción) fácilmente.

1. **Import** → Seleccionar uno de estos archivos:
   - `postman/TPI-Backend.postman_environment.json` (Local - por defecto)
   - `postman/TPI-Backend.postman_environment.staging.json` (Staging - ejemplo)
   
2. **Seleccionar el entorno** en el dropdown superior derecho de Postman (ej: "TPI-Backend Local")

3. **Verificar variables** (click en el ícono de ojo 👁️):
   - `keycloak_url`: http://localhost:8089
   - `gateway_url`: http://localhost:8080
   - `solicitudes_url`: {{gateway_url}}/api/v1/solicitudes
   - `calculos_url`: {{gateway_url}}/api/v1
   - `rutas_url`: {{gateway_url}}/api/v1/rutas
   - `username`: tester (puedes cambiarlo por cliente1, responsable1, etc.)
   - `password`: 1234

> **💡 Tip**: Puedes crear múltiples entornos para diferentes configuraciones (local, staging, producción) y cambiar entre ellos fácilmente sin modificar la colección.

### 3. Obtener Token (Automático o Manual):
   
   **Opción A - Automático** (Recomendado):
   - La colección tiene un script prerequest que **automáticamente** obtiene el token la primera vez
   - Simplemente ejecuta cualquier request y el token se obtendrá automáticamente
   - El token se guarda en `{{access_token}}` y se usa en todos los requests
   
   **Opción B - Manual**:
   - Ir a la carpeta `0. Authentication`
   - Ejecutar `Obtener Token (Password Grant)`
   - El token se guardará automáticamente en `{{access_token}}`
   - El refresh token se guardará en `{{refresh_token}}`

5. **Renovar Token** (cuando expire):
   - Ejecutar `Renovar Token (Refresh Token)`
   - Obtendrás un nuevo `access_token` sin volver a pedir credenciales
   - Si el `refresh_token` también expiró, vuelve a ejecutar "Obtener Token"

> 💡 **Tip**: Todos los requests usan el Gateway (puerto 8080). Las variables `solicitudes_url`, `calculos_url` y `rutas_url` apuntan al Gateway para routing centralizado.

## 🔐 Autorización por Roles

Las APIs están protegidas por **roles de Keycloak**. Cada endpoint requiere roles específicos:

| Rol | Permisos |
|-----|----------|
| **CLIENTE** | Crear solicitudes propias, ver sus propias solicitudes |
| **RESPONSABLE** | Listar todas las solicitudes, actualizar/eliminar, solicitar rutas |
| **TRANSPORTISTA** | Gestionar rutas, camiones, tramos |
| **OPERADOR** | Operaciones de cálculo de tarifas y precios |

### Ejemplos de Restricciones

- ✅ **CLIENTE** puede crear una solicitud (`POST /solicitudes`)
- ❌ **CLIENTE** NO puede listar todas las solicitudes (`GET /solicitudes` → 403 Forbidden)
- ✅ **RESPONSABLE** puede listar todas las solicitudes
- ✅ **RESPONSABLE** puede actualizar/eliminar solicitudes

> **💡 Tip**: El usuario `tester` tiene todos los roles. Usa `cliente1`, `responsable1`, etc. para probar restricciones de roles.

## 🚀 APIs Disponibles por Microservicio

> **📋 Nota**: Todos los endpoints se acceden a través del **Gateway en puerto 8080**.  
> Ejemplo: `http://localhost:8080/api/v1/solicitudes` (no `http://localhost:8083/...`)

### 1️⃣ MS Solicitudes

#### Crear Solicitud
```http
POST http://localhost:8080/api/v1/solicitudes
Authorization: Bearer {{token}}
Content-Type: application/json

{
    "direccionOrigen": "Av. Corrientes 1234, CABA",
    "direccionDestino": "Av. Santa Fe 5678, CABA"
}
```
**Rol requerido**: CLIENTE

#### Listar Todas las Solicitudes
```http
GET http://localhost:8080/api/v1/solicitudes
Authorization: Bearer {{token}}
```
**Rol requerido**: RESPONSABLE o ADMIN

#### Obtener Solicitud por ID
```http
GET http://localhost:8080/api/v1/solicitudes/{id}
Authorization: Bearer {{token}}
```
**Rol requerido**: CLIENTE, RESPONSABLE o ADMIN

#### Actualizar Solicitud
```http
PUT http://localhost:8080/api/v1/solicitudes/{id}
Authorization: Bearer {{token}}
Content-Type: application/json

{
    "direccionOrigen": "Nueva dirección origen",
    "direccionDestino": "Nueva dirección destino"
}
```
**Rol requerido**: RESPONSABLE o ADMIN

#### Eliminar Solicitud
```http
DELETE http://localhost:8080/api/v1/solicitudes/{id}
Authorization: Bearer {{token}}
```
**Rol requerido**: RESPONSABLE o ADMIN

#### Solicitar Ruta (Integración con MS Rutas)
```http
POST http://localhost:8080/api/v1/solicitudes/{id}/request-route
Authorization: Bearer {{token}}
```
**Rol requerido**: RESPONSABLE o ADMIN

#### Calcular Precio (Integración con MS Cálculos)
```http
POST http://localhost:8080/api/v1/solicitudes/{id}/calculate-price
Authorization: Bearer {{token}}
```
**Rol requerido**: RESPONSABLE o ADMIN

#### Asignar Transporte
```http
POST http://localhost:8080/api/v1/solicitudes/{id}/assign-transport?transportistaId=1
Authorization: Bearer {{token}}
```
**Rol requerido**: RESPONSABLE o ADMIN

---

### 2️⃣ MS Gestión Cálculos

#### Calcular Costo de Solicitud
```http
POST http://localhost:8080/api/v1/precio/solicitud/{solicitudId}/costo
Authorization: Bearer {{token}}
```

#### Listar Tarifas
```http
GET http://localhost:8080/api/v1/tarifas
Authorization: Bearer {{token}}
```

#### Crear Tarifa
```http
POST http://localhost:8080/api/v1/tarifas
Authorization: Bearer {{token}}
Content-Type: application/json

{
    "nombre": "Tarifa Express",
    "precioPorKm": 150.50,
    "activa": true
}
```

#### Listar Depósitos
```http
GET http://localhost:8080/api/v1/depositos
Authorization: Bearer {{token}}
```

---

### 3️⃣ MS Rutas Transportistas

#### Crear Ruta para Solicitud
```http
POST http://localhost:8080/api/v1/rutas
Authorization: Bearer {{token}}
Content-Type: application/json

{
    "idSolicitud": 1
}
```

#### Listar Rutas
```http
GET http://localhost:8080/api/v1/rutas
Authorization: Bearer {{token}}
```

#### Listar Camiones
```http
GET http://localhost:8080/api/v1/camiones
Authorization: Bearer {{token}}
```

#### Listar Tramos
```http
GET http://localhost:8080/api/v1/tramos
Authorization: Bearer {{token}}
```

#### 🗺️ Calcular Ruta con OSRM (Nuevo)
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
    "latitud": -34.608147,
    "longitud": -58.370226
  }
}
```
**Respuesta**:
```json
{
  "distanciaKm": 1.23,
  "duracionHoras": 0.05,
  "duracionMinutos": 3.2,
  "geometry": "encoded_polyline",
  "resumen": "Av. Corrientes",
  "exitoso": true,
  "mensaje": "Ruta calculada exitosamente"
}
```

#### 🗺️ Calcular Ruta Múltiple con OSRM
```http
POST http://localhost:8080/api/v1/osrm/ruta-multiple
Authorization: Bearer {{token}}
Content-Type: application/json

{
  "coordenadas": [
    { "latitud": -34.603722, "longitud": -58.381592 },
    { "latitud": -34.608147, "longitud": -58.370226 },
    { "latitud": -34.611667, "longitud": -58.361944 }
  ]
}
```

#### 🗺️ Obtener Solo Distancia (GET - OSRM)
```http
GET http://localhost:8080/api/v1/maps/distancia-osrm?origenLat=-34.603722&origenLon=-58.381592&destinoLat=-34.608147&destinoLon=-58.370226
Authorization: Bearer {{token}}
```

**Coordenadas de ejemplo (Buenos Aires)**:
- Obelisco: `-34.603722, -58.381592`
- Casa Rosada: `-34.608147, -58.370226`
- Puerto Madero: `-34.611667, -58.361944`
- Palermo: `-34.588889, -58.421944`

---
```http
GET http://localhost:8080/api/v1/camiones
Authorization: Bearer {{token}}
```

#### Listar Tramos
```http
GET http://localhost:8080/api/v1/tramos
Authorization: Bearer {{token}}
```

---

## 🧪 Pruebas Automáticas

### Smoke Test con PowerShell

Para verificar rápidamente que todo funciona, ejecuta:

```powershell
# Desde la raíz del proyecto
.\scripts\smoke-test.ps1
```

#### ¿Qué prueba el Smoke Test?

El smoke test **completo** incluye 15+ pruebas:

1. **Autenticación**: Obtiene tokens para diferentes usuarios (tester, cliente1, responsable1)
2. **MS Solicitudes - CRUD**:
   - Listar solicitudes (GET)
   - Crear solicitud (POST)
   - Obtener solicitud por ID (GET)
   - Actualizar solicitud (PUT)
3. **MS Gestión Cálculos**:
   - Listar tarifas (GET)
   - Crear tarifa (POST)
   - Listar depósitos (GET)
4. **MS Rutas Transportistas**:
   - Listar rutas (GET)
   - Listar camiones (GET)
   - Listar tramos (GET)
5. **Autorización - Roles**:
   - CLIENTE: crear solicitud (debe pasar), listar todas (debe fallar 403)
   - RESPONSABLE: listar todas (debe pasar), actualizar (debe pasar)

**Resultado esperado:**
```
========================================
TPI Backend - Smoke Tests
========================================

[1] AUTHENTICATION TESTS
Getting tokens for different users...
✓ Token obtained for 'tester' (all roles)

[2] MS SOLICITUDES - CRUD OPERATIONS

---- List all solicitudes
     GET http://localhost:8080/api/v1/solicitudes
     ✓ Status: 200
     Response: []

---- Create solicitud
     POST http://localhost:8080/api/v1/solicitudes
     ✓ Status: 200
     Response: {"id":1,...}

[...]

[6] AUTHORIZATION TESTS (Role-based Access)

---- CLIENTE: Create solicitud (should succeed)
     POST http://localhost:8080/api/v1/solicitudes
     ✓ Status: 200

---- CLIENTE: List all solicitudes (should fail 403)
     GET http://localhost:8080/api/v1/solicitudes
     ✓ Status: 403

========================================
TEST SUMMARY
========================================
Total Tests:  15
Passed:       15
Failed:       0
Success Rate: 100%

✓ ALL SMOKE TESTS PASSED
```

> **📝 Nota**: Algunos tests de integración pueden fallar si Google Maps API no está configurada. El script considera exitosos los tests si al menos el 80% pasan.

---

## 🧪 Pruebas Recomendadas (Manuales)

### 1. Flujo Completo: Crear y Procesar Solicitud

1. **Obtener Token** (Postman: `0. Authentication` → `Obtener Token`)
   ```json
   Respuesta:
   {
     "access_token": "eyJhbG...",  // ← Válido 5 minutos
     "refresh_token": "eyJhbG...", // ← Válido 30 minutos
     "expires_in": 300
   }
   ```

2. **Crear Solicitud** (POST `/api/v1/solicitudes`)
3. **Ver Solicitud Creada** (GET `/api/v1/solicitudes/1`)
4. **Calcular Precio** (POST `/api/v1/solicitudes/1/calculate-price`)
5. **Solicitar Ruta** (POST `/api/v1/solicitudes/1/request-route`)
6. **Asignar Transporte** (POST `/api/v1/solicitudes/1/assign-transport?transportistaId=1`)

**Si el token expira durante el flujo:**
- Ejecutar `Renovar Token (Refresh Token)` en lugar de volver a pedir credenciales
- El nuevo access_token se guardará automáticamente

### 2. Verificar Roles

El usuario `tester` tiene todos los roles, por lo que puede acceder a todos los endpoints. Para probar restricciones de roles, deberías:
- Crear nuevos usuarios en Keycloak con roles específicos
- O modificar los roles del usuario actual

### 3. Verificar Integración entre Microservicios

- **Solicitudes → Cálculos**: `POST /api/v1/solicitudes/{id}/calculate-price`
- **Solicitudes → Rutas**: `POST /api/v1/solicitudes/{id}/request-route`

---

## 🔧 Troubleshooting

### Error 401 Unauthorized
- Verificar que el token no haya expirado (duración por defecto: 5 minutos)
- **Opción 1**: Ejecutar `Renovar Token (Refresh Token)` para obtener un nuevo token sin volver a pedir credenciales
- **Opción 2**: Volver a ejecutar `Obtener Token` si el refresh token también expiró
- Verificar que el header `Authorization: Bearer {token}` esté presente

### Error 403 Forbidden
- El usuario no tiene el rol requerido para ese endpoint
- Verificar roles del usuario: el token debe contener los roles en `realm_access.roles`

### Keycloak se queda en el logo
- Limpiar cache del navegador (Ctrl + Shift + R)
- Acceder directamente a: `http://localhost:8089/admin`
- Usuario admin: `admin` / `admin123`
- Si sigue sin funcionar, reiniciar contenedor:
  ```bash
  docker-compose restart keycloak
  ```

### Puerto ocupado
- Verificar que no haya otros servicios corriendo en los puertos 8080-8089
- Ver qué puertos están en uso: `netstat -ano | findstr :808`

---

## 📊 Verificar Estado de Servicios

```powershell
# Ver todos los contenedores
docker ps

# Ver logs de un servicio específico
docker logs ms-solicitudes --tail 50
docker logs ms-gestion-calculos --tail 50
docker logs ms-rutas-transportistas --tail 50
docker logs keycloak --tail 50

# Verificar conectividad a PostgreSQL
docker exec postgres psql -U postgres -d tpi_backend_db -c "\dt"
```

---

## 🌐 Acceso a Keycloak Admin Console

- URL: `http://localhost:8089/admin`
- Usuario: `admin`
- Password: `admin123`

### Realm: tpi-backend
- **Clientes**: `postman-test`
- **Usuarios disponibles**: 
  - `tester` (todos los roles) 
  - `cliente1` (solo CLIENTE)
  - `responsable1` (solo RESPONSABLE)
  - `transportista1` (solo TRANSPORTISTA)
  - `operador1` (solo OPERADOR)
- **Todos los passwords**: `1234`
- **Roles disponibles**: `CLIENTE`, `RESPONSABLE`, `TRANSPORTISTA`, `OPERADOR`

### 👤 Crear nuevos usuarios
1. Ir a **Users** → **Create new user**
2. Completar: Username, Email, First name, Last name
3. Marcar **Email verified**: ON
4. Click **Create**
5. Tab **Credentials** → **Set password** → Password: `1234` → **Temporary**: OFF
6. Tab **Role mapping** → **Assign role** → Seleccionar roles → **Assign**

### ⏱️ Configuración de Tokens
Los tiempos de expiración están configurados en `keycloak/realm-tpi-backend.json`:
- **Access Token**: 300 segundos (5 minutos)
- **Refresh Token**: 1800 segundos (30 minutos) 
- **SSO Session Max**: 36000 segundos (10 horas)

Para cambiar estos valores:
1. Editar `keycloak/realm-tpi-backend.json`
2. O desde Admin Console: Realm Settings → Tokens
3. Reiniciar Keycloak: `docker-compose restart keycloak`

---

## 📝 Notas Importantes

1. **Todos los requests requieren autenticación** con JWT token de Keycloak
2. **Los roles se validan en cada endpoint** mediante `@PreAuthorize`
3. **CSRF está deshabilitado** para facilitar pruebas con Postman (válido para APIs REST stateless)
4. **El access token expira en 5 minutos** por defecto
5. **El refresh token expira en 30 minutos** - úsalo para renovar el access token
6. **Los microservicios se comunican entre sí** a través de RestClient
7. **Token Relay**: El Gateway pasa el token original del usuario a los microservicios

---

## 🎯 Endpoints de Health Check

```http
GET http://localhost:8080/actuator/health  # Gateway
GET http://localhost:8083/actuator/health  # ms-solicitudes (directo)
GET http://localhost:8081/actuator/health  # ms-gestion-calculos (directo)
GET http://localhost:8082/actuator/health  # ms-rutas-transportistas (directo)
```

> **Nota**: Los health checks de los microservicios individuales (8081-8083) son solo para monitoreo interno.  
> En producción, usa el health del Gateway (8080).

Si estos endpoints no están disponibles, agregar la dependencia de Actuator en los `pom.xml`:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

---

## ✅ Verificación Rápida (Quick Start)

1. **Iniciar servicios**:
   ```powershell
   docker-compose up -d
   ```

2. **Esperar ~30 segundos** a que todos los servicios estén listos

3. **Ejecutar smoke test**:
   ```powershell
   .\scripts\smoke-test.ps1
   ```

4. **Si todo pasa**, importa la colección de Postman y empieza a probar:
   - `postman/TPI-Backend-APIs.postman_collection.json`
   - El token se obtendrá automáticamente
   - Todas las URLs ya apuntan al Gateway (puerto 8080)

5. **Ver logs** si algo falla:
   ```powershell
   docker-compose logs --tail=100 gateway
   docker-compose logs --tail=100 keycloak
   docker-compose logs --tail=100 ms-solicitudes
   ```
