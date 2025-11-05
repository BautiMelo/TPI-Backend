package com.backend.tpi.ms_gestion_calculos.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestMicroservicioConfig {

    // Usamos RestClient (estilo del ejemplo RestClientConfig) para un único bean cliente
    @Bean
    public RestClient rutasClient(@Value("${app.rutas.base-url:http://localhost:8082}") String baseUrl) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

}
