package com.backend.tpi.ms_gestion_calculos.services;

import com.backend.tpi.ms_gestion_calculos.dtos.DepositoDTO;
import com.backend.tpi.ms_gestion_calculos.models.Deposito;
import com.backend.tpi.ms_gestion_calculos.repositories.DepositoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DepositoService {

    @Autowired
    private DepositoRepository depositoRepository;

    public List<DepositoDTO> findAll() {
        return depositoRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    public DepositoDTO save(DepositoDTO dto) {
        Deposito deposito = toEntity(dto);
        Deposito saved = depositoRepository.save(deposito);
        return toDto(saved);
    }

    public DepositoDTO findById(Long id) {
        Deposito deposito = depositoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Depósito no encontrado con id: " + id));
        return toDto(deposito);
    }

    public DepositoDTO update(Long id, DepositoDTO dto) {
        Deposito deposito = depositoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Depósito no encontrado con id: " + id));
        
        if (dto.getNombre() != null) {
            deposito.setNombre(dto.getNombre());
        }
        if (dto.getDireccion() != null) {
            deposito.setDireccion(dto.getDireccion());
        }
        if (dto.getLatitud() != null) {
            deposito.setLatitud(java.math.BigDecimal.valueOf(dto.getLatitud()));
        }
        if (dto.getLongitud() != null) {
            deposito.setLongitud(java.math.BigDecimal.valueOf(dto.getLongitud()));
        }
        if (dto.getIdCiudad() != null) {
            // Aquí podrías buscar la ciudad si tienes el repositorio
            // Por ahora solo guardamos el ID
        }
        
        Deposito saved = depositoRepository.save(deposito);
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
