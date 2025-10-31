package com.backend.tpi.ms_rutas_transportistas.services;

import com.backend.tpi.ms_rutas_transportistas.dtos.DistanciaResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class MapsService {

    @Autowired
    @Qualifier("googleMapsRestClient")
    private RestClient restClient;

    @Value("${google.maps.apikey}")
    private String apiKey;

    public DistanciaResponseDTO getDistancia(String origen, String destino) {
        // Lógica para llamar a la API de Google Maps y obtener la distancia
        // Esto es un placeholder
        return new DistanciaResponseDTO();
    }
}
