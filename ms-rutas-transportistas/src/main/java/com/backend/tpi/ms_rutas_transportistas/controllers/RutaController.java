package com.backend.tpi.ms_rutas_transportistas.controllers;

import com.backend.tpi.ms_rutas_transportistas.dtos.CreateRutaDTO;
import com.backend.tpi.ms_rutas_transportistas.dtos.RutaDTO;
import com.backend.tpi.ms_rutas_transportistas.dtos.TramoDTO;
import com.backend.tpi.ms_rutas_transportistas.dtos.TramoRequestDTO;
import com.backend.tpi.ms_rutas_transportistas.services.RutaService;
import com.backend.tpi.ms_rutas_transportistas.services.TramoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rutas")
@Tag(name = "Rutas", description = "Gestión de rutas y tramos de transporte")
public class RutaController {

    private static final Logger logger = LoggerFactory.getLogger(RutaController.class);

    @Autowired
    private RutaService rutaService;

    @Autowired
    private TramoService tramoService;

    @PostMapping
    @PreAuthorize("hasAnyRole('RESPONSABLE','ADMIN')")
    public ResponseEntity<RutaDTO> create(@RequestBody CreateRutaDTO createRutaDTO) {
        logger.info("POST /api/v1/rutas - Creando nueva ruta para solicitud ID: {}", createRutaDTO.getIdSolicitud());
        RutaDTO result = rutaService.create(createRutaDTO);
        logger.info("POST /api/v1/rutas - Respuesta: 200 - Ruta creada con ID: {}", result.getId());
        return ResponseEntity.ok(result);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CLIENTE','RESPONSABLE','ADMIN','TRANSPORTISTA')")
    public ResponseEntity<List<RutaDTO>> findAll() {
        logger.info("GET /api/v1/rutas - Listando todas las rutas");
        List<RutaDTO> result = rutaService.findAll();
        logger.info("GET /api/v1/rutas - Respuesta: 200 - {} rutas encontradas", result.size());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENTE','RESPONSABLE','ADMIN','TRANSPORTISTA')")
    public ResponseEntity<RutaDTO> findById(@PathVariable Long id) {
        logger.info("GET /api/v1/rutas/{} - Buscando ruta por ID", id);
        RutaDTO rutaDTO = rutaService.findById(id);
        if (rutaDTO == null) {
            logger.warn("GET /api/v1/rutas/{} - Respuesta: 404 - Ruta no encontrada", id);
            return ResponseEntity.notFound().build();
        }
        logger.info("GET /api/v1/rutas/{} - Respuesta: 200 - Ruta encontrada", id);
        return ResponseEntity.ok(rutaDTO);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('RESPONSABLE','ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        logger.info("DELETE /api/v1/rutas/{} - Eliminando ruta", id);
        rutaService.delete(id);
        logger.info("DELETE /api/v1/rutas/{} - Respuesta: 204 - Ruta eliminada", id);
        return ResponseEntity.noContent().build();
    }

    // ---- Integration endpoints (delegan al service) ----

    @PostMapping("/{id}/asignar-transportista")
    @PreAuthorize("hasAnyRole('RESPONSABLE','ADMIN')")
    public ResponseEntity<Object> asignarTransportista(@PathVariable Long id, @RequestParam Long transportistaId) {
        logger.info("POST /api/v1/rutas/{}/asignar-transportista - Asignando transportista ID: {}", id, transportistaId);
        Object result = rutaService.assignTransportista(id, transportistaId);
        logger.info("POST /api/v1/rutas/{}/asignar-transportista - Respuesta: 200 - Transportista asignado", id);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/por-solicitud/{solicitudId}")
    @PreAuthorize("hasAnyRole('RESPONSABLE','ADMIN','TRANSPORTISTA')")
    public ResponseEntity<Object> findBySolicitud(@PathVariable Long solicitudId) {
        logger.info("GET /api/v1/rutas/por-solicitud/{} - Buscando ruta por solicitud", solicitudId);
        Object result = rutaService.findBySolicitudId(solicitudId);
        logger.info("GET /api/v1/rutas/por-solicitud/{} - Respuesta: 200 - Ruta encontrada", solicitudId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/tramos")
    @PreAuthorize("hasAnyRole('RESPONSABLE','ADMIN')")
    @Operation(summary = "Agregar un nuevo tramo a una ruta")
    public ResponseEntity<TramoDTO> agregarTramo(
            @PathVariable Long id,
            @RequestBody TramoRequestDTO tramoRequest) {
        logger.info("POST /api/v1/rutas/{}/tramos - Agregando nuevo tramo a ruta", id);
        tramoRequest.setIdRuta(id); // Asegurar que el tramo se asocie a esta ruta
        TramoDTO tramo = tramoService.create(tramoRequest);
        if (tramo == null) {
            logger.warn("POST /api/v1/rutas/{}/tramos - Respuesta: 400 - Error al crear tramo", id);
            return ResponseEntity.badRequest().build();
        }
        logger.info("POST /api/v1/rutas/{}/tramos - Respuesta: 200 - Tramo creado con ID: {}", id, tramo.getId());
        return ResponseEntity.ok(tramo);
    }

    @PostMapping("/{id}/tramos/{tramoId}/iniciar")
    @PreAuthorize("hasAnyRole('TRANSPORTISTA','RESPONSABLE','ADMIN')")
    @Operation(summary = "Marcar el inicio de un tramo de transporte")
    public ResponseEntity<TramoDTO> iniciarTramo(
            @PathVariable Long id,
            @PathVariable Long tramoId) {
        logger.info("POST /api/v1/rutas/{}/tramos/{}/iniciar - Iniciando tramo", id, tramoId);
        TramoDTO tramo = tramoService.iniciarTramo(id, tramoId);
        if (tramo == null) {
            logger.warn("POST /api/v1/rutas/{}/tramos/{}/iniciar - Respuesta: 404 - Tramo no encontrado", id, tramoId);
            return ResponseEntity.notFound().build();
        }
        logger.info("POST /api/v1/rutas/{}/tramos/{}/iniciar - Respuesta: 200 - Tramo iniciado", id, tramoId);
        return ResponseEntity.ok(tramo);
    }

    @PostMapping("/{id}/tramos/{tramoId}/finalizar")
    @PreAuthorize("hasAnyRole('TRANSPORTISTA','RESPONSABLE','ADMIN')")
    @Operation(summary = "Marcar la finalización de un tramo de transporte")
    public ResponseEntity<TramoDTO> finalizarTramo(
            @PathVariable Long id,
            @PathVariable Long tramoId,
            @RequestParam(required = false) String fechaHoraReal) {
        logger.info("POST /api/v1/rutas/{}/tramos/{}/finalizar - Finalizando tramo", id, tramoId);
        
        java.time.LocalDateTime fechaHora = null;
        if (fechaHoraReal != null && !fechaHoraReal.isEmpty()) {
            try {
                fechaHora = java.time.LocalDateTime.parse(fechaHoraReal);
            } catch (Exception e) {
                logger.warn("POST /api/v1/rutas/{}/tramos/{}/finalizar - Respuesta: 400 - Formato de fecha inválido", id, tramoId);
                return ResponseEntity.badRequest().build();
            }
        }
        
        TramoDTO tramo = tramoService.finalizarTramo(id, tramoId, fechaHora);
        if (tramo == null) {
            logger.warn("POST /api/v1/rutas/{}/tramos/{}/finalizar - Respuesta: 404 - Tramo no encontrado", id, tramoId);
            return ResponseEntity.notFound().build();
        }
        logger.info("POST /api/v1/rutas/{}/tramos/{}/finalizar - Respuesta: 200 - Tramo finalizado", id, tramoId);
        return ResponseEntity.ok(tramo);
    }
}
