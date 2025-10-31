package com.backend.tpi.ms_gestion_calculos.controllers;

import com.backend.tpi.ms_gestion_calculos.dtos.CostoRequestDTO;
import com.backend.tpi.ms_gestion_calculos.dtos.CostoResponseDTO;
import com.backend.tpi.ms_gestion_calculos.services.PrecioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/precio")
public class PrecioController {

    @Autowired
    private PrecioService precioService;

    @PostMapping("/estimado")
    public CostoResponseDTO getPrecioEstimado(@RequestBody CostoRequestDTO request) {
        return precioService.calcularCostoEstimado(request);
    }
}
