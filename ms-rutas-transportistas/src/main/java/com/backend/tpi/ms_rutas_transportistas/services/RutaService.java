package com.backend.tpi.ms_rutas_transportistas.services;

import com.backend.tpi.ms_rutas_transportistas.dtos.CreateRutaDTO;
import com.backend.tpi.ms_rutas_transportistas.dtos.RutaDTO;
import com.backend.tpi.ms_rutas_transportistas.models.Ruta;
import com.backend.tpi.ms_rutas_transportistas.repositories.RutaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RutaService {

    @Autowired
    private RutaRepository rutaRepository;

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
        throw new UnsupportedOperationException("assignTransportista not implemented yet");
    }

    /**
     * Find a route by solicitud id. Implementation pending.
     */
    public Object findBySolicitudId(Long solicitudId) {
        throw new UnsupportedOperationException("findBySolicitudId not implemented yet");
    }
}
