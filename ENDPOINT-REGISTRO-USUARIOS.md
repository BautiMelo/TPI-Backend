# Endpoint de Registro de Usuarios (ADMIN)

## Descripción
Nuevo endpoint agregado al sistema que permite al administrador crear usuarios con diferentes roles en el sistema.

## Ubicación
- **Microservicio:** `ms-solicitudes`
- **Controlador:** `UsuarioController.java`
- **Servicio:** `UsuarioService.java`
- **Path:** `/api/v1/usuarios/registro`

## Detalles del Endpoint

### POST /api/v1/usuarios/registro

**Descripción:** Permite al administrador registrar nuevos usuarios en el sistema con cualquier rol disponible.

**Autorización:** Requiere rol `ADMIN`

**Request Body (UsuarioRegistroDTO):**
```json
{
  "nombre": "Pedro Operador",
  "email": "operador@example.com",
  "telefono": "+54911555666",
  "username": "operador1",
  "password": "1234",
  "rol": "OPERADOR"
}
```

**Roles disponibles:**
- `CLIENTE` - Usuario cliente del sistema (se guarda también en BD local)
- `OPERADOR` - Operador del sistema
- `TRANSPORTISTA` - Transportista
- `ADMIN` - Administrador del sistema

**Validaciones:**
- `nombre`: Obligatorio, entre 2 y 100 caracteres
- `email`: Obligatorio, formato email válido
- `telefono`: Opcional, máximo 20 caracteres
- `username`: Obligatorio, entre 3 y 50 caracteres
- `password`: Obligatorio, mínimo 4 caracteres
- `rol`: Obligatorio, debe ser uno de: CLIENTE, OPERADOR, TRANSPORTISTA, ADMIN

**Response (201 Created):**
```json
{
  "keycloakUserId": "abc123-def456-ghi789",
  "nombre": "Pedro Operador",
  "email": "operador@example.com",
  "telefono": "+54911555666",
  "username": "operador1",
  "rol": "OPERADOR",
  "mensaje": "Usuario registrado exitosamente con rol OPERADOR. Puede iniciar sesión con sus credenciales."
}
```

**Response (400 Bad Request):**
```json
{
  "error": "El username ya está en uso"
}
```

o

```json
{
  "error": "El email ya está registrado"
}
```

## Funcionamiento

1. El administrador envía los datos del nuevo usuario incluyendo el rol
2. El sistema valida que username y email no existan en Keycloak
3. Si el rol es CLIENTE, también valida que el email no exista en la BD local
4. Crea el usuario en Keycloak con el rol especificado
5. Si el rol es CLIENTE, también crea el registro en la tabla `clientes` de la BD local
6. Retorna los datos del usuario creado

## Diferencias con el Endpoint Público de Clientes

### `/api/v1/clientes/registro` (Público)
- **Acceso:** Público, sin autenticación
- **Rol:** Solo puede crear usuarios con rol CLIENTE
- **BD Local:** Siempre guarda en tabla clientes
- **Uso:** Auto-registro de clientes

### `/api/v1/usuarios/registro` (ADMIN)
- **Acceso:** Solo ADMIN autenticado
- **Rol:** Puede crear usuarios con cualquier rol (CLIENTE, OPERADOR, TRANSPORTISTA, ADMIN)
- **BD Local:** Solo guarda en tabla clientes si el rol es CLIENTE
- **Uso:** Gestión administrativa de usuarios del sistema

## Archivos Modificados/Creados

### Nuevos archivos:
1. `ms-solicitudes/src/main/java/com/backend/tpi/ms_solicitudes/dto/UsuarioRegistroDTO.java`
2. `ms-solicitudes/src/main/java/com/backend/tpi/ms_solicitudes/dto/UsuarioRegistroResponseDTO.java`
3. `ms-solicitudes/src/main/java/com/backend/tpi/ms_solicitudes/services/UsuarioService.java`
4. `ms-solicitudes/src/main/java/com/backend/tpi/ms_solicitudes/controllers/UsuarioController.java`

### Archivos modificados:
1. `ms-solicitudes/src/main/java/com/backend/tpi/ms_solicitudes/services/KeycloakService.java`
   - Agregado método `crearUsuarioConRol()` que permite especificar el rol
   - Refactorizado método `crearUsuario()` para usar el nuevo método

2. `postman/TPI-Backend-General.postman_collection.json`
   - Agregada nueva sección "Usuarios (ADMIN)"
   - Agregado endpoint "Registro Usuario por Admin"

## Uso en Postman

1. **Obtener token de ADMIN:**
   - Ejecutar: `Auth > Get Token - admin (ADMIN)`
   - Usuario: `tester`
   - Password: `1234`

2. **Registrar nuevo usuario:**
   - Ejecutar: `Usuarios (ADMIN) > Registro Usuario por Admin`
   - Modificar el body con los datos del usuario a crear
   - Cambiar el campo `rol` según el tipo de usuario deseado

## Ejemplo de Uso

### Crear un Operador:
```bash
POST http://localhost:8080/api/v1/usuarios/registro
Authorization: Bearer {token_admin}
Content-Type: application/json

{
  "nombre": "María Responsable",
  "email": "maria.responsable@example.com",
  "telefono": "+54911777888",
  "username": "maria.resp",
  "password": "securepass123",
  "rol": "OPERADOR"
}
```

### Crear un Transportista:
```bash
POST http://localhost:8080/api/v1/usuarios/registro
Authorization: Bearer {token_admin}
Content-Type: application/json

{
  "nombre": "Carlos Transportista",
  "email": "carlos.transporte@example.com",
  "telefono": "+54911999000",
  "username": "carlos.trans",
  "password": "securepass456",
  "rol": "TRANSPORTISTA"
}
```

### Crear un Cliente (desde admin):
```bash
POST http://localhost:8080/api/v1/usuarios/registro
Authorization: Bearer {token_admin}
Content-Type: application/json

{
  "nombre": "Ana Cliente",
  "email": "ana.cliente@example.com",
  "telefono": "+54911222333",
  "username": "ana.cliente",
  "password": "clientpass789",
  "rol": "CLIENTE"
}
```

## Notas Importantes

- Solo usuarios con rol ADMIN pueden acceder a este endpoint
- El endpoint valida que username y email sean únicos en Keycloak
- Los usuarios creados pueden iniciar sesión inmediatamente con sus credenciales
- Los usuarios con rol CLIENTE se sincronizan automáticamente con la tabla `clientes`
- La contraseña no es temporal, el usuario no necesita cambiarla en el primer login
