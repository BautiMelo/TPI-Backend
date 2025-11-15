package com.backend.tpi.ms_rutas_transportistas.services;

import com.backend.tpi.ms_rutas_transportistas.dtos.CamionDTO;
import com.backend.tpi.ms_rutas_transportistas.models.Camion;
import com.backend.tpi.ms_rutas_transportistas.repositories.CamionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional(readOnly = true)
    public CamionDTO findByDominio(String dominio) {
        Camion camion = camionRepository.findAll().stream()
                .filter(c -> dominio.equals(c.getDominio()) || dominio.equals(c.getPatente()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Camión no encontrado con dominio: " + dominio));
        return toDto(camion);
    }

    @Transactional
    public CamionDTO updateEstado(String dominio, Boolean disponible, Boolean activo) {
        Camion camion = camionRepository.findAll().stream()
                .filter(c -> dominio.equals(c.getDominio()) || dominio.equals(c.getPatente()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Camión no encontrado con dominio: " + dominio));
        
        if (disponible != null) {
            camion.setDisponible(disponible);
        }
        if (activo != null) {
            camion.setActivo(activo);
        }
        Camion saved = camionRepository.save(camion);
        return toDto(saved);
    }

    @Transactional
    public CamionDTO asignarTransportista(String dominio, String nombreTransportista) {
        Camion camion = camionRepository.findAll().stream()
                .filter(c -> dominio.equals(c.getDominio()) || dominio.equals(c.getPatente()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Camión no encontrado con dominio: " + dominio));
        
        camion.setNombreTransportista(nombreTransportista);
        Camion saved = camionRepository.save(camion);
        return toDto(saved);
    }

    private CamionDTO toDto(Camion camion) {
        if (camion == null) return null;
        CamionDTO dto = new CamionDTO();
        dto.setId(camion.getId());
        dto.setDominio(camion.getDominio());
        dto.setMarca(camion.getMarca());
        dto.setModelo(camion.getModelo());
        dto.setCapacidadPesoMax(camion.getCapacidadPesoMax());
        dto.setCapacidadVolumenMax(camion.getCapacidadVolumenMax());
        dto.setNombreTransportista(camion.getNombreTransportista());
        dto.setCostoBase(camion.getCostoBase());
        dto.setCostoPorKm(camion.getCostoPorKm());
        dto.setNumeroTransportistas(camion.getNumeroTransportistas());
        dto.setDisponible(camion.getDisponible());
        dto.setActivo(camion.getActivo());
        
        // Backward compatibility
        dto.setPatente(camion.getDominio() != null ? camion.getDominio() : camion.getPatente());
        dto.setCapacidad(camion.getCapacidadPesoMax() != null ? camion.getCapacidadPesoMax() : camion.getCapacidadCarga());
        dto.setEstado(camion.getDisponible() != null && camion.getDisponible() ? "disponible" : "ocupado");
        
        return dto;
    }

    private Camion toEntity(CamionDTO dto) {
        if (dto == null) return null;
        Camion camion = new Camion();
        camion.setId(dto.getId());
        camion.setDominio(dto.getDominio() != null ? dto.getDominio() : dto.getPatente());
        camion.setMarca(dto.getMarca());
        camion.setModelo(dto.getModelo());
        camion.setCapacidadPesoMax(dto.getCapacidadPesoMax() != null ? dto.getCapacidadPesoMax() : dto.getCapacidad());
        camion.setCapacidadVolumenMax(dto.getCapacidadVolumenMax());
        camion.setNombreTransportista(dto.getNombreTransportista());
        camion.setCostoBase(dto.getCostoBase());
        camion.setCostoPorKm(dto.getCostoPorKm());
        camion.setNumeroTransportistas(dto.getNumeroTransportistas());
        camion.setDisponible(dto.getDisponible() != null ? dto.getDisponible() : true);
        camion.setActivo(dto.getActivo() != null ? dto.getActivo() : true);
        return camion;
    }
}
