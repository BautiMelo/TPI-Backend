package com.backend.tpi.ms_gestion_calculos.services;

import com.backend.tpi.ms_gestion_calculos.models.Tarifa;
import com.backend.tpi.ms_gestion_calculos.repositories.TarifaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TarifaService {

    @Autowired
    private TarifaRepository tarifaRepository;

    public List<Tarifa> findAll() {
        return tarifaRepository.findAll();
    }

    public Tarifa save(Tarifa tarifa) {
        return tarifaRepository.save(tarifa);
    }
}
