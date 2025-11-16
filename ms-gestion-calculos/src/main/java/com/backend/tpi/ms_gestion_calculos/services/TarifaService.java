package com.backend.tpi.ms_gestion_calculos.services;

import com.backend.tpi.ms_gestion_calculos.dtos.TarifaDTO;
import com.backend.tpi.ms_gestion_calculos.dtos.TarifaVolumenPesoDTO;
import com.backend.tpi.ms_gestion_calculos.models.Tarifa;
import com.backend.tpi.ms_gestion_calculos.models.TarifaVolumenPeso;
import com.backend.tpi.ms_gestion_calculos.repositories.TarifaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TarifaService {

    private static final Logger logger = LoggerFactory.getLogger(TarifaService.class);

    @Autowired
    private TarifaRepository tarifaRepository;

    public List<TarifaDTO> findAll() {
        logger.info("Obteniendo todas las tarifas");
        List<TarifaDTO> tarifas = tarifaRepository.findAll().stream()
                .map(this::toDto)
                .toList();
        logger.debug("Tarifas obtenidas: {}", tarifas.size());
        return tarifas;
    }

    public TarifaDTO findById(Long id) {
        logger.info("Buscando tarifa por ID: {}", id);
        Tarifa tarifa = tarifaRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Tarifa no encontrada con ID: {}", id);
                    return new RuntimeException("Tarifa no encontrada con id: " + id);
                });
        logger.debug("Tarifa encontrada: ID={}", tarifa.getId());
        return toDto(tarifa);
    }

    public TarifaDTO save(TarifaDTO dto) {
        logger.info("Creando nueva tarifa");
        Tarifa tarifa = toEntity(dto);
        Tarifa saved = tarifaRepository.save(tarifa);
        logger.info("Tarifa creada exitosamente con ID: {}", saved.getId());
        return toDto(saved);
    }

    @Transactional
    public TarifaDTO update(Long id, TarifaDTO dto) {
        logger.info("Actualizando tarifa ID: {}", id);
        Tarifa tarifa = tarifaRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("No se pudo actualizar - Tarifa no encontrada con ID: {}", id);
                    return new RuntimeException("Tarifa no encontrada con id: " + id);
                });
        
        if (dto.getCostoBaseGestionFijo() != null) {
            logger.debug("Actualizando costoBaseGestionFijo: {}", dto.getCostoBaseGestionFijo());
            tarifa.setCostoBaseGestionFijo(java.math.BigDecimal.valueOf(dto.getCostoBaseGestionFijo()));
        }
        if (dto.getValorLitroCombustible() != null) {
            logger.debug("Actualizando valorLitroCombustible: {}", dto.getValorLitroCombustible());
            tarifa.setValorLitroCombustible(java.math.BigDecimal.valueOf(dto.getValorLitroCombustible()));
        }
        
        Tarifa saved = tarifaRepository.save(tarifa);
        logger.info("Tarifa actualizada exitosamente: ID={}", saved.getId());
        return toDto(saved);
    }

    @Transactional
    public TarifaDTO addRango(Long tarifaId, TarifaVolumenPesoDTO rangoDto) {
        logger.info("Agregando rango a tarifa ID: {}", tarifaId);
        Tarifa tarifa = tarifaRepository.findById(tarifaId)
                .orElseThrow(() -> {
                    logger.warn("No se pudo agregar rango - Tarifa no encontrada con ID: {}", tarifaId);
                    return new RuntimeException("Tarifa no encontrada con id: " + tarifaId);
                });
        
        logger.debug("Creando nuevo rango - volumen: {}-{}, peso: {}-{}", 
                rangoDto.getVolumenMin(), rangoDto.getVolumenMax(), 
                rangoDto.getPesoMin(), rangoDto.getPesoMax());
        TarifaVolumenPeso rango = new TarifaVolumenPeso();
        rango.setTarifa(tarifa);
        rango.setVolumenMin(rangoDto.getVolumenMin());
        rango.setVolumenMax(rangoDto.getVolumenMax());
        rango.setPesoMin(rangoDto.getPesoMin());
        rango.setPesoMax(rangoDto.getPesoMax());
        rango.setCostoPorKmBase(rangoDto.getCostoPorKmBase());
        
        tarifa.getRangos().add(rango);
        Tarifa saved = tarifaRepository.save(tarifa);
        logger.info("Rango agregado exitosamente a tarifa ID: {}", tarifaId);
        return toDto(saved);
    }

    @Transactional
    public TarifaDTO updateRango(Long tarifaId, Long rangoId, TarifaVolumenPesoDTO rangoDto) {
        logger.info("Actualizando rango ID: {} de tarifa ID: {}", rangoId, tarifaId);
        Tarifa tarifa = tarifaRepository.findById(tarifaId)
                .orElseThrow(() -> {
                    logger.warn("No se pudo actualizar rango - Tarifa no encontrada con ID: {}", tarifaId);
                    return new RuntimeException("Tarifa no encontrada con id: " + tarifaId);
                });
        
        TarifaVolumenPeso rango = tarifa.getRangos().stream()
                .filter(r -> r.getId().equals(rangoId))
                .findFirst()
                .orElseThrow(() -> {
                    logger.warn("No se pudo actualizar - Rango no encontrado con ID: {}", rangoId);
                    return new RuntimeException("Rango no encontrado con id: " + rangoId);
                });
        
        if (rangoDto.getVolumenMin() != null) {
            logger.debug("Actualizando volumenMin: {}", rangoDto.getVolumenMin());
            rango.setVolumenMin(rangoDto.getVolumenMin());
        }
        if (rangoDto.getVolumenMax() != null) {
            logger.debug("Actualizando volumenMax: {}", rangoDto.getVolumenMax());
            rango.setVolumenMax(rangoDto.getVolumenMax());
        }
        if (rangoDto.getPesoMin() != null) {
            logger.debug("Actualizando pesoMin: {}", rangoDto.getPesoMin());
            rango.setPesoMin(rangoDto.getPesoMin());
        }
        if (rangoDto.getPesoMax() != null) {
            logger.debug("Actualizando pesoMax: {}", rangoDto.getPesoMax());
            rango.setPesoMax(rangoDto.getPesoMax());
        }
        if (rangoDto.getCostoPorKmBase() != null) {
            logger.debug("Actualizando costoPorKmBase: {}", rangoDto.getCostoPorKmBase());
            rango.setCostoPorKmBase(rangoDto.getCostoPorKmBase());
        }
        
        Tarifa saved = tarifaRepository.save(tarifa);
        logger.info("Rango actualizado exitosamente: ID={}", rangoId);
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
