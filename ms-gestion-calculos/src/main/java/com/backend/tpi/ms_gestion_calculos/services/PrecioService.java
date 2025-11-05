package com.backend.tpi.ms_gestion_calculos.services;

import com.backend.tpi.ms_gestion_calculos.dtos.CostoRequestDTO;
import com.backend.tpi.ms_gestion_calculos.dtos.CostoResponseDTO;
import org.springframework.stereotype.Service;

@Service
public class PrecioService {

    public CostoResponseDTO calcularCostoEstimado(CostoRequestDTO request) {
        // Lógica de negocio para calcular costos y tiempos
        // Placeholder
        return new CostoResponseDTO();
    }

    /**
     * Calculate cost for a given solicitud id. Integration point: will call repository or other services.
     * Currently a stub.
     */
    public CostoResponseDTO calcularCostoParaSolicitud(Long solicitudId) {
        throw new UnsupportedOperationException("calcularCostoParaSolicitud not implemented yet");
    }
}
