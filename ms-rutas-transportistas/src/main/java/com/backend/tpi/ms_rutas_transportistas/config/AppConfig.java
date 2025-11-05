package com.backend.tpi.ms_rutas_transportistas.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {
    // ModelMapper bean removed; manual mapping is used.

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
