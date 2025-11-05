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
				.route(spec -> spec.path("/api/v1/gestion/**")
						.filters(f -> f.rewritePath("/api/v1/gestion/(?<segment>.*)", "/${segment}"))
						.uri("http://ms-gestion-calculos:8081"))
				.route(spec -> spec.path("/api/v1/rutas/**")
						.filters(f -> f.rewritePath("/api/v1/rutas/(?<segment>.*)", "/${segment}"))
						.uri("http://ms-rutas-transportistas:8082"))
				.route(spec -> spec.path("/api/v1/solicitudes/**")
						.filters(f -> f.rewritePath("/api/v1/solicitudes/(?<segment>.*)", "/${segment}"))
						.uri("http://ms-solicitudes:8083"))
				.build();
	}
}
