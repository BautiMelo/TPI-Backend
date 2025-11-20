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

/**
 * Controlador REST para cálculos de rutas con OSRM
 * Expone endpoints para calcular distancias y rutas reales entre coordenadas
 * Utiliza el servicio OSRM que se conecta al servidor OSRM en Docker
 */
@RestController
@RequestMapping("/api/v1/osrm")
@Tag(name = "OSRM", description = "Cálculo de rutas y distancias usando OSRM")
@Slf4j
public class OSRMController {

    @Autowired
    private OSRMService osrmService;

    /**
     * Calcula la ruta óptima entre dos puntos geográficos
     * @param request Coordenadas de origen y destino
     * @return Ruta calculada con distancia, duración y geometría
     */
    @PostMapping("/ruta")
    @PreAuthorize("hasAnyRole('OPERADOR', 'TRANSPORTISTA', 'ADMIN')")
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

    /**
     * Calcula una ruta que pasa por múltiples puntos de forma secuencial
     * @param request Lista de coordenadas (mínimo 2 puntos)
     * @return Ruta calculada que conecta todos los puntos
     */
    @PostMapping("/ruta-multiple")
    @PreAuthorize("hasAnyRole('OPERADOR', 'TRANSPORTISTA', 'ADMIN')")
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

    /**
     * Calcula una ruta entre dos puntos usando parámetros de consulta (GET)
     * @param origenLat Latitud del punto de origen
     * @param origenLong Longitud del punto de origen
     * @param destinoLat Latitud del punto de destino
     * @param destinoLong Longitud del punto de destino
     * @return Ruta calculada con distancia y duración
     */
    @GetMapping("/ruta-simple")
    @PreAuthorize("hasAnyRole('OPERADOR', 'TRANSPORTISTA', 'ADMIN')")
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

    /**
     * Calcula la distancia y duración entre dos puntos (endpoint de compatibilidad)
     * @param origenLat Latitud del punto de origen
     * @param origenLong Longitud del punto de origen
     * @param destinoLat Latitud del punto de destino
     * @param destinoLong Longitud del punto de destino
     * @return Distancia en km y duración en minutos
     */
    @GetMapping("/distancia")
    @PreAuthorize("hasAnyRole('CLIENTE','OPERADOR','ADMIN')")
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
