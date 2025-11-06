package com.backend.tpi.ms_gestion_calculos.services;

import com.backend.tpi.ms_gestion_calculos.dtos.TarifaVolumenPesoDTO;
import com.backend.tpi.ms_gestion_calculos.models.TarifaVolumenPeso;
import com.backend.tpi.ms_gestion_calculos.repositories.TarifaVolumenPesoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TarifaVolumenPesoService {

    @Autowired
    private TarifaVolumenPesoRepository repository;

    public List<TarifaVolumenPesoDTO> findAll() {
        return repository.findAll().stream().map(this::toDto).toList();
    }

    public TarifaVolumenPesoDTO findById(Long id) {
        Optional<TarifaVolumenPeso> opt = repository.findById(id);
        return opt.map(this::toDto).orElse(null);
    }

    public TarifaVolumenPesoDTO save(TarifaVolumenPesoDTO dto) {
        TarifaVolumenPeso e = toEntity(dto);
        TarifaVolumenPeso saved = repository.save(e);
        return toDto(saved);
    }

    public TarifaVolumenPesoDTO update(Long id, TarifaVolumenPesoDTO dto) {
        return repository.findById(id).map(existing -> {
            existing.setVolumenMax(dto.getVolumenMax());
            existing.setPesoMax(dto.getPesoMax());
            existing.setPrecio(dto.getPrecio());
            TarifaVolumenPeso saved = repository.save(existing);
            return toDto(saved);
        }).orElse(null);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    private TarifaVolumenPesoDTO toDto(TarifaVolumenPeso e) {
        if (e == null) return null;
        TarifaVolumenPesoDTO dto = new TarifaVolumenPesoDTO();
        dto.setId(e.getId());
        dto.setVolumenMax(e.getVolumenMax());
        dto.setPesoMax(e.getPesoMax());
        dto.setPrecio(e.getPrecio());
        return dto;
    }

    private TarifaVolumenPeso toEntity(TarifaVolumenPesoDTO dto) {
        if (dto == null) return null;
        TarifaVolumenPeso e = new TarifaVolumenPeso();
        e.setId(dto.getId());
        e.setVolumenMax(dto.getVolumenMax());
        e.setPesoMax(dto.getPesoMax());
        e.setPrecio(dto.getPrecio());
        return e;
    }
}
