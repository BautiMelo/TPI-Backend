package com.backend.tpi.ms_rutas_transportistas.controllers;

import com.backend.tpi.ms_rutas_transportistas.dtos.CreateRutaDTO;
import com.backend.tpi.ms_rutas_transportistas.dtos.RutaDTO;
import com.backend.tpi.ms_rutas_transportistas.dtos.TramoDTO;
import com.backend.tpi.ms_rutas_transportistas.dtos.TramoRequestDTO;
import com.backend.tpi.ms_rutas_transportistas.services.RutaService;
import com.backend.tpi.ms_rutas_transportistas.services.TramoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rutas")
@Tag(name = "Rutas", description = "Gestión de rutas y tramos de transporte")
public class RutaController {

    @Autowired
    private RutaService rutaService;

    @Autowired
    private TramoService tramoService;

    @PostMapping
    @PreAuthorize("hasAnyRole('RESPONSABLE','ADMIN')")
    public ResponseEntity<RutaDTO> create(@RequestBody CreateRutaDTO createRutaDTO) {
        return ResponseEntity.ok(rutaService.create(createRutaDTO));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CLIENTE','RESPONSABLE','ADMIN','TRANSPORTISTA')")
    public ResponseEntity<List<RutaDTO>> findAll() {
        return ResponseEntity.ok(rutaService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENTE','RESPONSABLE','ADMIN','TRANSPORTISTA')")
    public ResponseEntity<RutaDTO> findById(@PathVariable Long id) {
        RutaDTO rutaDTO = rutaService.findById(id);
        if (rutaDTO == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(rutaDTO);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('RESPONSABLE','ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        rutaService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ---- Integration endpoints (delegan al service) ----

    @PostMapping("/{id}/asignar-transportista")
    @PreAuthorize("hasAnyRole('RESPONSABLE','ADMIN')")
    public ResponseEntity<Object> asignarTransportista(@PathVariable Long id, @RequestParam Long transportistaId) {
        Object result = rutaService.assignTransportista(id, transportistaId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/por-solicitud/{solicitudId}")
    @PreAuthorize("hasAnyRole('RESPONSABLE','ADMIN','TRANSPORTISTA')")
    public ResponseEntity<Object> findBySolicitud(@PathVariable Long solicitudId) {
        Object result = rutaService.findBySolicitudId(solicitudId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/tramos")
    @PreAuthorize("hasAnyRole('RESPONSABLE','ADMIN')")
    @Operation(summary = "Agregar un nuevo tramo a una ruta")
    public ResponseEntity<TramoDTO> agregarTramo(
            @PathVariable Long id,
            @RequestBody TramoRequestDTO tramoRequest) {
        tramoRequest.setIdRuta(id); // Asegurar que el tramo se asocie a esta ruta
        TramoDTO tramo = tramoService.create(tramoRequest);
        if (tramo == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(tramo);
    }

    @PostMapping("/{id}/tramos/{tramoId}/iniciar")
    @PreAuthorize("hasAnyRole('TRANSPORTISTA','RESPONSABLE','ADMIN')")
    @Operation(summary = "Marcar el inicio de un tramo de transporte")
    public ResponseEntity<TramoDTO> iniciarTramo(
            @PathVariable Long id,
            @PathVariable Long tramoId) {
        TramoDTO tramo = tramoService.iniciarTramo(id, tramoId);
        if (tramo == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(tramo);
    }

    @PostMapping("/{id}/tramos/{tramoId}/finalizar")
    @PreAuthorize("hasAnyRole('TRANSPORTISTA','RESPONSABLE','ADMIN')")
    @Operation(summary = "Marcar la finalización de un tramo de transporte")
    public ResponseEntity<TramoDTO> finalizarTramo(
            @PathVariable Long id,
            @PathVariable Long tramoId,
            @RequestParam(required = false) String fechaHoraReal) {
        
        java.time.LocalDateTime fechaHora = null;
        if (fechaHoraReal != null && !fechaHoraReal.isEmpty()) {
            try {
                fechaHora = java.time.LocalDateTime.parse(fechaHoraReal);
            } catch (Exception e) {
                return ResponseEntity.badRequest().build();
            }
        }
        
        TramoDTO tramo = tramoService.finalizarTramo(id, tramoId, fechaHora);
        if (tramo == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(tramo);
    }
}
