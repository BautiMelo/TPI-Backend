package com.backend.tpi.ms_gestion_calculos.services;

import com.backend.tpi.ms_gestion_calculos.dtos.TarifaDTO;
import com.backend.tpi.ms_gestion_calculos.models.Tarifa;
import com.backend.tpi.ms_gestion_calculos.repositories.TarifaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TarifaService {

    @Autowired
    private TarifaRepository tarifaRepository;

    public List<TarifaDTO> findAll() {
        return tarifaRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    public TarifaDTO save(TarifaDTO dto) {
        Tarifa tarifa = toEntity(dto);
        Tarifa saved = tarifaRepository.save(tarifa);
        return toDto(saved);
    }

    private TarifaDTO toDto(Tarifa tarifa) {
        if (tarifa == null) return null;
        TarifaDTO dto = new TarifaDTO();
        dto.setId(tarifa.getId());
        if (tarifa.getValorLitroCombustible() != null) {
            dto.setPrecioPorKm(tarifa.getValorLitroCombustible().doubleValue());
        }
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
        return tarifa;
    }
}
