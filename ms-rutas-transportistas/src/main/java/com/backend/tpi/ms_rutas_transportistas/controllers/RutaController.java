package com.backend.tpi.ms_rutas_transportistas.controllers;

import com.backend.tpi.ms_rutas_transportistas.dtos.CreateRutaDTO;
import com.backend.tpi.ms_rutas_transportistas.dtos.RutaDTO;
import com.backend.tpi.ms_rutas_transportistas.services.RutaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rutas")
public class RutaController {

    @Autowired
    private RutaService rutaService;

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
}
