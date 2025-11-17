package com.backend.tpi.ms_rutas_transportistas.services;

import com.backend.tpi.ms_rutas_transportistas.dtos.CreateRutaDTO;
import com.backend.tpi.ms_rutas_transportistas.dtos.RutaDTO;
import com.backend.tpi.ms_rutas_transportistas.models.Ruta;
import com.backend.tpi.ms_rutas_transportistas.repositories.RutaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Servicio de negocio para Rutas
 * Gestiona la creación de rutas y la asignación de transportistas a tramos
 */
@Service
public class RutaService {

    private static final Logger logger = LoggerFactory.getLogger(RutaService.class);

    @Autowired
    private RutaRepository rutaRepository;
    
    @Autowired
    private TramoService tramoService;

    // Manual mapping - ModelMapper removed

    /**
     * Crea una nueva ruta para una solicitud
     * @param createRutaDTO Datos de la ruta a crear (incluye idSolicitud)
     * @return DTO de la ruta creada
     */
    public RutaDTO create(CreateRutaDTO createRutaDTO) {
        logger.debug("Creando nueva ruta para solicitud ID: {}", createRutaDTO.getIdSolicitud());
        Ruta ruta = new Ruta();
        ruta.setIdSolicitud(createRutaDTO.getIdSolicitud());
        ruta = rutaRepository.save(ruta);
        logger.info("Ruta creada exitosamente con ID: {} para solicitud ID: {}", ruta.getId(), createRutaDTO.getIdSolicitud());
        return toDto(ruta);
    }

    /**
     * Obtiene todas las rutas del sistema
     * @return Lista de DTOs de rutas
     */
    public List<RutaDTO> findAll() {
        logger.debug("Buscando todas las rutas");
        List<RutaDTO> rutas = rutaRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        logger.debug("Encontradas {} rutas", rutas.size());
        return rutas;
    }

    /**
     * Busca una ruta por su ID
     * @param id ID de la ruta
     * @return DTO de la ruta encontrada, o null si no existe
     */
    public RutaDTO findById(Long id) {
        logger.debug("Buscando ruta por ID: {}", id);
        Optional<Ruta> ruta = rutaRepository.findById(id);
        if (ruta.isPresent()) {
            logger.debug("Ruta encontrada con ID: {}", id);
        } else {
            logger.warn("Ruta no encontrada con ID: {}", id);
        }
        return ruta.map(this::toDto).orElse(null);
    }

    /**
     * Elimina una ruta por su ID
     * @param id ID de la ruta a eliminar
     */
    public void delete(Long id) {
        logger.info("Eliminando ruta ID: {}", id);
        rutaRepository.deleteById(id);
        logger.debug("Ruta ID: {} eliminada de la base de datos", id);
    }

    /**
     * Convierte una entidad Ruta a su DTO
     * @param ruta Entidad ruta
     * @return DTO de la ruta
     */
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
     * Asigna un transportista (camión) a una ruta
     * Busca el primer tramo sin asignar y le asigna el transportista
     * @param rutaId ID de la ruta
     * @param transportistaId ID del camión a asignar
     * @return DTO del tramo con el transportista asignado
     */
    public Object assignTransportista(Long rutaId, Long transportistaId) {
        logger.info("Asignando transportista ID: {} a ruta ID: {}", transportistaId, rutaId);
        Optional<Ruta> optionalRuta = rutaRepository.findById(rutaId);
        if (optionalRuta.isEmpty()) {
            logger.error("No se puede asignar transportista - Ruta no encontrada con ID: {}", rutaId);
            throw new IllegalArgumentException("Ruta not found: " + rutaId);
        }
        // Find first unassigned tramo for this ruta and delegate assignment to TramoService
        logger.debug("Buscando tramos sin asignar para ruta ID: {}", rutaId);
        java.util.List<com.backend.tpi.ms_rutas_transportistas.dtos.TramoDTO> tramos = tramoService.findByRutaId(rutaId);
        if (tramos == null || tramos.isEmpty()) {
            logger.warn("No se encontraron tramos para ruta ID: {}", rutaId);
            Map<String, Object> result = new HashMap<>();
            result.put("rutaId", rutaId);
            result.put("status", "no_tramos");
            return result;
        }
        for (com.backend.tpi.ms_rutas_transportistas.dtos.TramoDTO t : tramos) {
            if (t.getCamionDominio() == null || t.getCamionDominio().isEmpty()) {
                logger.debug("Tramo sin asignar encontrado - ID: {}, asignando transportista", t.getId());
                // delegate to TramoService to assign the camion and persist
                com.backend.tpi.ms_rutas_transportistas.dtos.TramoDTO assigned = tramoService.assignTransportista(t.getId(), transportistaId);
                logger.info("Transportista ID: {} asignado exitosamente a ruta ID: {}", transportistaId, rutaId);
                return assigned != null ? assigned : java.util.Collections.emptyMap();
            }
        }
        logger.info("Todos los tramos de la ruta ID: {} ya tienen transportista asignado", rutaId);
        Map<String, Object> result = new HashMap<>();
        result.put("rutaId", rutaId);
        result.put("status", "all_tramos_assigned");
        return result;
    }

    /**
     * Busca una ruta por el ID de la solicitud asociada
     * @param solicitudId ID de la solicitud
     * @return DTO de la ruta encontrada, o null si no existe
     */
    public Object findBySolicitudId(Long solicitudId) {
        logger.debug("Buscando ruta por solicitud ID: {}", solicitudId);
        Optional<Ruta> ruta = rutaRepository.findByIdSolicitud(solicitudId);
        if (ruta.isPresent()) {
            logger.debug("Ruta encontrada para solicitud ID: {} - ruta ID: {}", solicitudId, ruta.get().getId());
        } else {
            logger.warn("No se encontró ruta para solicitud ID: {}", solicitudId);
        }
        return ruta.map(this::toDto).orElse(null);
    }
}
