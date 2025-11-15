package com.backend.tpi.ms_solicitudes.controllers;

import com.backend.tpi.ms_solicitudes.dtos.CreateSolicitudDTO;
import com.backend.tpi.ms_solicitudes.dtos.SolicitudDTO;
import com.backend.tpi.ms_solicitudes.services.SolicitudService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/solicitudes")
public class SolicitudController {

    @Autowired
    private SolicitudService solicitudService;

    @PostMapping
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<SolicitudDTO> create(@RequestBody CreateSolicitudDTO createSolicitudDTO) {
        return ResponseEntity.ok(solicitudService.create(createSolicitudDTO));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('RESPONSABLE','ADMIN')")
    public ResponseEntity<List<SolicitudDTO>> findAll(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Long clienteId) {
        return ResponseEntity.ok(solicitudService.findAllWithFilters(estado, clienteId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENTE','RESPONSABLE','ADMIN')")
    public ResponseEntity<SolicitudDTO> findById(@PathVariable Long id) {
        SolicitudDTO solicitudDTO = solicitudService.findById(id);
        if (solicitudDTO == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(solicitudDTO);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('RESPONSABLE','ADMIN')")
    public ResponseEntity<SolicitudDTO> update(@PathVariable Long id, @RequestBody CreateSolicitudDTO createSolicitudDTO) {
        SolicitudDTO solicitudDTO = solicitudService.update(id, createSolicitudDTO);
        if (solicitudDTO == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(solicitudDTO);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('RESPONSABLE','ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        solicitudService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ---- Integration endpoints (delegan al service) ----

    @PostMapping("/{id}/request-route")
    @PreAuthorize("hasAnyRole('RESPONSABLE','ADMIN')")
    public ResponseEntity<Object> requestRoute(@PathVariable Long id) {
        // Delegar al service que deberá comunicarse con ms-rutas-transportistas
        Object result = solicitudService.requestRoute(id);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/calculate-price")
    @PreAuthorize("hasAnyRole('RESPONSABLE','ADMIN')")
    public ResponseEntity<Object> calculatePrice(@PathVariable Long id) {
        // Delegar al service que deberá comunicarse con ms-gestion-calculos
        Object result = solicitudService.calculatePrice(id);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/assign-transport")
    @PreAuthorize("hasAnyRole('RESPONSABLE','ADMIN')")
    public ResponseEntity<Object> assignTransport(@PathVariable Long id, @RequestParam Long transportistaId) {
        // Delegar al service para asignar camion/transportista a la solicitud
        Object result = solicitudService.assignTransport(id, transportistaId);
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('RESPONSABLE','ADMIN')")
    public ResponseEntity<SolicitudDTO> updateEstado(@PathVariable Long id, @RequestParam Long estadoId) {
        SolicitudDTO solicitudDTO = solicitudService.updateEstado(id, estadoId);
        return ResponseEntity.ok(solicitudDTO);
    }

    @PatchMapping("/{id}/programar")
    @PreAuthorize("hasAnyRole('RESPONSABLE','ADMIN')")
    public ResponseEntity<SolicitudDTO> programar(
            @PathVariable Long id, 
            @RequestParam java.math.BigDecimal costoEstimado,
            @RequestParam java.math.BigDecimal tiempoEstimado) {
        SolicitudDTO solicitudDTO = solicitudService.programar(id, costoEstimado, tiempoEstimado);
        return ResponseEntity.ok(solicitudDTO);
    }
}
