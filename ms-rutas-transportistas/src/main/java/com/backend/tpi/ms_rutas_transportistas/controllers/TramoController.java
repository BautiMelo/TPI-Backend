package com.backend.tpi.ms_rutas_transportistas.controllers;

import com.backend.tpi.ms_rutas_transportistas.dtos.TramoRequestDTO;
import com.backend.tpi.ms_rutas_transportistas.dtos.TramoDTO;
import com.backend.tpi.ms_rutas_transportistas.services.TramoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tramos")
public class TramoController {

    private static final Logger logger = LoggerFactory.getLogger(TramoController.class);

    @Autowired
    private TramoService tramoService;

    @PostMapping
    @PreAuthorize("hasAnyRole('RESPONSABLE','ADMIN')")
    public ResponseEntity<TramoDTO> create(@RequestBody TramoRequestDTO tramoRequestDTO) {
        logger.info("POST /api/v1/tramos - Creando nuevo tramo para ruta ID: {}", tramoRequestDTO.getIdRuta());
        TramoDTO tramo = tramoService.create(tramoRequestDTO);
        if (tramo == null) {
            logger.warn("POST /api/v1/tramos - Respuesta: 400 - Error al crear tramo");
            return ResponseEntity.badRequest().build();
        }
        logger.info("POST /api/v1/tramos - Respuesta: 200 - Tramo creado con ID: {}", tramo.getId());
        return ResponseEntity.ok(tramo);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('RESPONSABLE','TRANSPORTISTA','ADMIN','CLIENTE')")
    public List<TramoDTO> getAllTramos() {
        logger.info("GET /api/v1/tramos - Listando todos los tramos");
        List<TramoDTO> result = tramoService.findAll();
        logger.info("GET /api/v1/tramos - Respuesta: 200 - {} tramos encontrados", result.size());
        return result;
    }

    @GetMapping("/por-ruta/{rutaId}")
    @PreAuthorize("hasAnyRole('RESPONSABLE','TRANSPORTISTA','ADMIN','CLIENTE')")
    public List<TramoDTO> getByRuta(@PathVariable Long rutaId) {
        logger.info("GET /api/v1/tramos/por-ruta/{} - Buscando tramos de la ruta", rutaId);
        List<TramoDTO> result = tramoService.findByRutaId(rutaId);
        logger.info("GET /api/v1/tramos/por-ruta/{} - Respuesta: 200 - {} tramos encontrados", rutaId, result.size());
        return result;
    }

    @PostMapping("/{id}/asignar-transportista")
    @PreAuthorize("hasAnyRole('RESPONSABLE','ADMIN')")
    public ResponseEntity<TramoDTO> asignarTransportista(@PathVariable Long id, @RequestParam Long camionId) {
        logger.info("POST /api/v1/tramos/{}/asignar-transportista - Asignando camión ID: {}", id, camionId);
        TramoDTO dto = tramoService.assignTransportista(id, camionId);
        if (dto == null) {
            logger.warn("POST /api/v1/tramos/{}/asignar-transportista - Respuesta: 404 - Tramo no encontrado", id);
            return ResponseEntity.notFound().build();
        }
        logger.info("POST /api/v1/tramos/{}/asignar-transportista - Respuesta: 200 - Camión asignado", id);
        return ResponseEntity.ok(dto);
    }
}
