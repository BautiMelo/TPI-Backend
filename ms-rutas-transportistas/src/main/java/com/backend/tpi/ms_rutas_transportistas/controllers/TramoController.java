package com.backend.tpi.ms_rutas_transportistas.controllers;

import com.backend.tpi.ms_rutas_transportistas.dtos.TramoRequestDTO;
import com.backend.tpi.ms_rutas_transportistas.dtos.TramoDTO;
import com.backend.tpi.ms_rutas_transportistas.services.TramoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tramos")
public class TramoController {

    @Autowired
    private TramoService tramoService;

    @PostMapping
    @PreAuthorize("hasAnyRole('RESPONSABLE','ADMIN')")
    public ResponseEntity<TramoDTO> create(@RequestBody TramoRequestDTO tramoRequestDTO) {
        TramoDTO tramo = tramoService.create(tramoRequestDTO);
        if (tramo == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(tramo);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('RESPONSABLE','TRANSPORTISTA','ADMIN','CLIENTE')")
    public List<TramoDTO> getAllTramos() {
        return tramoService.findAll();
    }
}
