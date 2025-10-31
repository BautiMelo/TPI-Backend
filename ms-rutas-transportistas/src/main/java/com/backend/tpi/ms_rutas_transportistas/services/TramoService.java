package com.backend.tpi.ms_rutas_transportistas.services;

import com.backend.tpi.ms_rutas_transportistas.dtos.DistanciaResponseDTO;
import com.backend.tpi.ms_rutas_transportistas.dtos.TramoRequestDTO;
import com.backend.tpi.ms_rutas_transportistas.models.Ruta;
import com.backend.tpi.ms_rutas_transportistas.models.Tramo;
import com.backend.tpi.ms_rutas_transportistas.repositories.RutaRepository;
import com.backend.tpi.ms_rutas_transportistas.repositories.TramoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

@Service
public class TramoService {

    @Autowired
    private TramoRepository tramoRepository;

    @Autowired
    private RutaRepository rutaRepository;

    @Autowired
    private RestTemplate restTemplate;

    public Tramo create(TramoRequestDTO tramoRequestDTO) {
        Optional<Ruta> optionalRuta = rutaRepository.findById(tramoRequestDTO.getIdRuta());
        if (optionalRuta.isPresent()) {
            DistanciaResponseDTO distanciaResponse = restTemplate.postForObject(
                    "http://localhost:8083/calculos/distancia",
                    tramoRequestDTO,
                    DistanciaResponseDTO.class
            );

            Tramo tramo = new Tramo();
            tramo.setRuta(optionalRuta.get());
            tramo.setOrigen(String.valueOf(tramoRequestDTO.getOrigenDepositoId()));
            tramo.setDestino(String.valueOf(tramoRequestDTO.getDestinoDepositoId()));
            tramo.setDistancia(distanciaResponse.getDistancia());
            return tramoRepository.save(tramo);
        }
        return null;
    }

    public List<Tramo> findAll() {
        return tramoRepository.findAll();
    }

    public Tramo save(Tramo tramo) {
        return tramoRepository.save(tramo);
    }
}
