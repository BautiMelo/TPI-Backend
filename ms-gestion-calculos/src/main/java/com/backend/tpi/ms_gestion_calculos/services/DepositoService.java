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

    private DepositoDTO toDto(Deposito deposito) {
        if (deposito == null) return null;
        DepositoDTO dto = new DepositoDTO();
        dto.setId(deposito.getId());
        dto.setNombre(deposito.getNombre());
        dto.setDireccion(deposito.getDireccion());
        return dto;
    }

    private Deposito toEntity(DepositoDTO dto) {
        if (dto == null) return null;
        Deposito deposito = new Deposito();
        deposito.setId(dto.getId());
        deposito.setNombre(dto.getNombre());
        deposito.setDireccion(dto.getDireccion());
        return deposito;
    }
}
