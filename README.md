# TPI Backend Logística

Este es el backend para la aplicación de logística.

## Estructura del Proyecto

El proyecto está organizado en una arquitectura de microservicios con un API Gateway.

- `api-gateway`: Punto de entrada único para todas las solicitudes de los clientes. Se encarga del enrutamiento y la seguridad.
- `ms-solicitudes`: Microservicio para gestionar las solicitudes de transporte de contenedores.
- `ms-rutas-transportistas`: Microservicio para gestionar las rutas, camiones y transportistas. **Incluye integración con OSRM** para cálculo de distancias y tiempos.
- `ms-gestion-calculos`: Microservicio para calcular precios y tiempos estimados.
- `docker`: Contiene la configuración de Docker Compose para levantar todo el entorno.

## Características Principales

### 🚀 **NUEVO: Sistema de Rutas Tentativas**
El sistema ahora soporta el cálculo de **rutas tentativas** considerando depósitos intermedios:
- ✅ Cálculo de distancias reales entre depósitos usando OSRM
- ✅ Rutas con múltiples depósitos intermedios
- ✅ Optimización de tramos con métricas detalladas
- ✅ Consulta de coordenadas de depósitos
- 📖 Ver [Documentación de Rutas Tentativas](docs/RUTAS-TENTATIVAS.md)

### 🗺️ Integración OSRM
El microservicio `ms-rutas-transportistas` ahora incluye integración con **OSRM (Open Source Routing Machine)** para:
- Cálculo de distancias precisas entre coordenadas
- Estimación de tiempos de viaje
- Rutas con múltiples waypoints
- Sin costos de API (usa servidor público o self-hosted)

### 📚 Documentación API con Swagger
Todos los microservicios incluyen **Swagger UI** (SpringDoc OpenAPI) para explorar y probar los endpoints:

| Microservicio | Swagger UI | API Docs JSON |
|---------------|------------|---------------|
| **MS-Solicitudes** | http://localhost:8083/swagger-ui.html | http://localhost:8083/v3/api-docs |
| **MS-Rutas** | http://localhost:8082/swagger-ui.html | http://localhost:8082/v3/api-docs |
| **MS-Cálculos** | http://localhost:8081/swagger-ui.html | http://localhost:8081/v3/api-docs |

**Nota:** Swagger requiere autenticación JWT. Obtén un token desde Keycloak y usa el botón "Authorize" (🔓) en Swagger UI.

## Cómo empezar

1.  **Levantar el entorno:**
    ```bash
    cd docker
    docker-compose up -d
    ```

2.  **Ejecutar los microservicios:**
    Cada microservicio es una aplicación Spring Boot independiente. Puedes ejecutarlos desde tu IDE o usando Maven:
    ```bash
    mvn spring-boot:run
    ```

3.  **Probar la integración OSRM:**
    ```powershell
    .\scripts\test-osrm.ps1
    ```
