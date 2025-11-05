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
    public ResponseEntity<List<SolicitudDTO>> findAll() {
        return ResponseEntity.ok(solicitudService.findAll());
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
}
