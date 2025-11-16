package com.backend.tpi.ms_gestion_calculos.services;

import com.backend.tpi.ms_gestion_calculos.dtos.DepositoDTO;
import com.backend.tpi.ms_gestion_calculos.models.Deposito;
import com.backend.tpi.ms_gestion_calculos.repositories.DepositoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DepositoService {

    private static final Logger logger = LoggerFactory.getLogger(DepositoService.class);

    @Autowired
    private DepositoRepository depositoRepository;

    public List<DepositoDTO> findAll() {
        logger.info("Obteniendo todos los depósitos");
        List<DepositoDTO> depositos = depositoRepository.findAll().stream()
                .map(this::toDto)
                .toList();
        logger.debug("Depósitos obtenidos: {}", depositos.size());
        return depositos;
    }

    public DepositoDTO save(DepositoDTO dto) {
        logger.info("Creando nuevo depósito: {}", dto.getNombre());
        Deposito deposito = toEntity(dto);
        Deposito saved = depositoRepository.save(deposito);
        logger.info("Depósito creado exitosamente con ID: {}", saved.getId());
        return toDto(saved);
    }

    public DepositoDTO findById(Long id) {
        logger.info("Buscando depósito por ID: {}", id);
        Deposito deposito = depositoRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Depósito no encontrado con ID: {}", id);
                    return new RuntimeException("Depósito no encontrado con id: " + id);
                });
        logger.debug("Depósito encontrado: ID={}, nombre={}", deposito.getId(), deposito.getNombre());
        return toDto(deposito);
    }

    public DepositoDTO update(Long id, DepositoDTO dto) {
        logger.info("Actualizando depósito ID: {}", id);
        Deposito deposito = depositoRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("No se pudo actualizar - Depósito no encontrado con ID: {}", id);
                    return new RuntimeException("Depósito no encontrado con id: " + id);
                });
        
        if (dto.getNombre() != null) {
            logger.debug("Actualizando nombre: {}", dto.getNombre());
            deposito.setNombre(dto.getNombre());
        }
        if (dto.getDireccion() != null) {
            logger.debug("Actualizando dirección: {}", dto.getDireccion());
            deposito.setDireccion(dto.getDireccion());
        }
        if (dto.getLatitud() != null) {
            logger.debug("Actualizando latitud: {}", dto.getLatitud());
            deposito.setLatitud(java.math.BigDecimal.valueOf(dto.getLatitud()));
        }
        if (dto.getLongitud() != null) {
            logger.debug("Actualizando longitud: {}", dto.getLongitud());
            deposito.setLongitud(java.math.BigDecimal.valueOf(dto.getLongitud()));
        }
        if (dto.getIdCiudad() != null) {
            logger.debug("Actualizando ID ciudad: {}", dto.getIdCiudad());
            // Aquí podrías buscar la ciudad si tienes el repositorio
            // Por ahora solo guardamos el ID
        }
        
        Deposito saved = depositoRepository.save(deposito);
        logger.info("Depósito actualizado exitosamente: ID={}, nombre={}", saved.getId(), saved.getNombre());
        return toDto(saved);
    }

    private DepositoDTO toDto(Deposito deposito) {
        if (deposito == null) return null;
        DepositoDTO dto = new DepositoDTO();
        dto.setId(deposito.getId());
        dto.setNombre(deposito.getNombre());
        dto.setDireccion(deposito.getDireccion());
        if (deposito.getLatitud() != null) {
            dto.setLatitud(deposito.getLatitud().doubleValue());
        }
        if (deposito.getLongitud() != null) {
            dto.setLongitud(deposito.getLongitud().doubleValue());
        }
        if (deposito.getCiudad() != null) {
            dto.setIdCiudad(deposito.getCiudad().getId());
        }
        return dto;
    }

    private Deposito toEntity(DepositoDTO dto) {
        if (dto == null) return null;
        Deposito deposito = new Deposito();
        deposito.setId(dto.getId());
        deposito.setNombre(dto.getNombre());
        deposito.setDireccion(dto.getDireccion());
        if (dto.getLatitud() != null) {
            deposito.setLatitud(java.math.BigDecimal.valueOf(dto.getLatitud()));
        }
        if (dto.getLongitud() != null) {
            deposito.setLongitud(java.math.BigDecimal.valueOf(dto.getLongitud()));
        }
        return deposito;
    }
}
