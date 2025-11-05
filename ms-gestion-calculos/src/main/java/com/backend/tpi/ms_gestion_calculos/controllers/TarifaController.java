package com.backend.tpi.ms_gestion_calculos.controllers;

import com.backend.tpi.ms_gestion_calculos.models.Tarifa;
import com.backend.tpi.ms_gestion_calculos.dtos.TarifaDTO;
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
        return tarifaService.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('RESPONSABLE','ADMIN')")
    public TarifaDTO createTarifa(@RequestBody TarifaDTO tarifaDto) {
        Tarifa tarifa = toEntity(tarifaDto);
        Tarifa saved = tarifaService.save(tarifa);
        return toDto(saved);
    }

    // Manual mapping helpers (no external mapper library)
    private TarifaDTO toDto(Tarifa tarifa) {
        if (tarifa == null) return null;
        TarifaDTO dto = new TarifaDTO();
        dto.setId(tarifa.getId());
        // map valorLitroCombustible -> precioPorKm (approximation)
        if (tarifa.getValorLitroCombustible() != null) {
            dto.setPrecioPorKm(tarifa.getValorLitroCombustible().doubleValue());
        }
        // nombre is not present in entity; leave null or set a default
        dto.setNombre(null);
        return dto;
    }

    private Tarifa toEntity(TarifaDTO dto) {
        if (dto == null) return null;
        Tarifa tarifa = new Tarifa();
        tarifa.setId(dto.getId());
        if (dto.getPrecioPorKm() != null) {
            tarifa.setValorLitroCombustible(java.math.BigDecimal.valueOf(dto.getPrecioPorKm()));
        }
        // costoBaseGestionFijo unknown from DTO; leave null
        return tarifa;
    }
}
