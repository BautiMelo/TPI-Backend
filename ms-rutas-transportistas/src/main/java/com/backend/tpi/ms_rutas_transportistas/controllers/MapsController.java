package com.backend.tpi.ms_rutas_transportistas.controllers;

import com.backend.tpi.ms_rutas_transportistas.dtos.DistanciaResponseDTO;
import com.backend.tpi.ms_rutas_transportistas.dtos.osrm.CoordenadaDTO;
import com.backend.tpi.ms_rutas_transportistas.services.MapsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/maps")
@Tag(name = "Maps", description = "Servicios de mapas y distancias")
public class MapsController {

    @Autowired
    private MapsService mapsService;

    @GetMapping("/distancia")
    @PreAuthorize("hasAnyRole('CLIENTE','RESPONSABLE','ADMIN')")
    @Operation(summary = "Calcular distancia (Google Maps - legacy)",
            description = "Calcula distancia usando el microservicio de cálculos (Google Maps)")
    public DistanciaResponseDTO getDistancia(@RequestParam String origen, @RequestParam String destino) {
        return mapsService.getDistancia(origen, destino);
    }

    @GetMapping("/distancia-osrm")
    @PreAuthorize("hasAnyRole('CLIENTE','RESPONSABLE','ADMIN','TRANSPORTISTA')")
    @Operation(summary = "Calcular distancia con OSRM",
            description = "Calcula distancia y duración usando OSRM (más rápido)")
    public DistanciaResponseDTO getDistanciaOSRM(
            @RequestParam Double origenLat,
            @RequestParam Double origenLon,
            @RequestParam Double destinoLat,
            @RequestParam Double destinoLon
    ) {
        CoordenadaDTO origen = new CoordenadaDTO(origenLat, origenLon);
        CoordenadaDTO destino = new CoordenadaDTO(destinoLat, destinoLon);
        return mapsService.getDistanciaConOSRM(origen, destino);
    }
}
