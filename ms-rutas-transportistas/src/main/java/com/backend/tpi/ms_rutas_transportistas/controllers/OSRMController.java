package com.backend.tpi.ms_rutas_transportistas.controllers;

import com.backend.tpi.ms_rutas_transportistas.dtos.osrm.CoordenadaDTO;
import com.backend.tpi.ms_rutas_transportistas.dtos.osrm.RutaCalculadaDTO;
import com.backend.tpi.ms_rutas_transportistas.services.OSRMService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/osrm")
@Tag(name = "OSRM", description = "Cálculo de rutas y distancias usando OSRM")
@Slf4j
public class OSRMController {

    @Autowired
    private OSRMService osrmService;

    @PostMapping("/ruta")
    @PreAuthorize("hasAnyRole('RESPONSABLE', 'TRANSPORTISTA', 'ADMIN')")
    @Operation(summary = "Calcular ruta entre dos puntos",
            description = "Calcula la distancia y duración de una ruta entre dos coordenadas usando OSRM")
    public ResponseEntity<RutaCalculadaDTO> calcularRuta(@RequestBody RutaRequest request) {
        log.info("Calculando ruta desde ({},{}) hasta ({},{})",
                request.getOrigen().getLatitud(), request.getOrigen().getLongitud(),
                request.getDestino().getLatitud(), request.getDestino().getLongitud());

        RutaCalculadaDTO resultado = osrmService.calcularRuta(
                request.getOrigen(),
                request.getDestino()
        );

        if (resultado.isExitoso()) {
            return ResponseEntity.ok(resultado);
        } else {
            return ResponseEntity.badRequest().body(resultado);
        }
    }

    @PostMapping("/ruta-multiple")
    @PreAuthorize("hasAnyRole('RESPONSABLE', 'TRANSPORTISTA', 'ADMIN')")
    @Operation(summary = "Calcular ruta con múltiples waypoints",
            description = "Calcula una ruta que pasa por múltiples puntos (mínimo 2)")
    public ResponseEntity<RutaCalculadaDTO> calcularRutaMultiple(@RequestBody RutaMultipleRequest request) {
        if (request.getCoordenadas() == null || request.getCoordenadas().size() < 2) {
            return ResponseEntity.badRequest().body(
                    RutaCalculadaDTO.builder()
                            .exitoso(false)
                            .mensaje("Se requieren al menos 2 coordenadas")
                            .build()
            );
        }

        log.info("Calculando ruta múltiple con {} waypoints", request.getCoordenadas().size());

        RutaCalculadaDTO resultado = osrmService.calcularRutaMultiple(
                request.getCoordenadas().toArray(new CoordenadaDTO[0])
        );

        if (resultado.isExitoso()) {
            return ResponseEntity.ok(resultado);
        } else {
            return ResponseEntity.badRequest().body(resultado);
        }
    }

    @GetMapping("/ruta-simple")
    @PreAuthorize("hasAnyRole('RESPONSABLE', 'TRANSPORTISTA', 'ADMIN')")
    @Operation(summary = "Calcular ruta (GET con query params)",
            description = "Alternativa GET para calcular ruta entre dos puntos")
    public ResponseEntity<RutaCalculadaDTO> calcularRutaSimple(
            @RequestParam Double origenLat,
            @RequestParam Double origenLong,
            @RequestParam Double destinoLat,
            @RequestParam Double destinoLong
    ) {
        CoordenadaDTO origen = new CoordenadaDTO(origenLat, origenLong);
        CoordenadaDTO destino = new CoordenadaDTO(destinoLat, destinoLong);

        RutaCalculadaDTO resultado = osrmService.calcularRuta(origen, destino);

        if (resultado.isExitoso()) {
            return ResponseEntity.ok(resultado);
        } else {
            return ResponseEntity.badRequest().body(resultado);
        }
    }

    @GetMapping("/distancia")
    @PreAuthorize("hasAnyRole('CLIENTE','RESPONSABLE','ADMIN','OPERADOR')")
    @Operation(summary = "Calcular distancia y duración entre dos puntos",
            description = "Calcula distancia usando OSRM - Compatible con endpoint legacy /maps/distancia")
    public ResponseEntity<com.backend.tpi.ms_rutas_transportistas.dtos.DistanciaResponseDTO> getDistancia(
            @RequestParam Double origenLat,
            @RequestParam Double origenLong,
            @RequestParam Double destinoLat,
            @RequestParam Double destinoLong) {
        
        CoordenadaDTO origen = new CoordenadaDTO(origenLat, origenLong);
        CoordenadaDTO destino = new CoordenadaDTO(destinoLat, destinoLong);
        
        RutaCalculadaDTO resultado = osrmService.calcularRuta(origen, destino);
        
        // Convertir a DistanciaResponseDTO para compatibilidad
        com.backend.tpi.ms_rutas_transportistas.dtos.DistanciaResponseDTO response = 
            new com.backend.tpi.ms_rutas_transportistas.dtos.DistanciaResponseDTO();
        response.setDistancia(resultado.getDistanciaKm());
        response.setDuracion(resultado.getDuracionMinutos());
        
        return ResponseEntity.ok(response);
    }

    // DTOs internos para los requests
    @lombok.Data
    public static class RutaRequest {
        private CoordenadaDTO origen;
        private CoordenadaDTO destino;
    }

    @lombok.Data
    public static class RutaMultipleRequest {
        private List<CoordenadaDTO> coordenadas;
    }
}
