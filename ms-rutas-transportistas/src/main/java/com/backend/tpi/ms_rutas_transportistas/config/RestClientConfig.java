package com.backend.tpi.ms_rutas_transportistas.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Value("${app.calculos.base-url}")
    private String calculosBaseUrl;

    @Bean("calculosClient")
    public RestClient calculosClient() {
        // se configura base URL para poder usar rutas relativas en los servicios
        return RestClient.builder()
                .baseUrl(calculosBaseUrl)
                .build();
    }
}

