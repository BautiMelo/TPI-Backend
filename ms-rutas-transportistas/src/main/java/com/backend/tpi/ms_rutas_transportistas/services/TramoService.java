package com.backend.tpi.ms_rutas_transportistas.services;

import com.backend.tpi.ms_rutas_transportistas.dtos.DistanciaResponseDTO;
import com.backend.tpi.ms_rutas_transportistas.dtos.TramoRequestDTO;
import com.backend.tpi.ms_rutas_transportistas.models.Ruta;
import com.backend.tpi.ms_rutas_transportistas.models.Tramo;
import com.backend.tpi.ms_rutas_transportistas.repositories.RutaRepository;
import com.backend.tpi.ms_rutas_transportistas.repositories.TramoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

@Service
public class TramoService {

    @Autowired
    private TramoRepository tramoRepository;

    @Autowired
    private RutaRepository rutaRepository;

    @Autowired
    private org.springframework.web.client.RestClient calculosClient;
    
    @Autowired
    private com.backend.tpi.ms_rutas_transportistas.repositories.CamionRepository camionRepository;

    @org.springframework.beans.factory.annotation.Value("${app.calculos.base-url:http://ms-gestion-calculos:8081}")
    private String calculosBaseUrl;

    public com.backend.tpi.ms_rutas_transportistas.dtos.TramoDTO create(TramoRequestDTO tramoRequestDTO) {
        Optional<Ruta> optionalRuta = rutaRepository.findById(tramoRequestDTO.getIdRuta());
        if (optionalRuta.isPresent()) {
            // call calculos service using configured base-url
        java.util.Map<String, String> distanciaReq = new java.util.HashMap<>();
        distanciaReq.put("origen", String.valueOf(tramoRequestDTO.getOrigenDepositoId()));
        distanciaReq.put("destino", String.valueOf(tramoRequestDTO.getDestinoDepositoId()));
    ResponseEntity<DistanciaResponseDTO> distanciaRespEntity = calculosClient.post()
        .uri("/api/v1/gestion/distancia")
        .body(distanciaReq, new org.springframework.core.ParameterizedTypeReference<java.util.Map<String,String>>() {})
        .retrieve()
        .toEntity(DistanciaResponseDTO.class);
    DistanciaResponseDTO distanciaResponse = distanciaRespEntity != null ? distanciaRespEntity.getBody() : null;

            Tramo tramo = new Tramo();
            tramo.setRuta(optionalRuta.get());
            tramo.setOrigenDepositoId(tramoRequestDTO.getOrigenDepositoId());
            tramo.setDestinoDepositoId(tramoRequestDTO.getDestinoDepositoId());
            tramo.setOrigenLat(tramoRequestDTO.getOrigenLat());
            tramo.setOrigenLong(tramoRequestDTO.getOrigenLong());
            tramo.setDestinoLat(tramoRequestDTO.getDestinoLat());
            tramo.setDestinoLong(tramoRequestDTO.getDestinoLong());
            if (distanciaResponse != null) tramo.setDistancia(distanciaResponse.getDistancia());
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

    public java.util.List<com.backend.tpi.ms_rutas_transportistas.dtos.TramoDTO> findByRutaId(Long rutaId) {
        return tramoRepository.findByRutaId(rutaId).stream()
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
        dto.setOrigenDepositoId(tramo.getOrigenDepositoId());
        dto.setDestinoDepositoId(tramo.getDestinoDepositoId());
        dto.setOrigenLat(tramo.getOrigenLat());
        dto.setOrigenLong(tramo.getOrigenLong());
        dto.setDestinoLat(tramo.getDestinoLat());
        dto.setDestinoLong(tramo.getDestinoLong());
        dto.setDistancia(tramo.getDistancia());
        dto.setCamionDominio(tramo.getCamionDominio());
        dto.setCostoAproximado(tramo.getCostoAproximado());
        dto.setCostoReal(tramo.getCostoReal());
        dto.setFechaHoraInicioEstimada(tramo.getFechaHoraInicioEstimada());
        dto.setFechaHoraFinEstimada(tramo.getFechaHoraFinEstimada());
        dto.setFechaHoraInicioReal(tramo.getFechaHoraInicioReal());
        dto.setFechaHoraFinReal(tramo.getFechaHoraFinReal());
        return dto;
    }
    public com.backend.tpi.ms_rutas_transportistas.dtos.TramoDTO assignTransportista(Long tramoId, Long camionId) {
        Optional<Tramo> optionalTramo = tramoRepository.findById(tramoId);
        if (optionalTramo.isEmpty()) return null;
        Tramo tramo = optionalTramo.get();
        if (camionId != null) {
            Optional<com.backend.tpi.ms_rutas_transportistas.models.Camion> maybeCamion = camionRepository.findById(camionId);
            maybeCamion.ifPresent(c -> tramo.setCamionDominio(c.getPatente()));
        }
        Tramo saved = tramoRepository.save(tramo);
        return toDto(saved);
    }
}
