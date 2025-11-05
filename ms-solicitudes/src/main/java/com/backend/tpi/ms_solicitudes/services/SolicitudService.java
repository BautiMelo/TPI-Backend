package com.backend.tpi.ms_solicitudes.services;

import com.backend.tpi.ms_solicitudes.dtos.CreateSolicitudDTO;
import com.backend.tpi.ms_solicitudes.dtos.SolicitudDTO;
import com.backend.tpi.ms_solicitudes.models.Solicitud;
import com.backend.tpi.ms_solicitudes.repositories.SolicitudRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SolicitudService {

    @Autowired
    private SolicitudRepository solicitudRepository;

    // Manual mapping - removed ModelMapper dependency

    public SolicitudDTO create(CreateSolicitudDTO createSolicitudDTO) {
        Solicitud solicitud = new Solicitud();
        // Map fields from DTO to entity
        solicitud.setDireccionOrigen(createSolicitudDTO.getDireccionOrigen());
        solicitud.setDireccionDestino(createSolicitudDTO.getDireccionDestino());
        // other fields (clienteId, contenedorId, etc.) should be set elsewhere
        solicitud = solicitudRepository.save(solicitud);
        return toDto(solicitud);
    }

    public List<SolicitudDTO> findAll() {
        return solicitudRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public SolicitudDTO findById(Long id) {
        Optional<Solicitud> solicitud = solicitudRepository.findById(id);
        return solicitud.map(this::toDto).orElse(null);
    }

    public SolicitudDTO update(Long id, CreateSolicitudDTO createSolicitudDTO) {
        Optional<Solicitud> optionalSolicitud = solicitudRepository.findById(id);
        if (optionalSolicitud.isPresent()) {
            Solicitud solicitud = optionalSolicitud.get();
            // manual mapping of updatable fields
            solicitud.setDireccionOrigen(createSolicitudDTO.getDireccionOrigen());
            solicitud.setDireccionDestino(createSolicitudDTO.getDireccionDestino());
            solicitud = solicitudRepository.save(solicitud);
            return toDto(solicitud);
        }
        return null;
    }

    public void delete(Long id) {
        solicitudRepository.deleteById(id);
    }

    // Helper: map entity -> DTO
    private SolicitudDTO toDto(Solicitud solicitud) {
        if (solicitud == null) return null;
        SolicitudDTO dto = new SolicitudDTO();
        dto.setId(solicitud.getId());
        dto.setDireccionOrigen(solicitud.getDireccionOrigen());
        dto.setDireccionDestino(solicitud.getDireccionDestino());
        // estado may be null
        if (solicitud.getEstado() != null) dto.setEstado(solicitud.getEstado().getNombre());
        // other fields (fechaCreacion, etc.) are not present on entity; left null
        return dto;
    }

    // ----- Integration points (stubs) -----
    /**
     * Request a route for the given solicitud. Should call ms-rutas-transportistas via RestClient.
     * Currently a stub: implementation pending.
     */
    public Object requestRoute(Long solicitudId) {
        throw new UnsupportedOperationException("requestRoute not implemented yet");
    }

    /**
     * Calculate price for the given solicitud. Should call ms-gestion-calculos via RestClient.
     * Currently a stub: implementation pending.
     */
    public Object calculatePrice(Long solicitudId) {
        throw new UnsupportedOperationException("calculatePrice not implemented yet");
    }

    /**
     * Assign a transport (transportista/camion) to the solicitud. Implementation pending.
     */
    public Object assignTransport(Long solicitudId, Long transportistaId) {
        throw new UnsupportedOperationException("assignTransport not implemented yet");
    }
}
