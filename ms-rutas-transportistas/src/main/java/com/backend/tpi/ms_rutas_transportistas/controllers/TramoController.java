package com.backend.tpi.ms_rutas_transportistas.controllers;

import com.backend.tpi.ms_rutas_transportistas.dtos.TramoRequestDTO;
import com.backend.tpi.ms_rutas_transportistas.models.Tramo;
import com.backend.tpi.ms_rutas_transportistas.services.TramoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tramos")
public class TramoController {

    @Autowired
    private TramoService tramoService;

    @PostMapping
    public ResponseEntity<Tramo> create(@RequestBody TramoRequestDTO tramoRequestDTO) {
        Tramo tramo = tramoService.create(tramoRequestDTO);
        if (tramo == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(tramo);
    }

    @GetMapping
    public List<Tramo> getAllTramos() {
        return tramoService.findAll();
    }
}
