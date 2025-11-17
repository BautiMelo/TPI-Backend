package com.backend.tpi.ms_rutas_transportistas.services;

import com.backend.tpi.ms_rutas_transportistas.dtos.CamionDTO;
import com.backend.tpi.ms_rutas_transportistas.models.Camion;
import com.backend.tpi.ms_rutas_transportistas.repositories.CamionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CamionService {

    private static final Logger logger = LoggerFactory.getLogger(CamionService.class);

    @Autowired
    private CamionRepository camionRepository;

    /**
     * Obtiene la lista de todos los camiones registrados en el sistema
     * @return Lista de camiones como DTOs
     */
    public List<CamionDTO> findAll() {
        logger.debug("Buscando todos los camiones");
        List<CamionDTO> camiones = camionRepository.findAll().stream()
                .map(this::toDto)
                .toList();
        logger.debug("Encontrados {} camiones", camiones.size());
        return camiones;
    }

    /**
     * Registra un nuevo camión en el sistema
     * @param dto Datos del camión a registrar
     * @return Camión registrado como DTO
     */
    public CamionDTO save(CamionDTO dto) {
        logger.info("Guardando nuevo camión con dominio: {}", dto.getDominio());
        Camion camion = toEntity(dto);
        Camion saved = camionRepository.save(camion);
        logger.info("Camión guardado exitosamente con ID: {}", saved.getId());
        return toDto(saved);
    }

    /**
     * Busca un camión por su dominio o patente
     * @param dominio Dominio o patente del camión a buscar
     * @return Camión encontrado como DTO
     * @throws RuntimeException si no se encuentra el camión
     */
    @Transactional(readOnly = true)
    public CamionDTO findByDominio(String dominio) {
        logger.debug("Buscando camión por dominio: {}", dominio);
        Camion camion = camionRepository.findAll().stream()
                .filter(c -> dominio.equals(c.getDominio()) || dominio.equals(c.getPatente()))
                .findFirst()
                .orElseThrow(() -> {
                    logger.error("Camión no encontrado con dominio: {}", dominio);
                    return new RuntimeException("Camión no encontrado con dominio: " + dominio);
                });
        logger.debug("Camión encontrado con dominio: {}", dominio);
        return toDto(camion);
    }

    /**
     * Actualiza el estado operativo de un camión (disponibilidad y actividad)
     * @param dominio Dominio o patente del camión
     * @param disponible Nuevo estado de disponibilidad (null para no modificar)
     * @param activo Nuevo estado de actividad (null para no modificar)
     * @return Camión actualizado como DTO
     * @throws RuntimeException si no se encuentra el camión
     */
    @Transactional
    public CamionDTO updateEstado(String dominio, Boolean disponible, Boolean activo) {
        logger.info("Actualizando estado de camión con dominio: {} - disponible: {}, activo: {}", dominio, disponible, activo);
        Camion camion = camionRepository.findAll().stream()
                .filter(c -> dominio.equals(c.getDominio()) || dominio.equals(c.getPatente()))
                .findFirst()
                .orElseThrow(() -> {
                    logger.error("Camión no encontrado con dominio: {}", dominio);
                    return new RuntimeException("Camión no encontrado con dominio: " + dominio);
                });
        
        if (disponible != null) {
            camion.setDisponible(disponible);
        }
        if (activo != null) {
            camion.setActivo(activo);
        }
        Camion saved = camionRepository.save(camion);
        logger.info("Estado del camión actualizado exitosamente - dominio: {}", dominio);
        return toDto(saved);
    }

    /**
     * Asigna un transportista a un camión específico
     * @param dominio Dominio o patente del camión
     * @param nombreTransportista Nombre del transportista a asignar
     * @return Camión con transportista asignado como DTO
     * @throws RuntimeException si no se encuentra el camión
     */
    @Transactional
    public CamionDTO asignarTransportista(String dominio, String nombreTransportista) {
        logger.info("Asignando transportista '{}' al camión con dominio: {}", nombreTransportista, dominio);
        Camion camion = camionRepository.findAll().stream()
                .filter(c -> dominio.equals(c.getDominio()) || dominio.equals(c.getPatente()))
                .findFirst()
                .orElseThrow(() -> {
                    logger.error("Camión no encontrado con dominio: {}", dominio);
                    return new RuntimeException("Camión no encontrado con dominio: " + dominio);
                });
        
        camion.setNombreTransportista(nombreTransportista);
        Camion saved = camionRepository.save(camion);
        logger.info("Transportista asignado exitosamente al camión con dominio: {}", dominio);
        return toDto(saved);
    }

    /**
     * Convierte una entidad Camion a DTO
     * @param camion Entidad a convertir
     * @return DTO con los datos del camión
     */
    private CamionDTO toDto(Camion camion) {
        if (camion == null) return null;
        CamionDTO dto = new CamionDTO();
        dto.setId(camion.getId());
        dto.setDominio(camion.getDominio());
        dto.setMarca(camion.getMarca());
        dto.setModelo(camion.getModelo());
        dto.setCapacidadPesoMax(camion.getCapacidadPesoMax());
        dto.setCapacidadVolumenMax(camion.getCapacidadVolumenMax());
        dto.setNombreTransportista(camion.getNombreTransportista());
        dto.setCostoBase(camion.getCostoBase());
        dto.setCostoPorKm(camion.getCostoPorKm());
        dto.setNumeroTransportistas(camion.getNumeroTransportistas());
        dto.setDisponible(camion.getDisponible());
        dto.setActivo(camion.getActivo());
        
        // Backward compatibility
        dto.setPatente(camion.getDominio() != null ? camion.getDominio() : camion.getPatente());
        dto.setCapacidad(camion.getCapacidadPesoMax() != null ? camion.getCapacidadPesoMax() : camion.getCapacidadCarga());
        dto.setEstado(camion.getDisponible() != null && camion.getDisponible() ? "disponible" : "ocupado");
        
        return dto;
    }

    /**
     * Convierte un DTO a entidad Camion
     * @param dto DTO a convertir
     * @return Entidad Camion
     */
    private Camion toEntity(CamionDTO dto) {
        if (dto == null) return null;
        Camion camion = new Camion();
        camion.setId(dto.getId());
        camion.setDominio(dto.getDominio() != null ? dto.getDominio() : dto.getPatente());
        camion.setMarca(dto.getMarca());
        camion.setModelo(dto.getModelo());
        camion.setCapacidadPesoMax(dto.getCapacidadPesoMax() != null ? dto.getCapacidadPesoMax() : dto.getCapacidad());
        camion.setCapacidadVolumenMax(dto.getCapacidadVolumenMax());
        camion.setNombreTransportista(dto.getNombreTransportista());
        camion.setCostoBase(dto.getCostoBase());
        camion.setCostoPorKm(dto.getCostoPorKm());
        camion.setNumeroTransportistas(dto.getNumeroTransportistas());
        camion.setDisponible(dto.getDisponible() != null ? dto.getDisponible() : true);
        camion.setActivo(dto.getActivo() != null ? dto.getActivo() : true);
        return camion;
    }
}
