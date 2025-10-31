package com.backend.tpi.ms_gestion_calculos.controllers;

import com.backend.tpi.ms_gestion_calculos.dtos.DistanciaRequestDTO;
import com.backend.tpi.ms_gestion_calculos.dtos.DistanciaResponseDTO;
import com.backend.tpi.ms_gestion_calculos.services.CalculoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/calculos")
public class CalculoController {

    @Autowired
    private CalculoService calculoService;

    @PostMapping("/distancia")
    public ResponseEntity<DistanciaResponseDTO> calcularDistancia(@RequestBody DistanciaRequestDTO request) {
        return ResponseEntity.ok(calculoService.calcularDistancia(request));
    }
}
