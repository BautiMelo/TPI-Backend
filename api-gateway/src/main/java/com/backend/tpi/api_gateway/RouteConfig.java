package com.backend.tpi.api_gateway;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RouteConfig {
	
    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // ms-solicitudes: handles /api/v1/solicitudes/**
                .route("ms-solicitudes", spec -> spec.path("/api/v1/solicitudes/**")
                        .uri("http://ms-solicitudes:8083"))

                // ms-gestion-calculos: handles multiple API groups
                .route("ms-calculos-gestion", spec -> spec.path("/api/v1/gestion/**")
                        .uri("http://ms-gestion-calculos:8081"))
                .route("ms-calculos-tarifas", spec -> spec.path("/api/v1/tarifas/**")
                        .uri("http://ms-gestion-calculos:8081"))
                .route("ms-calculos-tarifa-volumen-peso", spec -> spec.path("/api/v1/tarifa-volumen-peso/**")
                        .uri("http://ms-gestion-calculos:8081"))
                .route("ms-calculos-precio", spec -> spec.path("/api/v1/precio/**")
                        .uri("http://ms-gestion-calculos:8081"))
                .route("ms-calculos-depositos", spec -> spec.path("/api/v1/depositos/**")
                        .uri("http://ms-gestion-calculos:8081"))

                // ms-rutas-transportistas: routes, tramos, maps, camiones, osrm
                .route("ms-rutas-rutas", spec -> spec.path("/api/v1/rutas/**")
                        .uri("http://ms-rutas-transportistas:8082"))
                .route("ms-rutas-tramos", spec -> spec.path("/api/v1/tramos/**")
                        .uri("http://ms-rutas-transportistas:8082"))
                .route("ms-rutas-maps", spec -> spec.path("/api/v1/maps/**")
                        .uri("http://ms-rutas-transportistas:8082"))
                .route("ms-rutas-osrm", spec -> spec.path("/api/v1/osrm/**")
                        .uri("http://ms-rutas-transportistas:8082"))
                .route("ms-rutas-camiones", spec -> spec.path("/api/v1/camiones/**")
                        .uri("http://ms-rutas-transportistas:8082"))

                .build();
    }

}
