package com.backend.tpi.ms_rutas_transportistas.services;

import com.backend.tpi.ms_rutas_transportistas.dtos.CreateRutaDTO;
import com.backend.tpi.ms_rutas_transportistas.dtos.RutaDTO;
import com.backend.tpi.ms_rutas_transportistas.models.Ruta;
import com.backend.tpi.ms_rutas_transportistas.repositories.RutaRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RutaService {

    @Autowired
    private RutaRepository rutaRepository;

    @Autowired
    private ModelMapper modelMapper;

    public RutaDTO create(CreateRutaDTO createRutaDTO) {
        Ruta ruta = modelMapper.map(createRutaDTO, Ruta.class);
        ruta = rutaRepository.save(ruta);
        return modelMapper.map(ruta, RutaDTO.class);
    }

    public List<RutaDTO> findAll() {
        return rutaRepository.findAll().stream()
                .map(ruta -> modelMapper.map(ruta, RutaDTO.class))
                .collect(Collectors.toList());
    }

    public RutaDTO findById(Long id) {
        Optional<Ruta> ruta = rutaRepository.findById(id);
        return ruta.map(value -> modelMapper.map(value, RutaDTO.class)).orElse(null);
    }

    public void delete(Long id) {
        rutaRepository.deleteById(id);
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
