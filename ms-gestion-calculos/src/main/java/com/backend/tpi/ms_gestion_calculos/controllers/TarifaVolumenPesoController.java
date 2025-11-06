package com.backend.tpi.ms_gestion_calculos.controllers;

import com.backend.tpi.ms_gestion_calculos.dtos.TarifaVolumenPesoDTO;
import com.backend.tpi.ms_gestion_calculos.services.TarifaVolumenPesoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tarifa-volumen-peso")
public class TarifaVolumenPesoController {

    @Autowired
    private TarifaVolumenPesoService service;

    @GetMapping
    @PreAuthorize("hasAnyRole('CLIENTE','RESPONSABLE','ADMIN')")
    public List<TarifaVolumenPesoDTO> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENTE','RESPONSABLE','ADMIN')")
    public TarifaVolumenPesoDTO getById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('RESPONSABLE','ADMIN')")
    public TarifaVolumenPesoDTO create(@RequestBody TarifaVolumenPesoDTO dto) {
        return service.save(dto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('RESPONSABLE','ADMIN')")
    public TarifaVolumenPesoDTO update(@PathVariable Long id, @RequestBody TarifaVolumenPesoDTO dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('RESPONSABLE','ADMIN')")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

}
