package com.backend.tpi.ms_gestion_calculos.controllers;

import com.backend.tpi.ms_gestion_calculos.dtos.TarifaDTO;
import com.backend.tpi.ms_gestion_calculos.dtos.TarifaVolumenPesoDTO;
import com.backend.tpi.ms_gestion_calculos.services.TarifaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tarifas")
public class TarifaController {

    @Autowired
    private TarifaService tarifaService;

    @GetMapping
    @PreAuthorize("hasAnyRole('CLIENTE','RESPONSABLE','ADMIN')")
    public List<TarifaDTO> getAllTarifas() {
        return tarifaService.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENTE','RESPONSABLE','ADMIN')")
    public TarifaDTO getTarifaById(@PathVariable Long id) {
        return tarifaService.findById(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('RESPONSABLE','ADMIN')")
    public TarifaDTO createTarifa(@RequestBody TarifaDTO tarifaDto) {
        return tarifaService.save(tarifaDto);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABLE')")
    public TarifaDTO updateTarifa(@PathVariable Long id, @RequestBody TarifaDTO tarifaDto) {
        return tarifaService.update(id, tarifaDto);
    }

    @PostMapping("/{id}/rango")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABLE')")
    public TarifaDTO addRango(@PathVariable Long id, @RequestBody TarifaVolumenPesoDTO rangoDto) {
        return tarifaService.addRango(id, rangoDto);
    }

    @PatchMapping("/{idTarifa}/rango/{idRango}")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABLE')")
    public TarifaDTO updateRango(
            @PathVariable Long idTarifa,
            @PathVariable Long idRango,
            @RequestBody TarifaVolumenPesoDTO rangoDto) {
        return tarifaService.updateRango(idTarifa, idRango, rangoDto);
    }
}
