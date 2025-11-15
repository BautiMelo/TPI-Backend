package com.backend.tpi.ms_gestion_calculos.controllers;

import com.backend.tpi.ms_gestion_calculos.dtos.CostoRequestDTO;
import com.backend.tpi.ms_gestion_calculos.dtos.CostoResponseDTO;
import com.backend.tpi.ms_gestion_calculos.services.PrecioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/precio")
public class PrecioController {

    @Autowired
    private PrecioService precioService;

    @PostMapping("/estimado")
    @PreAuthorize("hasAnyRole('CLIENTE','RESPONSABLE')")
    public CostoResponseDTO getPrecioEstimado(@RequestBody CostoRequestDTO request) {
        return precioService.calcularCostoEstimado(request);
    }

    @PostMapping("/traslado")
    @PreAuthorize("hasAnyRole('ADMIN','OPERADOR')")
    public CostoResponseDTO getCostoTraslado(@RequestBody CostoRequestDTO request) {
        // Calcular el costo real de un traslado
        return precioService.calcularCostoTraslado(request);
    }

    @PostMapping("/solicitud/{id}/costo")
    @PreAuthorize("hasAnyRole('RESPONSABLE','ADMIN')")
    public CostoResponseDTO getCostoPorSolicitud(@PathVariable Long id) {
        // Delegar al servicio que calculará el costo para la solicitud (stub por ahora)
        return precioService.calcularCostoParaSolicitud(id);
    }
}
