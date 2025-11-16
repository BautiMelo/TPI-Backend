package com.backend.tpi.ms_solicitudes.controllers;

import com.backend.tpi.ms_solicitudes.dtos.CreateSolicitudDTO;
import com.backend.tpi.ms_solicitudes.dtos.SolicitudDTO;
import com.backend.tpi.ms_solicitudes.services.SolicitudService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/solicitudes")
public class SolicitudController {

    private static final Logger logger = LoggerFactory.getLogger(SolicitudController.class);

    @Autowired
    private SolicitudService solicitudService;

    @PostMapping
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<SolicitudDTO> create(@RequestBody CreateSolicitudDTO createSolicitudDTO) {
        logger.info("POST /api/v1/solicitudes - Creando nueva solicitud");
        SolicitudDTO result = solicitudService.create(createSolicitudDTO);
        logger.info("POST /api/v1/solicitudes - Respuesta: 200 - Solicitud creada con ID: {}", result.getId());
        return ResponseEntity.ok(result);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('RESPONSABLE','ADMIN')")
    public ResponseEntity<List<SolicitudDTO>> findAll(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Long clienteId) {
        logger.info("GET /api/v1/solicitudes - Consultando solicitudes con filtros - estado: {}, clienteId: {}", estado, clienteId);
        List<SolicitudDTO> result = solicitudService.findAllWithFilters(estado, clienteId);
        logger.info("GET /api/v1/solicitudes - Respuesta: 200 - {} solicitudes encontradas", result.size());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENTE','RESPONSABLE','ADMIN')")
    public ResponseEntity<SolicitudDTO> findById(@PathVariable Long id) {
        logger.info("GET /api/v1/solicitudes/{} - Buscando solicitud por ID", id);
        SolicitudDTO solicitudDTO = solicitudService.findById(id);
        if (solicitudDTO == null) {
            logger.warn("GET /api/v1/solicitudes/{} - Respuesta: 404 - Solicitud no encontrada", id);
            return ResponseEntity.notFound().build();
        }
        logger.info("GET /api/v1/solicitudes/{} - Respuesta: 200 - Solicitud encontrada", id);
        return ResponseEntity.ok(solicitudDTO);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('RESPONSABLE','ADMIN')")
    public ResponseEntity<SolicitudDTO> update(@PathVariable Long id, @RequestBody CreateSolicitudDTO createSolicitudDTO) {
        logger.info("PUT /api/v1/solicitudes/{} - Actualizando solicitud", id);
        SolicitudDTO solicitudDTO = solicitudService.update(id, createSolicitudDTO);
        if (solicitudDTO == null) {
            logger.warn("PUT /api/v1/solicitudes/{} - Respuesta: 404 - Solicitud no encontrada", id);
            return ResponseEntity.notFound().build();
        }
        logger.info("PUT /api/v1/solicitudes/{} - Respuesta: 200 - Solicitud actualizada", id);
        return ResponseEntity.ok(solicitudDTO);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('RESPONSABLE','ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        logger.info("DELETE /api/v1/solicitudes/{} - Eliminando solicitud", id);
        solicitudService.delete(id);
        logger.info("DELETE /api/v1/solicitudes/{} - Respuesta: 204 - Solicitud eliminada", id);
        return ResponseEntity.noContent().build();
    }

    // ---- Integration endpoints (delegan al service) ----

    @PostMapping("/{id}/request-route")
    @PreAuthorize("hasAnyRole('RESPONSABLE','ADMIN')")
    public ResponseEntity<Object> requestRoute(@PathVariable Long id) {
        logger.info("POST /api/v1/solicitudes/{}/request-route - Solicitando ruta", id);
        Object result = solicitudService.requestRoute(id);
        logger.info("POST /api/v1/solicitudes/{}/request-route - Respuesta: 200 - Ruta solicitada", id);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/calculate-price")
    @PreAuthorize("hasAnyRole('RESPONSABLE','ADMIN')")
    public ResponseEntity<Object> calculatePrice(@PathVariable Long id) {
        logger.info("POST /api/v1/solicitudes/{}/calculate-price - Calculando precio", id);
        Object result = solicitudService.calculatePrice(id);
        logger.info("POST /api/v1/solicitudes/{}/calculate-price - Respuesta: 200 - Precio calculado", id);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/assign-transport")
    @PreAuthorize("hasAnyRole('RESPONSABLE','ADMIN')")
    public ResponseEntity<Object> assignTransport(@PathVariable Long id, @RequestParam Long transportistaId) {
        logger.info("POST /api/v1/solicitudes/{}/assign-transport - Asignando transporte - transportistaId: {}", id, transportistaId);
        Object result = solicitudService.assignTransport(id, transportistaId);
        logger.info("POST /api/v1/solicitudes/{}/assign-transport - Respuesta: 200 - Transporte asignado", id);
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('RESPONSABLE','ADMIN')")
    public ResponseEntity<SolicitudDTO> updateEstado(@PathVariable Long id, @RequestParam Long estadoId) {
        logger.info("PATCH /api/v1/solicitudes/{}/estado - Actualizando estado - nuevoEstadoId: {}", id, estadoId);
        SolicitudDTO solicitudDTO = solicitudService.updateEstado(id, estadoId);
        logger.info("PATCH /api/v1/solicitudes/{}/estado - Respuesta: 200 - Estado actualizado", id);
        return ResponseEntity.ok(solicitudDTO);
    }

    @PatchMapping("/{id}/programar")
    @PreAuthorize("hasAnyRole('RESPONSABLE','ADMIN')")
    public ResponseEntity<SolicitudDTO> programar(
            @PathVariable Long id, 
            @RequestParam java.math.BigDecimal costoEstimado,
            @RequestParam java.math.BigDecimal tiempoEstimado) {
        logger.info("PATCH /api/v1/solicitudes/{}/programar - Programando solicitud - costoEstimado: {}, tiempoEstimado: {}", 
            id, costoEstimado, tiempoEstimado);
        SolicitudDTO solicitudDTO = solicitudService.programar(id, costoEstimado, tiempoEstimado);
        logger.info("PATCH /api/v1/solicitudes/{}/programar - Respuesta: 200 - Solicitud programada", id);
        return ResponseEntity.ok(solicitudDTO);
    }
}
