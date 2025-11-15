package com.backend.tpi.ms_gestion_calculos.services;

import com.backend.tpi.ms_gestion_calculos.dtos.TarifaDTO;
import com.backend.tpi.ms_gestion_calculos.dtos.TarifaVolumenPesoDTO;
import com.backend.tpi.ms_gestion_calculos.models.Tarifa;
import com.backend.tpi.ms_gestion_calculos.models.TarifaVolumenPeso;
import com.backend.tpi.ms_gestion_calculos.repositories.TarifaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TarifaService {

    @Autowired
    private TarifaRepository tarifaRepository;

    public List<TarifaDTO> findAll() {
        return tarifaRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    public TarifaDTO findById(Long id) {
        Tarifa tarifa = tarifaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tarifa no encontrada con id: " + id));
        return toDto(tarifa);
    }

    public TarifaDTO save(TarifaDTO dto) {
        Tarifa tarifa = toEntity(dto);
        Tarifa saved = tarifaRepository.save(tarifa);
        return toDto(saved);
    }

    @Transactional
    public TarifaDTO update(Long id, TarifaDTO dto) {
        Tarifa tarifa = tarifaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tarifa no encontrada con id: " + id));
        
        if (dto.getCostoBaseGestionFijo() != null) {
            tarifa.setCostoBaseGestionFijo(java.math.BigDecimal.valueOf(dto.getCostoBaseGestionFijo()));
        }
        if (dto.getValorLitroCombustible() != null) {
            tarifa.setValorLitroCombustible(java.math.BigDecimal.valueOf(dto.getValorLitroCombustible()));
        }
        
        Tarifa saved = tarifaRepository.save(tarifa);
        return toDto(saved);
    }

    @Transactional
    public TarifaDTO addRango(Long tarifaId, TarifaVolumenPesoDTO rangoDto) {
        Tarifa tarifa = tarifaRepository.findById(tarifaId)
                .orElseThrow(() -> new RuntimeException("Tarifa no encontrada con id: " + tarifaId));
        
        TarifaVolumenPeso rango = new TarifaVolumenPeso();
        rango.setTarifa(tarifa);
        rango.setVolumenMin(rangoDto.getVolumenMin());
        rango.setVolumenMax(rangoDto.getVolumenMax());
        rango.setPesoMin(rangoDto.getPesoMin());
        rango.setPesoMax(rangoDto.getPesoMax());
        rango.setCostoPorKmBase(rangoDto.getCostoPorKmBase());
        
        tarifa.getRangos().add(rango);
        Tarifa saved = tarifaRepository.save(tarifa);
        return toDto(saved);
    }

    @Transactional
    public TarifaDTO updateRango(Long tarifaId, Long rangoId, TarifaVolumenPesoDTO rangoDto) {
        Tarifa tarifa = tarifaRepository.findById(tarifaId)
                .orElseThrow(() -> new RuntimeException("Tarifa no encontrada con id: " + tarifaId));
        
        TarifaVolumenPeso rango = tarifa.getRangos().stream()
                .filter(r -> r.getId().equals(rangoId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Rango no encontrado con id: " + rangoId));
        
        if (rangoDto.getVolumenMin() != null) {
            rango.setVolumenMin(rangoDto.getVolumenMin());
        }
        if (rangoDto.getVolumenMax() != null) {
            rango.setVolumenMax(rangoDto.getVolumenMax());
        }
        if (rangoDto.getPesoMin() != null) {
            rango.setPesoMin(rangoDto.getPesoMin());
        }
        if (rangoDto.getPesoMax() != null) {
            rango.setPesoMax(rangoDto.getPesoMax());
        }
        if (rangoDto.getCostoPorKmBase() != null) {
            rango.setCostoPorKmBase(rangoDto.getCostoPorKmBase());
        }
        
        Tarifa saved = tarifaRepository.save(tarifa);
        return toDto(saved);
    }

    private TarifaDTO toDto(Tarifa tarifa) {
        if (tarifa == null) return null;
        TarifaDTO dto = new TarifaDTO();
        dto.setId(tarifa.getId());
        if (tarifa.getCostoBaseGestionFijo() != null) {
            dto.setCostoBaseGestionFijo(tarifa.getCostoBaseGestionFijo().doubleValue());
        }
        if (tarifa.getValorLitroCombustible() != null) {
            dto.setValorLitroCombustible(tarifa.getValorLitroCombustible().doubleValue());
        }
        if (tarifa.getRangos() != null && !tarifa.getRangos().isEmpty()) {
            dto.setRangos(tarifa.getRangos().stream()
                    .map(this::rangoToDto)
                    .collect(Collectors.toList()));
        }
        return dto;
    }

    private TarifaVolumenPesoDTO rangoToDto(TarifaVolumenPeso rango) {
        if (rango == null) return null;
        TarifaVolumenPesoDTO dto = new TarifaVolumenPesoDTO();
        dto.setId(rango.getId());
        dto.setVolumenMin(rango.getVolumenMin());
        dto.setVolumenMax(rango.getVolumenMax());
        dto.setPesoMin(rango.getPesoMin());
        dto.setPesoMax(rango.getPesoMax());
        dto.setCostoPorKmBase(rango.getCostoPorKmBase());
        return dto;
    }

    private Tarifa toEntity(TarifaDTO dto) {
        if (dto == null) return null;
        Tarifa tarifa = new Tarifa();
        tarifa.setId(dto.getId());
        if (dto.getCostoBaseGestionFijo() != null) {
            tarifa.setCostoBaseGestionFijo(java.math.BigDecimal.valueOf(dto.getCostoBaseGestionFijo()));
        }
        if (dto.getValorLitroCombustible() != null) {
            tarifa.setValorLitroCombustible(java.math.BigDecimal.valueOf(dto.getValorLitroCombustible()));
        }
        return tarifa;
    }
}
