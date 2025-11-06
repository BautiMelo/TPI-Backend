package com.backend.tpi.ms_rutas_transportistas.services;

import com.backend.tpi.ms_rutas_transportistas.dtos.DistanciaResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.http.ResponseEntity;

@Service
public class MapsService {

    @Autowired
    @Qualifier("calculosClient")
    private RestClient calculosClient;

    public DistanciaResponseDTO getDistancia(String origen, String destino) {
        java.util.Map<String, String> req = new java.util.HashMap<>();
        req.put("origen", origen);
        req.put("destino", destino);
        try {
            ResponseEntity<DistanciaResponseDTO> response = calculosClient.post()
                    .uri("/api/v1/gestion/distancia")
                    .body(req, new org.springframework.core.ParameterizedTypeReference<java.util.Map<String,String>>() {})
                    .retrieve()
                    .toEntity(DistanciaResponseDTO.class);
            DistanciaResponseDTO resp = response != null ? response.getBody() : null;
            return resp != null ? resp : new DistanciaResponseDTO();
        } catch (Exception ex) {
            return new DistanciaResponseDTO();
        }
    }
}
