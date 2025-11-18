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
    
    @Autowired
    private org.springframework.web.client.RestClient solicitudesClient;

    @org.springframework.beans.factory.annotation.Value("${app.calculos.base-url:http://ms-gestion-calculos:8081}")
    private String calculosBaseUrl;

    /**
     * Crea un nuevo tramo para una ruta, calculando la distancia entre origen y destino
     * @param tramoRequestDTO Datos del tramo a crear
     * @return Tramo creado como DTO, o null si la ruta no existe
     * @throws IllegalArgumentException si los datos del tramo son inválidos
     */
    @org.springframework.transaction.annotation.Transactional
    public com.backend.tpi.ms_rutas_transportistas.dtos.TramoDTO create(TramoRequestDTO tramoRequestDTO) {
        // Validar datos de entrada
        if (tramoRequestDTO == null) {
            logger.error("TramoRequestDTO no puede ser null");
            throw new IllegalArgumentException("Los datos del tramo no pueden ser null");
        }
        if (tramoRequestDTO.getIdRuta() == null) {
            logger.error("IdRuta no puede ser null");
            throw new IllegalArgumentException("El ID de la ruta no puede ser null");
        }
        if (tramoRequestDTO.getOrigenDepositoId() == null || tramoRequestDTO.getDestinoDepositoId() == null) {
            logger.error("Depósitos origen/destino no pueden ser null");
            throw new IllegalArgumentException("Los depósitos de origen y destino son obligatorios");
        }
        if (tramoRequestDTO.getOrigenDepositoId().equals(tramoRequestDTO.getDestinoDepositoId())) {
            logger.error("Depósito origen y destino no pueden ser iguales");
            throw new IllegalArgumentException("El depósito de origen y destino deben ser diferentes");
        }
        // Validar coordenadas si están presentes
        if (tramoRequestDTO.getOrigenLat() != null && (tramoRequestDTO.getOrigenLat().compareTo(new java.math.BigDecimal("-90")) < 0 || tramoRequestDTO.getOrigenLat().compareTo(new java.math.BigDecimal("90")) > 0)) {
            logger.error("Latitud origen inválida: {}", tramoRequestDTO.getOrigenLat());
            throw new IllegalArgumentException("La latitud de origen debe estar entre -90 y 90");
        }
        if (tramoRequestDTO.getOrigenLong() != null && (tramoRequestDTO.getOrigenLong().compareTo(new java.math.BigDecimal("-180")) < 0 || tramoRequestDTO.getOrigenLong().compareTo(new java.math.BigDecimal("180")) > 0)) {
            logger.error("Longitud origen inválida: {}", tramoRequestDTO.getOrigenLong());
            throw new IllegalArgumentException("La longitud de origen debe estar entre -180 y 180");
        }
        if (tramoRequestDTO.getDestinoLat() != null && (tramoRequestDTO.getDestinoLat().compareTo(new java.math.BigDecimal("-90")) < 0 || tramoRequestDTO.getDestinoLat().compareTo(new java.math.BigDecimal("90")) > 0)) {
            logger.error("Latitud destino inválida: {}", tramoRequestDTO.getDestinoLat());
            throw new IllegalArgumentException("La latitud de destino debe estar entre -90 y 90");
        }
        if (tramoRequestDTO.getDestinoLong() != null && (tramoRequestDTO.getDestinoLong().compareTo(new java.math.BigDecimal("-180")) < 0 || tramoRequestDTO.getDestinoLong().compareTo(new java.math.BigDecimal("180")) > 0)) {
            logger.error("Longitud destino inválida: {}", tramoRequestDTO.getDestinoLong());
            throw new IllegalArgumentException("La longitud de destino debe estar entre -180 y 180");
        }
        
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
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
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
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
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
    @org.springframework.transaction.annotation.Transactional
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
        dto.setOrden(tramo.getOrden());
        dto.setGeneradoAutomaticamente(tramo.getGeneradoAutomaticamente());
        dto.setDuracionHoras(tramo.getDuracionHoras());
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
     * Asigna un camión a un tramo específico, validando que tenga capacidad suficiente
     * @param tramoId ID del tramo
     * @param camionId ID del camión a asignar
     * @return Tramo con camión asignado como DTO, o null si el tramo no existe
     * @throws IllegalArgumentException si el camión no tiene capacidad suficiente
     */
    public com.backend.tpi.ms_rutas_transportistas.dtos.TramoDTO assignTransportista(Long tramoId, Long camionId) {
        logger.info("Asignando camión ID: {} al tramo ID: {}", camionId, tramoId);
        
        // Validar que el tramo existe
        Optional<Tramo> optionalTramo = tramoRepository.findById(tramoId);
        if (optionalTramo.isEmpty()) {
            logger.warn("No se pudo asignar camión - Tramo no encontrado con ID: {}", tramoId);
            return null;
        }
        Tramo tramo = optionalTramo.get();
        
        // Validar que el camión existe
        if (camionId == null) {
            logger.warn("No se puede asignar camión - camionId es null");
            throw new IllegalArgumentException("El ID del camión no puede ser null");
        }
        
        Optional<com.backend.tpi.ms_rutas_transportistas.models.Camion> maybeCamion = camionRepository.findById(camionId);
        if (maybeCamion.isEmpty()) {
            logger.error("Camión no encontrado con ID: {}", camionId);
            throw new IllegalArgumentException("Camión no encontrado con ID: " + camionId);
        }
        
        com.backend.tpi.ms_rutas_transportistas.models.Camion camion = maybeCamion.get();
        
        // Validar disponibilidad del camión
        if (camion.getDisponible() != null && !camion.getDisponible()) {
            logger.error("El camión {} no está disponible", camion.getDominio());
            throw new IllegalArgumentException("El camión con dominio " + camion.getDominio() + " no está disponible");
        }
        
        if (camion.getActivo() != null && !camion.getActivo()) {
            logger.error("El camión {} no está activo", camion.getDominio());
            throw new IllegalArgumentException("El camión con dominio " + camion.getDominio() + " no está activo");
        }
        
        // Obtener datos del contenedor desde la solicitud
        try {
            logger.debug("Obteniendo datos del contenedor para validar capacidad del camión");
            Ruta ruta = tramo.getRuta();
            if (ruta == null || ruta.getIdSolicitud() == null) {
                logger.warn("No se puede validar capacidad - tramo sin ruta o solicitud asociada");
                // Permitir asignación sin validación si no hay solicitud
                asignarCamionSinValidacion(tramo, camion);
                Tramo saved = tramoRepository.save(tramo);
                logger.info("Camión asignado sin validación de capacidad al tramo ID: {}", tramoId);
                return toDto(saved);
            }
            
            Long solicitudId = ruta.getIdSolicitud();
            String token = extractBearerToken();
            
            // Consultar solicitud para obtener el contenedor
            ResponseEntity<java.util.Map<String, Object>> solicitudEntity = solicitudesClient.get()
                    .uri("/api/v1/solicitudes/{id}", solicitudId)
                    .headers(h -> { if (token != null) h.setBearerAuth(token); })
                    .retrieve()
                    .toEntity(new org.springframework.core.ParameterizedTypeReference<java.util.Map<String, Object>>() {});
            
            java.util.Map<String, Object> solicitud = solicitudEntity.getBody();
            if (solicitud == null || !solicitud.containsKey("contenedorId")) {
                logger.warn("No se puede validar capacidad - solicitud sin contenedor asociado");
                asignarCamionSinValidacion(tramo, camion);
                Tramo saved = tramoRepository.save(tramo);
                logger.info("Camión asignado sin validación de capacidad al tramo ID: {}", tramoId);
                return toDto(saved);
            }
            
            Long contenedorId = ((Number) solicitud.get("contenedorId")).longValue();
            
            // Consultar contenedor para obtener peso y volumen
            ResponseEntity<java.util.Map<String, Object>> contenedorEntity = solicitudesClient.get()
                    .uri("/api/v1/contenedores/{id}", contenedorId)
                    .headers(h -> { if (token != null) h.setBearerAuth(token); })
                    .retrieve()
                    .toEntity(new org.springframework.core.ParameterizedTypeReference<java.util.Map<String, Object>>() {});
            
            java.util.Map<String, Object> contenedor = contenedorEntity.getBody();
            if (contenedor == null) {
                logger.warn("No se puede validar capacidad - contenedor no encontrado");
                asignarCamionSinValidacion(tramo, camion);
                Tramo saved = tramoRepository.save(tramo);
                logger.info("Camión asignado sin validación de capacidad al tramo ID: {}", tramoId);
                return toDto(saved);
            }
            
            // Extraer peso y volumen del contenedor
            Double pesoCarga = contenedor.containsKey("peso") ? ((Number) contenedor.get("peso")).doubleValue() : null;
            Double volumenCarga = contenedor.containsKey("volumen") ? ((Number) contenedor.get("volumen")).doubleValue() : null;
            
            logger.debug("Contenedor: peso={} kg, volumen={} m³", pesoCarga, volumenCarga);
            logger.debug("Camión {}: capacidadPeso={} kg, capacidadVolumen={} m³", 
                    camion.getDominio(), camion.getCapacidadPesoMax(), camion.getCapacidadVolumenMax());
            
            // VALIDAR CAPACIDAD DE PESO
            if (pesoCarga != null && camion.getCapacidadPesoMax() != null) {
                if (pesoCarga > camion.getCapacidadPesoMax()) {
                    String mensaje = String.format(
                            "Camión insuficiente: el peso del contenedor (%.2f kg) excede la capacidad máxima del camión %s (%.2f kg)",
                            pesoCarga, camion.getDominio(), camion.getCapacidadPesoMax()
                    );
                    logger.error(mensaje);
                    throw new IllegalArgumentException(mensaje);
                }
            }
            
            // VALIDAR CAPACIDAD DE VOLUMEN
            if (volumenCarga != null && camion.getCapacidadVolumenMax() != null) {
                if (volumenCarga > camion.getCapacidadVolumenMax()) {
                    String mensaje = String.format(
                            "Camión insuficiente: el volumen del contenedor (%.2f m³) excede la capacidad máxima del camión %s (%.2f m³)",
                            volumenCarga, camion.getDominio(), camion.getCapacidadVolumenMax()
                    );
                    logger.error(mensaje);
                    throw new IllegalArgumentException(mensaje);
                }
            }
            
            logger.info("Validación de capacidad exitosa - Camión {} es compatible con la carga", camion.getDominio());
            
        } catch (IllegalArgumentException e) {
            // Re-lanzar excepciones de validación
            throw e;
        } catch (Exception e) {
            logger.warn("Error al validar capacidad del camión: {}. Asignando sin validación.", e.getMessage());
            logger.debug("Stack trace de error de validación:", e);
            // Si hay error al consultar servicios externos, permitir asignación sin validación
        }
        
        // Asignar camión al tramo
        asignarCamionSinValidacion(tramo, camion);
        Tramo saved = tramoRepository.save(tramo);
        logger.info("Camión {} asignado exitosamente al tramo ID: {}", camion.getDominio(), tramoId);
        return toDto(saved);
    }
    
    /**
     * Asigna el camión al tramo sin validaciones adicionales
     * @param tramo Tramo al que asignar el camión
     * @param camion Camión a asignar
     */
    private void asignarCamionSinValidacion(Tramo tramo, com.backend.tpi.ms_rutas_transportistas.models.Camion camion) {
        tramo.setCamionDominio(camion.getDominio());
        logger.debug("Camión con dominio {} asignado al tramo ID: {}", camion.getDominio(), tramo.getId());
    }

    /**
     * Marca el inicio de un tramo, registrando la fecha y hora actual
     * @param rutaId ID de la ruta
     * @param tramoId ID del tramo a iniciar
     * @return Tramo iniciado como DTO, o null si no existe
     * @throws RuntimeException si el tramo no pertenece a la ruta
     * @throws IllegalStateException si el tramo ya fue iniciado o no tiene camión asignado
     */
    @org.springframework.transaction.annotation.Transactional
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
        
        // Validar que el tramo tenga un camión asignado
        if (tramo.getCamionDominio() == null || tramo.getCamionDominio().isEmpty()) {
            logger.error("El tramo ID: {} no tiene camión asignado", tramoId);
            throw new IllegalStateException("No se puede iniciar el tramo sin un camión asignado");
        }
        
        // Validar que el tramo no haya sido iniciado previamente
        if (tramo.getFechaHoraInicioReal() != null) {
            logger.error("El tramo ID: {} ya fue iniciado el {}", tramoId, tramo.getFechaHoraInicioReal());
            throw new IllegalStateException("El tramo ya fue iniciado anteriormente");
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
     * @throws IllegalStateException si el tramo no fue iniciado o ya fue finalizado
     * @throws IllegalArgumentException si la fecha de fin es anterior a la de inicio
     */
    @org.springframework.transaction.annotation.Transactional
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
        
        // Validar que el tramo haya sido iniciado
        if (tramo.getFechaHoraInicioReal() == null) {
            logger.error("El tramo ID: {} no ha sido iniciado", tramoId);
            throw new IllegalStateException("No se puede finalizar un tramo que no ha sido iniciado");
        }
        
        // Validar que el tramo no haya sido finalizado previamente
        if (tramo.getFechaHoraFinReal() != null) {
            logger.error("El tramo ID: {} ya fue finalizado el {}", tramoId, tramo.getFechaHoraFinReal());
            throw new IllegalStateException("El tramo ya fue finalizado anteriormente");
        }
        
        // Validar que la fecha de fin sea posterior a la de inicio
        java.time.LocalDateTime fechaFin = fechaHora != null ? fechaHora : java.time.LocalDateTime.now();
        if (fechaFin.isBefore(tramo.getFechaHoraInicioReal())) {
            logger.error("La fecha de fin ({}) no puede ser anterior a la de inicio ({})", fechaFin, tramo.getFechaHoraInicioReal());
            throw new IllegalArgumentException("La fecha de finalización no puede ser anterior a la fecha de inicio");
        }
        
        tramo.setFechaHoraFinReal(fechaFin);
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
