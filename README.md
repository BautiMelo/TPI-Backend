# TPI Backend Logística

Este es el backend para la aplicación de logística.

## Estructura del Proyecto

El proyecto está organizado en una arquitectura de microservicios con un API Gateway.

- `api-gateway`: Punto de entrada único para todas las solicitudes de los clientes. Se encarga del enrutamiento y la seguridad.
- `ms-solicitudes`: Microservicio para gestionar las solicitudes de transporte de contenedores.
- `ms-rutas-transportistas`: Microservicio para gestionar las rutas, camiones y transportistas.
- `ms-gestion-calculos`: Microservicio para calcular precios y tiempos estimados.
- `docker`: Contiene la configuración de Docker Compose para levantar todo el entorno.

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
