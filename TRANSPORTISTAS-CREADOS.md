# Transportistas Creados

## Resumen

Se han creado exitosamente 3 usuarios transportistas en el sistema con datos realistas.

## Usuarios Creados

### 1. Carlos Ramírez
- **Username:** `carlos.ramirez`
- **Password:** `1234`
- **Email:** carlos.ramirez@transporte.com
- **Teléfono:** +5491165432109
- **Rol:** TRANSPORTISTA
- **Keycloak User ID:** 51a9da87-aa44-4efb-b75e-3d44b5dca852

### 2. Laura Fernández
- **Username:** `laura.fernandez`
- **Password:** `1234`
- **Email:** laura.fernandez@logistica.com
- **Teléfono:** +5491187654321
- **Rol:** TRANSPORTISTA
- **Keycloak User ID:** a716f85b-7317-47ee-8bd9-17816be4e1aa

### 3. Diego Martínez
- **Username:** `diego.martinez`
- **Password:** `1234`
- **Email:** diego.martinez@fleteargentino.com
- **Teléfono:** +5491198765432
- **Rol:** TRANSPORTISTA
- **Keycloak User ID:** 96affe6d-aa63-4a55-96f8-2b201a7546ce

## Autenticación

Cada transportista puede obtener su token de acceso usando el endpoint de Keycloak:

**Endpoint:** `POST http://localhost:8089/realms/tpi-backend/protocol/openid-connect/token`

**Body (form-data o JSON):**
```json
{
  "grant_type": "password",
  "client_id": "tpi-backend-client",
  "username": "carlos.ramirez",
  "password": "1234"
}
```

## Postman Collection

Se han agregado 3 nuevos requests en la carpeta **"Autenticación"** de la colección `TPI-Backend-General.postman_collection.json`:

1. **Get Token - Transportista Carlos**
2. **Get Token - Transportista Laura**
3. **Get Token - Transportista Diego**

Estos requests están listos para usar y obtener los tokens JWT de cada transportista.

## Permisos

Los transportistas tienen acceso a los siguientes endpoints (según su rol TRANSPORTISTA):

- Ver camiones asignados
- Ver rutas asignadas
- Actualizar estado de entregas
- Ver información de solicitudes asignadas

## Notas Técnicas

### Endpoint de Creación Usado

Los usuarios fueron creados usando el endpoint de administración:

**Endpoint:** `POST http://localhost:8080/api/v1/usuarios/registro`

**Requiere:** Token de ADMIN

**Body:**
```json
{
  "nombre": "Carlos Ramirez",
  "email": "carlos.ramirez@transporte.com",
  "telefono": "+5491165432109",
  "username": "carlos.ramirez",
  "password": "1234",
  "rol": "TRANSPORTISTA"
}
```

### Configuración del Gateway

Se agregó la siguiente ruta al API Gateway (`RouteConfig.java`):

```java
.route("ms-usuarios", spec -> spec.path("/api/v1/usuarios/**")
        .uri("http://ms-solicitudes:8083"))
```

### Servicios Reconstruidos

Para que el endpoint funcionara correctamente, se reconstruyeron los siguientes contenedores:

1. **ms-solicitudes** - Contiene el nuevo endpoint de registro de usuarios
2. **gateway** - Actualizado con la nueva ruta hacia `/api/v1/usuarios/**`

## Próximos Pasos

Los transportistas ya están listos para:

1. Obtener sus tokens usando Postman
2. Acceder a los endpoints autorizados para su rol
3. Ser asignados a camiones y rutas en el sistema
4. Gestionar entregas y solicitudes

## Validación

Para verificar que un transportista puede autenticarse, usar cualquiera de los 3 requests de "Get Token - Transportista" en Postman. El response incluirá:

- `access_token`: Token JWT para usar en las peticiones
- `expires_in`: Tiempo de expiración (en segundos)
- `refresh_token`: Token para renovar el acceso
- `token_type`: "Bearer"

---

**Fecha de creación:** 20 de noviembre de 2024  
**Creado mediante:** Endpoint `/api/v1/usuarios/registro` (ADMIN)
