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
            @RequestParam Double origenLon,
            @RequestParam Double destinoLat,
            @RequestParam Double destinoLon
    ) {
        CoordenadaDTO origen = new CoordenadaDTO(origenLat, origenLon);
        CoordenadaDTO destino = new CoordenadaDTO(destinoLat, destinoLon);

        RutaCalculadaDTO resultado = osrmService.calcularRuta(origen, destino);

        if (resultado.isExitoso()) {
            return ResponseEntity.ok(resultado);
        } else {
            return ResponseEntity.badRequest().body(resultado);
        }
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
