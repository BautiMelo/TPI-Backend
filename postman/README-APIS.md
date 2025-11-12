# Guía de Uso - APIs TPI Backend

## 📋 Información General

Este proyecto contiene 3 microservicios principales:

| Microservicio | Puerto | Descripción |
|--------------|--------|-------------|
| **ms-solicitudes** | 8083 | Gestión de solicitudes de transporte |
| **ms-gestion-calculos** | 8081 | Cálculo de precios y tarifas |
| **ms-rutas-transportistas** | 8082 | Gestión de rutas y transportistas |
| **Keycloak** | 8089 | Servidor de autenticación OAuth2/OIDC |
| **API Gateway** | 8080 | Gateway (Spring Cloud Gateway) |

## 🔑 Autenticación

### Usuario de Prueba
```
Username: tester
Password: 1234
Roles: CLIENTE, RESPONSABLE, TRANSPORTISTA, OPERADOR
```

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
Write-Host "Token: $token"
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

1. **Abrir Postman**
2. **Import** → Seleccionar `postman/TPI-Backend-APIs.postman_collection.json`
3. **Configurar variables** (ya están preconfiguradas):
   - `keycloak_url`: http://localhost:8089
   - `solicitudes_url`: http://localhost:8083
   - `calculos_url`: http://localhost:8081
   - `rutas_url`: http://localhost:8082

4. **Obtener Token**:
   - Ir a la carpeta `0. Authentication`
   - Ejecutar `Obtener Token (Password Grant)`
   - El token se guardará automáticamente en `{{access_token}}`
   - Todas las demás requests usarán este token automáticamente

## 🚀 APIs Disponibles por Microservicio

### 1️⃣ MS Solicitudes (Puerto 8083)

#### Crear Solicitud
```http
POST http://localhost:8083/api/v1/solicitudes
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
GET http://localhost:8083/api/v1/solicitudes
Authorization: Bearer {{token}}
```
**Rol requerido**: RESPONSABLE o ADMIN

#### Obtener Solicitud por ID
```http
GET http://localhost:8083/api/v1/solicitudes/{id}
Authorization: Bearer {{token}}
```
**Rol requerido**: CLIENTE, RESPONSABLE o ADMIN

#### Actualizar Solicitud
```http
PUT http://localhost:8083/api/v1/solicitudes/{id}
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
DELETE http://localhost:8083/api/v1/solicitudes/{id}
Authorization: Bearer {{token}}
```
**Rol requerido**: RESPONSABLE o ADMIN

#### Solicitar Ruta (Integración con MS Rutas)
```http
POST http://localhost:8083/api/v1/solicitudes/{id}/request-route
Authorization: Bearer {{token}}
```
**Rol requerido**: RESPONSABLE o ADMIN

#### Calcular Precio (Integración con MS Cálculos)
```http
POST http://localhost:8083/api/v1/solicitudes/{id}/calculate-price
Authorization: Bearer {{token}}
```
**Rol requerido**: RESPONSABLE o ADMIN

#### Asignar Transporte
```http
POST http://localhost:8083/api/v1/solicitudes/{id}/assign-transport?transportistaId=1
Authorization: Bearer {{token}}
```
**Rol requerido**: RESPONSABLE o ADMIN

---

### 2️⃣ MS Gestión Cálculos (Puerto 8081)

#### Calcular Costo de Solicitud
```http
POST http://localhost:8081/api/v1/precio/solicitud/{solicitudId}/costo
Authorization: Bearer {{token}}
```

#### Listar Tarifas
```http
GET http://localhost:8081/api/v1/tarifas
Authorization: Bearer {{token}}
```

#### Crear Tarifa
```http
POST http://localhost:8081/api/v1/tarifas
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
GET http://localhost:8081/api/v1/depositos
Authorization: Bearer {{token}}
```

---

### 3️⃣ MS Rutas Transportistas (Puerto 8082)

#### Crear Ruta para Solicitud
```http
POST http://localhost:8082/api/v1/rutas
Authorization: Bearer {{token}}
Content-Type: application/json

{
    "idSolicitud": 1
}
```

#### Listar Rutas
```http
GET http://localhost:8082/api/v1/rutas
Authorization: Bearer {{token}}
```

#### Listar Camiones
```http
GET http://localhost:8082/api/v1/camiones
Authorization: Bearer {{token}}
```

#### Listar Tramos
```http
GET http://localhost:8082/api/v1/tramos
Authorization: Bearer {{token}}
```

---

## 🧪 Pruebas Recomendadas

### 1. Flujo Completo: Crear y Procesar Solicitud

1. **Obtener Token** (Postman: `0. Authentication` → `Obtener Token`)
2. **Crear Solicitud** (POST `/api/v1/solicitudes`)
3. **Ver Solicitud Creada** (GET `/api/v1/solicitudes/1`)
4. **Calcular Precio** (POST `/api/v1/solicitudes/1/calculate-price`)
5. **Solicitar Ruta** (POST `/api/v1/solicitudes/1/request-route`)
6. **Asignar Transporte** (POST `/api/v1/solicitudes/1/assign-transport?transportistaId=1`)

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
- Verificar que el token no haya expirado (duración: 5 minutos)
- Volver a ejecutar `Obtener Token`
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
- Clientes: `postman-test`
- Usuarios: `tester` (password: `1234`)
- Roles disponibles: `CLIENTE`, `RESPONSABLE`, `TRANSPORTISTA`, `OPERADOR`

---

## 📝 Notas Importantes

1. **Todos los requests requieren autenticación** con JWT token de Keycloak
2. **Los roles se validan en cada endpoint** mediante `@PreAuthorize`
3. **CSRF está deshabilitado** para facilitar pruebas con Postman
4. **El token expira en 5 minutos** - volver a solicitar cuando sea necesario
5. **Los microservicios se comunican entre sí** a través de RestClient

---

## 🎯 Endpoints de Health Check

```http
GET http://localhost:8083/actuator/health
GET http://localhost:8081/actuator/health
GET http://localhost:8082/actuator/health
```

Si estos endpoints no están disponibles, agregar la dependencia de Actuator en los `pom.xml`:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```
