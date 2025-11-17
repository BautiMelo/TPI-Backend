package com.backend.tpi.ms_rutas_transportistas.services;

import com.backend.tpi.ms_rutas_transportistas.dtos.DistanciaResponseDTO;
import com.backend.tpi.ms_rutas_transportistas.dtos.TramoRequestDTO;
import com.backend.tpi.ms_rutas_transportistas.models.Ruta;
import com.backend.tpi.ms_rutas_transportistas.models.Tramo;
import com.backend.tpi.ms_rutas_transportistas.repositories.RutaRepository;
import com.backend.tpi.ms_rutas_transportistas.repositories.TramoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.List;
import java.util.Optional;

/**
 * Servicio de negocio para Tramos
 * Gestiona la creación de tramos, cálculo de distancias y asignación de camiones
 * Se comunica con ms-gestion-calculos para obtener distancias
 */
@Service
public class TramoService {

    private static final Logger logger = LoggerFactory.getLogger(TramoService.class);

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

    /**
     * Crea un nuevo tramo para una ruta, calculando la distancia entre origen y destino
     * @param tramoRequestDTO Datos del tramo a crear
     * @return Tramo creado como DTO, o null si la ruta no existe
     */
    public com.backend.tpi.ms_rutas_transportistas.dtos.TramoDTO create(TramoRequestDTO tramoRequestDTO) {
        logger.info("Creando nuevo tramo para ruta ID: {}", tramoRequestDTO.getIdRuta());
        Optional<Ruta> optionalRuta = rutaRepository.findById(tramoRequestDTO.getIdRuta());
        if (optionalRuta.isPresent()) {
            logger.debug("Calculando distancia del tramo entre depósito origen {} y destino {}", 
                tramoRequestDTO.getOrigenDepositoId(), tramoRequestDTO.getDestinoDepositoId());
            // call calculos service using configured base-url
        java.util.Map<String, String> distanciaReq = new java.util.HashMap<>();
        distanciaReq.put("origen", String.valueOf(tramoRequestDTO.getOrigenDepositoId()));
        distanciaReq.put("destino", String.valueOf(tramoRequestDTO.getDestinoDepositoId()));
    String token = extractBearerToken();
    ResponseEntity<DistanciaResponseDTO> distanciaRespEntity = calculosClient.post()
        .uri("/api/v1/gestion/distancia")
        .headers(h -> { if (token != null) h.setBearerAuth(token); })
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
            if (distanciaResponse != null) {
                tramo.setDistancia(distanciaResponse.getDistancia());
                logger.debug("Distancia calculada para tramo: {} km", distanciaResponse.getDistancia());
            }
            Tramo saved = tramoRepository.save(tramo);
            logger.info("Tramo creado exitosamente con ID: {}", saved.getId());
            return toDto(saved);
        }
        logger.warn("No se pudo crear tramo - Ruta no encontrada con ID: {}", tramoRequestDTO.getIdRuta());
        return null;
    }

