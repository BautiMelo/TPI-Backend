package com.backend.tpi.ms_solicitudes.services;

import com.backend.tpi.ms_solicitudes.dtos.CreateSolicitudDTO;
import com.backend.tpi.ms_solicitudes.dtos.SolicitudDTO;
import com.backend.tpi.ms_solicitudes.models.Solicitud;
import com.backend.tpi.ms_solicitudes.repositories.SolicitudRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SolicitudService {

    @Autowired
    private SolicitudRepository solicitudRepository;

    @Autowired
    private ModelMapper modelMapper;

    public SolicitudDTO create(CreateSolicitudDTO createSolicitudDTO) {
        Solicitud solicitud = modelMapper.map(createSolicitudDTO, Solicitud.class);
        solicitud = solicitudRepository.save(solicitud);
        return modelMapper.map(solicitud, SolicitudDTO.class);
    }

    public List<SolicitudDTO> findAll() {
        return solicitudRepository.findAll().stream()
                .map(solicitud -> modelMapper.map(solicitud, SolicitudDTO.class))
                .collect(Collectors.toList());
    }

    public SolicitudDTO findById(Long id) {
        Optional<Solicitud> solicitud = solicitudRepository.findById(id);
        return solicitud.map(value -> modelMapper.map(value, SolicitudDTO.class)).orElse(null);
    }

    public SolicitudDTO update(Long id, CreateSolicitudDTO createSolicitudDTO) {
        Optional<Solicitud> optionalSolicitud = solicitudRepository.findById(id);
        if (optionalSolicitud.isPresent()) {
            Solicitud solicitud = optionalSolicitud.get();
            modelMapper.map(createSolicitudDTO, solicitud);
            solicitud = solicitudRepository.save(solicitud);
            return modelMapper.map(solicitud, SolicitudDTO.class);
        }
        return null;
    }

    public void delete(Long id) {
        solicitudRepository.deleteById(id);
    }
}
