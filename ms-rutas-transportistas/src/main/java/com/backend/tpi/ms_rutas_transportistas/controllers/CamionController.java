package com.backend.tpi.ms_rutas_transportistas.controllers;

import com.backend.tpi.ms_rutas_transportistas.dtos.CamionDTO;
import com.backend.tpi.ms_rutas_transportistas.services.CamionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/camiones")
public class CamionController {

    @Autowired
    private CamionService camionService;

    @GetMapping
    @PreAuthorize("hasAnyRole('RESPONSABLE','TRANSPORTISTA','ADMIN')")
    public List<CamionDTO> getAllCamiones() {
        return camionService.findAll();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('RESPONSABLE','ADMIN')")
    public CamionDTO createCamion(@RequestBody CamionDTO camion) {
        return camionService.save(camion);
    }
}
