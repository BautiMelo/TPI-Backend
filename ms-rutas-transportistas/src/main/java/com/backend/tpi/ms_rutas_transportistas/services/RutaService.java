package com.backend.tpi.ms_rutas_transportistas.services;

import com.backend.tpi.ms_rutas_transportistas.dtos.CreateRutaDTO;
import com.backend.tpi.ms_rutas_transportistas.dtos.RutaDTO;
import com.backend.tpi.ms_rutas_transportistas.models.Ruta;
import com.backend.tpi.ms_rutas_transportistas.repositories.RutaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RutaService {

    @Autowired
    private RutaRepository rutaRepository;
    
    @Autowired
    private TramoService tramoService;

    // Manual mapping - ModelMapper removed

    public RutaDTO create(CreateRutaDTO createRutaDTO) {
        Ruta ruta = new Ruta();
        ruta.setIdSolicitud(createRutaDTO.getIdSolicitud());
        ruta = rutaRepository.save(ruta);
        return toDto(ruta);
    }

    public List<RutaDTO> findAll() {
        return rutaRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public RutaDTO findById(Long id) {
        Optional<Ruta> ruta = rutaRepository.findById(id);
        return ruta.map(this::toDto).orElse(null);
    }

    public void delete(Long id) {
        rutaRepository.deleteById(id);
    }

    private RutaDTO toDto(Ruta ruta) {
        if (ruta == null) return null;
        RutaDTO dto = new RutaDTO();
        dto.setId(ruta.getId());
        dto.setIdSolicitud(ruta.getIdSolicitud());
        dto.setFechaCreacion(ruta.getFechaCreacion());
        return dto;
    }

    // ----- Integration/stub methods -----
    /**
     * Assign a transportista (or camion) to a ruta. Implementation pending.
     */
    public Object assignTransportista(Long rutaId, Long transportistaId) {
        Optional<Ruta> optionalRuta = rutaRepository.findById(rutaId);
        if (optionalRuta.isEmpty()) {
            throw new IllegalArgumentException("Ruta not found: " + rutaId);
        }
        // Find first unassigned tramo for this ruta and delegate assignment to TramoService
        java.util.List<com.backend.tpi.ms_rutas_transportistas.dtos.TramoDTO> tramos = tramoService.findByRutaId(rutaId);
        if (tramos == null || tramos.isEmpty()) {
            Map<String, Object> result = new HashMap<>();
            result.put("rutaId", rutaId);
            result.put("status", "no_tramos");
            return result;
        }
        for (com.backend.tpi.ms_rutas_transportistas.dtos.TramoDTO t : tramos) {
            if (t.getCamionDominio() == null || t.getCamionDominio().isEmpty()) {
                // delegate to TramoService to assign the camion and persist
                com.backend.tpi.ms_rutas_transportistas.dtos.TramoDTO assigned = tramoService.assignTransportista(t.getId(), transportistaId);
                return assigned != null ? assigned : java.util.Collections.emptyMap();
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("rutaId", rutaId);
        result.put("status", "all_tramos_assigned");
        return result;
    }

    /**
     * Find a route by solicitud id. Implementation pending.
     */
    public Object findBySolicitudId(Long solicitudId) {
        Optional<Ruta> ruta = rutaRepository.findByIdSolicitud(solicitudId);
        return ruta.map(this::toDto).orElse(null);
    }
}
