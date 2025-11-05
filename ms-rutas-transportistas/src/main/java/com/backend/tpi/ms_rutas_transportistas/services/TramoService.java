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

    public com.backend.tpi.ms_rutas_transportistas.dtos.TramoDTO create(TramoRequestDTO tramoRequestDTO) {
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
            Tramo saved = tramoRepository.save(tramo);
            return toDto(saved);
        }
        return null;
    }

    public java.util.List<com.backend.tpi.ms_rutas_transportistas.dtos.TramoDTO> findAll() {
        return tramoRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    public com.backend.tpi.ms_rutas_transportistas.dtos.TramoDTO save(Tramo tramo) {
        Tramo saved = tramoRepository.save(tramo);
        return toDto(saved);
    }

    private com.backend.tpi.ms_rutas_transportistas.dtos.TramoDTO toDto(Tramo tramo) {
        if (tramo == null) return null;
        com.backend.tpi.ms_rutas_transportistas.dtos.TramoDTO dto = new com.backend.tpi.ms_rutas_transportistas.dtos.TramoDTO();
        dto.setId(tramo.getId());
        if (tramo.getRuta() != null) dto.setIdRuta(tramo.getRuta().getId());
        try {
            dto.setOrigenDepositoId(Long.valueOf(tramo.getOrigen()));
        } catch (Exception ignored) {}
        try {
            dto.setDestinoDepositoId(Long.valueOf(tramo.getDestino()));
        } catch (Exception ignored) {}
        dto.setDistancia(tramo.getDistancia());
        return dto;
    }
}
