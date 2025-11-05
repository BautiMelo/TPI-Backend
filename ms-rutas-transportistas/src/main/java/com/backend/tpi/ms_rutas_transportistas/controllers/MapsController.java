package com.backend.tpi.ms_rutas_transportistas.controllers;

import com.backend.tpi.ms_rutas_transportistas.dtos.DistanciaResponseDTO;
import com.backend.tpi.ms_rutas_transportistas.services.MapsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/maps")
public class MapsController {

    @Autowired
    private MapsService mapsService;

    @GetMapping("/distancia")
    @PreAuthorize("hasAnyRole('CLIENTE','RESPONSABLE','ADMIN')")
    public DistanciaResponseDTO getDistancia(@RequestParam String origen, @RequestParam String destino) {
        return mapsService.getDistancia(origen, destino);
    }
}
