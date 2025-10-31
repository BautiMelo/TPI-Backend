package com.backend.tpi.ms_gestion_calculos.services;

import com.backend.tpi.ms_gestion_calculos.dtos.DistanciaRequestDTO;
import com.backend.tpi.ms_gestion_calculos.dtos.DistanciaResponseDTO;
import org.springframework.stereotype.Service;

@Service
public class CalculoService {

    private static final int RADIO_TIERRA = 6371; // Radio de la Tierra en kilómetros

    public DistanciaResponseDTO calcularDistancia(DistanciaRequestDTO request) {
        // Simulación del cálculo de distancia. En un caso real, se utilizaría una API externa.
        double distancia = calcularDistanciaHaversine(request.getOrigen(), request.getDestino());
        return new DistanciaResponseDTO(distancia);
    }

    private double calcularDistanciaHaversine(String origen, String destino) {
        // Coordenadas simuladas
        double lat1 = -34.6037; // Buenos Aires
        double lon1 = -58.3816;
        double lat2 = -32.9445; // Rosario
        double lon2 = -60.6500;

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return RADIO_TIERRA * c;
    }
}
