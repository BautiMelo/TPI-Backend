package com.backend.tpi.ms_rutas_transportistas.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Value("${google.maps.api.url}")
    private String googleMapsApiUrl;

    @Bean("googleMapsRestClient")
    public RestClient googleMapsRestClient() {
        return RestClient.builder()
                .baseUrl(googleMapsApiUrl)
                .build();
    }
}
