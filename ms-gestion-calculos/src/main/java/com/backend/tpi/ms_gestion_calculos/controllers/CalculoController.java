package com.backend.tpi.ms_gestion_calculos.controllers;

import com.backend.tpi.ms_gestion_calculos.dtos.DistanciaRequestDTO;
import com.backend.tpi.ms_gestion_calculos.dtos.DistanciaResponseDTO;
import com.backend.tpi.ms_gestion_calculos.services.CalculoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/gestion")
public class CalculoController {

    private static final Logger logger = LoggerFactory.getLogger(CalculoController.class);

    @Autowired
    private CalculoService calculoService;

    @PostMapping("/distancia")
    @PreAuthorize("hasAnyRole('CLIENTE','RESPONSABLE')")
    public ResponseEntity<DistanciaResponseDTO> calcularDistancia(@RequestBody DistanciaRequestDTO request) {
        logger.info("POST /api/v1/gestion/distancia - Calculando distancia");
        DistanciaResponseDTO result = calculoService.calcularDistancia(request);
        logger.info("POST /api/v1/gestion/distancia - Respuesta: 200 - Distancia: {} km", result.getDistancia());
        return ResponseEntity.ok(result);
    }
}
