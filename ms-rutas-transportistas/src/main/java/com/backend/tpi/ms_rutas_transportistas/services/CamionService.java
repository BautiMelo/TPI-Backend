package com.backend.tpi.ms_rutas_transportistas.services;

import com.backend.tpi.ms_rutas_transportistas.dtos.CamionDTO;
import com.backend.tpi.ms_rutas_transportistas.models.Camion;
import com.backend.tpi.ms_rutas_transportistas.repositories.CamionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CamionService {

    @Autowired
    private CamionRepository camionRepository;

    public List<CamionDTO> findAll() {
        return camionRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    public CamionDTO save(CamionDTO dto) {
        Camion camion = toEntity(dto);
        Camion saved = camionRepository.save(camion);
        return toDto(saved);
    }

    private CamionDTO toDto(Camion camion) {
        if (camion == null) return null;
        CamionDTO dto = new CamionDTO();
        dto.setId(camion.getId());
        dto.setPatente(camion.getPatente());
        dto.setCapacidad(camion.getCapacidadCarga());
        dto.setEstado(null);
        return dto;
    }

    private Camion toEntity(CamionDTO dto) {
        if (dto == null) return null;
        Camion camion = new Camion();
        camion.setId(dto.getId());
        camion.setPatente(dto.getPatente());
        if (dto.getCapacidad() != null) camion.setCapacidadCarga(dto.getCapacidad());
        return camion;
    }
}
