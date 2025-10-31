package com.backend.tpi.ms_rutas_transportistas.controllers;

import com.backend.tpi.ms_rutas_transportistas.models.Camion;
import com.backend.tpi.ms_rutas_transportistas.services.CamionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/camiones")
public class CamionController {

    @Autowired
    private CamionService camionService;

    @GetMapping
    public List<Camion> getAllCamiones() {
        return camionService.findAll();
    }

    @PostMapping
    public Camion createCamion(@RequestBody Camion camion) {
        return camionService.save(camion);
    }
}