    /**
     * Obtiene la lista de todos los tramos registrados
     * @return Lista de tramos como DTOs
     */
    public java.util.List<com.backend.tpi.ms_rutas_transportistas.dtos.TramoDTO> findAll() {
        return tramoRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * Obtiene todos los tramos de una ruta específica
     * @param rutaId ID de la ruta
     * @return Lista de tramos de la ruta
     */
    public java.util.List<com.backend.tpi.ms_rutas_transportistas.dtos.TramoDTO> findByRutaId(Long rutaId) {
        return tramoRepository.findByRutaId(rutaId).stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * Guarda o actualiza un tramo
     * @param tramo Entidad tramo a guardar
     * @return Tramo guardado como DTO
     */
    public com.backend.tpi.ms_rutas_transportistas.dtos.TramoDTO save(Tramo tramo) {
        Tramo saved = tramoRepository.save(tramo);
        return toDto(saved);
    }

    /**
     * Convierte una entidad Tramo a DTO
     * @param tramo Entidad a convertir
     * @return DTO con los datos del tramo
     */
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
    
    /**
     * Asigna un camión a un tramo específico
     * @param tramoId ID del tramo
     * @param camionId ID del camión a asignar
     * @return Tramo con camión asignado como DTO, o null si el tramo no existe
     */
    public com.backend.tpi.ms_rutas_transportistas.dtos.TramoDTO assignTransportista(Long tramoId, Long camionId) {
        logger.info("Asignando camión ID: {} al tramo ID: {}", camionId, tramoId);
        Optional<Tramo> optionalTramo = tramoRepository.findById(tramoId);
        if (optionalTramo.isEmpty()) {
            logger.warn("No se pudo asignar camión - Tramo no encontrado con ID: {}", tramoId);
            return null;
        }
        Tramo tramo = optionalTramo.get();
        if (camionId != null) {
            Optional<com.backend.tpi.ms_rutas_transportistas.models.Camion> maybeCamion = camionRepository.findById(camionId);
            maybeCamion.ifPresent(c -> {
                tramo.setCamionDominio(c.getPatente());
                logger.debug("Camión con dominio {} asignado al tramo ID: {}", c.getPatente(), tramoId);
            });
        }
        Tramo saved = tramoRepository.save(tramo);
        logger.info("Camión asignado exitosamente al tramo ID: {}", tramoId);
        return toDto(saved);
    }

    /**
     * Marca el inicio de un tramo, registrando la fecha y hora actual
     * @param rutaId ID de la ruta
     * @param tramoId ID del tramo a iniciar
     * @return Tramo iniciado como DTO, o null si no existe
     * @throws RuntimeException si el tramo no pertenece a la ruta
     */
    public com.backend.tpi.ms_rutas_transportistas.dtos.TramoDTO iniciarTramo(Long rutaId, Long tramoId) {
        logger.info("Iniciando tramo ID: {} de ruta ID: {}", tramoId, rutaId);
        Optional<Tramo> optionalTramo = tramoRepository.findById(tramoId);
        if (optionalTramo.isEmpty()) {
            logger.warn("No se pudo iniciar - Tramo no encontrado con ID: {}", tramoId);
            return null;
        }
        
        Tramo tramo = optionalTramo.get();
        if (tramo.getRuta() == null || !tramo.getRuta().getId().equals(rutaId)) {
            logger.error("El tramo ID: {} no pertenece a la ruta ID: {}", tramoId, rutaId);
            throw new RuntimeException("El tramo no pertenece a la ruta especificada");
        }
        
        tramo.setFechaHoraInicioReal(java.time.LocalDateTime.now());
        Tramo saved = tramoRepository.save(tramo);
        logger.info("Tramo ID: {} iniciado exitosamente a las {}", tramoId, saved.getFechaHoraInicioReal());
        return toDto(saved);
    }

    /**
     * Marca la finalización de un tramo, registrando la fecha y hora
     * @param rutaId ID de la ruta
     * @param tramoId ID del tramo a finalizar
     * @param fechaHora Fecha y hora de finalización (null para usar la actual)
     * @return Tramo finalizado como DTO, o null si no existe
     * @throws RuntimeException si el tramo no pertenece a la ruta
     */
    public com.backend.tpi.ms_rutas_transportistas.dtos.TramoDTO finalizarTramo(Long rutaId, Long tramoId, java.time.LocalDateTime fechaHora) {
        logger.info("Finalizando tramo ID: {} de ruta ID: {}", tramoId, rutaId);
        Optional<Tramo> optionalTramo = tramoRepository.findById(tramoId);
        if (optionalTramo.isEmpty()) {
            logger.warn("No se pudo finalizar - Tramo no encontrado con ID: {}", tramoId);
            return null;
        }
        
        Tramo tramo = optionalTramo.get();
        if (tramo.getRuta() == null || !tramo.getRuta().getId().equals(rutaId)) {
            logger.error("El tramo ID: {} no pertenece a la ruta ID: {}", tramoId, rutaId);
            throw new RuntimeException("El tramo no pertenece a la ruta especificada");
        }
        
        tramo.setFechaHoraFinReal(fechaHora != null ? fechaHora : java.time.LocalDateTime.now());
        Tramo saved = tramoRepository.save(tramo);
        logger.info("Tramo ID: {} finalizado exitosamente a las {}", tramoId, saved.getFechaHoraFinReal());
        return toDto(saved);
    }

    /**
     * Helper: extrae token Bearer del SecurityContext si existe
     */
    private String extractBearerToken() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken) {
            return ((JwtAuthenticationToken) auth).getToken().getTokenValue();
        }
        return null;
    }
}
