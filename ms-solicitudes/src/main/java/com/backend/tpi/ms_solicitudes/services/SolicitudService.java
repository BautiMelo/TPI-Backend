package com.backend.tpi.ms_solicitudes.services;

import com.backend.tpi.ms_solicitudes.dtos.CreateSolicitudDTO;
import com.backend.tpi.ms_solicitudes.dtos.SolicitudDTO;
import com.backend.tpi.ms_solicitudes.models.Solicitud;
import com.backend.tpi.ms_solicitudes.repositories.SolicitudRepository;
import com.backend.tpi.ms_solicitudes.repositories.EstadoSolicitudRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * Servicio de negocio para Solicitudes
 * Gestiona la lógica de solicitudes y se comunica con otros microservicios
 * (ms-gestion-calculos para precios y ms-rutas-transportistas para rutas)
 */
@Service
public class SolicitudService {

    private static final Logger logger = LoggerFactory.getLogger(SolicitudService.class);

    @Autowired
    private SolicitudRepository solicitudRepository;

    @Autowired
    private org.springframework.web.client.RestClient calculosClient;

    @Autowired
    private org.springframework.web.client.RestClient rutasClient;

    @Autowired
    private EstadoTransicionService estadoTransicionService;
    
    @Autowired
    private EstadoSolicitudRepository estadoSolicitudRepository;

    // Base URLs for other microservices (provide defaults for local/docker environment)
    @Value("${app.calculos.base-url:http://ms-gestion-calculos:8081}")
    private String calculosBaseUrl;

    @Value("${app.rutas.base-url:http://ms-rutas-transportistas:8082}")
    private String rutasBaseUrl;

    // Manual mapping - removed ModelMapper dependency

    /**
         * Crea una nueva solicitud de transporte
         * @param createSolicitudDTO Datos de la solicitud a crear
         * @return DTO con los datos de la solicitud creada
         * @throws IllegalArgumentException si los datos son inválidos
         */
        public SolicitudDTO create(CreateSolicitudDTO createSolicitudDTO) {
            // Validar datos de entrada
            if (createSolicitudDTO == null) {
                logger.error("CreateSolicitudDTO no puede ser null");
                throw new IllegalArgumentException("Los datos de la solicitud no pueden ser null");
            }
            if (createSolicitudDTO.getDireccionOrigen() == null || createSolicitudDTO.getDireccionOrigen().trim().isEmpty()) {
                logger.error("Dirección de origen no puede ser null o vacía");
                throw new IllegalArgumentException("La dirección de origen es obligatoria");
            }
            if (createSolicitudDTO.getDireccionDestino() == null || createSolicitudDTO.getDireccionDestino().trim().isEmpty()) {
                logger.error("Dirección de destino no puede ser null o vacía");
                throw new IllegalArgumentException("La dirección de destino es obligatoria");
            }
            if (createSolicitudDTO.getDireccionOrigen().equals(createSolicitudDTO.getDireccionDestino())) {
                logger.error("Dirección de origen y destino no pueden ser iguales");
                throw new IllegalArgumentException("La dirección de origen y destino deben ser diferentes");
            }
            
            logger.debug("Creando nueva solicitud - origen: {}, destino: {}", 
                createSolicitudDTO.getDireccionOrigen(), createSolicitudDTO.getDireccionDestino());
            Solicitud solicitud = new Solicitud();
            // Map fields from DTO to entity
            solicitud.setDireccionOrigen(createSolicitudDTO.getDireccionOrigen());
            solicitud.setDireccionDestino(createSolicitudDTO.getDireccionDestino());
            // other fields (clienteId, contenedorId, etc.) should be set elsewhere
            solicitud = solicitudRepository.save(solicitud);
            logger.info("Solicitud creada exitosamente con ID: {}", solicitud.getId());
            return toDto(solicitud);
        }

        /**
         * Obtiene todas las solicitudes sin filtros
         * @return Lista de DTOs con todas las solicitudes
         */
        @org.springframework.transaction.annotation.Transactional(readOnly = true)
        public List<SolicitudDTO> findAll() {
            return solicitudRepository.findAll().stream()
                    .map(this::toDto)
                    .collect(Collectors.toList());
        }

        /**
         * Busca solicitudes aplicando filtros opcionales por estado y/o clienteId
         * @param estado Nombre del estado a filtrar (opcional)
         * @param clienteId ID del cliente a filtrar (opcional)
         * @return Lista de solicitudes que cumplen los criterios
         */
        @org.springframework.transaction.annotation.Transactional(readOnly = true)
        public List<SolicitudDTO> findAllWithFilters(String estado, Long clienteId) {
            logger.debug("Buscando solicitudes con filtros - estado: {}, clienteId: {}", estado, clienteId);
            List<Solicitud> solicitudes;
        
            if (estado != null && !estado.isEmpty() && clienteId != null) {
                // Filtrar por ambos
                solicitudes = solicitudRepository.findByEstado_Nombre(estado).stream()
                        .filter(s -> clienteId.equals(s.getClienteId()))
                        .toList();
            } else if (estado != null && !estado.isEmpty()) {
                // Solo por estado
                solicitudes = solicitudRepository.findByEstado_Nombre(estado);
            } else if (clienteId != null) {
                // Solo por cliente
                solicitudes = solicitudRepository.findByClienteId(clienteId);
            } else {
                // Sin filtros
                solicitudes = solicitudRepository.findAll();
            }
        
            logger.debug("Encontradas {} solicitudes con los filtros aplicados", solicitudes.size());
            return solicitudes.stream()
                    .map(this::toDto)
                    .collect(Collectors.toList());
        }

        /**
         * Busca una solicitud por su ID
         * @param id ID de la solicitud
         * @return DTO de la solicitud encontrada, o null si no existe
         */
        @org.springframework.transaction.annotation.Transactional(readOnly = true)
        public SolicitudDTO findById(Long id) {
            logger.debug("Buscando solicitud por ID: {}", id);
            Optional<Solicitud> solicitud = solicitudRepository.findById(id);
            if (solicitud.isPresent()) {
                logger.debug("Solicitud encontrada con ID: {}", id);
            } else {
                logger.warn("Solicitud no encontrada con ID: {}", id);
            }
            return solicitud.map(this::toDto).orElse(null);
        }

        /**
         * Actualiza una solicitud existente
         * @param id ID de la solicitud a actualizar
         * @param createSolicitudDTO Nuevos datos de la solicitud
         * @return DTO de la solicitud actualizada, o null si no existe
         */
        @org.springframework.transaction.annotation.Transactional
        public SolicitudDTO update(Long id, CreateSolicitudDTO createSolicitudDTO) {
            logger.debug("Actualizando solicitud ID: {}", id);
            Optional<Solicitud> optionalSolicitud = solicitudRepository.findById(id);
            if (optionalSolicitud.isPresent()) {
                Solicitud solicitud = optionalSolicitud.get();
                // manual mapping of updatable fields
                solicitud.setDireccionOrigen(createSolicitudDTO.getDireccionOrigen());
                solicitud.setDireccionDestino(createSolicitudDTO.getDireccionDestino());
                solicitud = solicitudRepository.save(solicitud);
                logger.info("Solicitud ID: {} actualizada exitosamente", id);
                return toDto(solicitud);
            }
            logger.warn("No se pudo actualizar solicitud - ID no encontrado: {}", id);
            return null;
        }

        /**
         * Elimina una solicitud por su ID
         * @param id ID de la solicitud a eliminar
         */
        @org.springframework.transaction.annotation.Transactional
        public void delete(Long id) {
            logger.info("Eliminando solicitud ID: {}", id);
            solicitudRepository.deleteById(id);
            logger.debug("Solicitud ID: {} eliminada de la base de datos", id);
        }

        /**
         * Convierte una entidad Solicitud a su DTO
         * @param solicitud Entidad solicitud
         * @return DTO de la solicitud
         */
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

        // ----- Integration points (basic implementations) -----
        /**
         * Solicita una ruta al microservicio ms-rutas-transportistas para la solicitud indicada
         * @param solicitudId ID de la solicitud para la cual solicitar la ruta
         * @return Respuesta del microservicio de rutas
         */
        public Object requestRoute(Long solicitudId) {
            logger.info("Solicitando ruta para solicitud ID: {} al microservicio de rutas", solicitudId);
            Map<String, Object> body = new HashMap<>();
            body.put("idSolicitud", solicitudId);
            try {
                String token = extractBearerToken();
                // usar rutasClient (baseUrl ya configurada)
                ResponseEntity<Map<String, Object>> rutasResp = rutasClient.post()
                    .uri("/api/v1/rutas")
                    .headers(h -> { if (token != null) h.setBearerAuth(token); })
                    .body(body)
                    .retrieve()
                    .toEntity(new ParameterizedTypeReference<Map<String, Object>>() {});
                logger.info("Ruta solicitada exitosamente para solicitud ID: {}", solicitudId);
                return rutasResp != null ? rutasResp.getBody() : null;
            } catch (Exception e) {
                logger.error("Error al solicitar ruta para solicitud ID: {} - {}", solicitudId, e.getMessage());
                throw e;
            }
        }

        /**
         * Calcula el precio de una solicitud delegando al microservicio ms-gestion-calculos
         * Si falla la llamada remota, utiliza cálculo local como fallback
         * @param solicitudId ID de la solicitud
         * @return Objeto con información del costo calculado
         */
        public Object calculatePrice(Long solicitudId) {
            logger.info("Calculando precio para solicitud ID: {}", solicitudId);
            // Prefer delegar el cálculo al microservicio ms-gestion-calculos
            try {
                // delegar la llamada al cálculo remoto
                logger.debug("Llamando a ms-gestion-calculos para calcular precio de solicitud ID: {}", solicitudId);
                String token = extractBearerToken();
                ResponseEntity<com.backend.tpi.ms_solicitudes.dtos.CostoResponseDTO> costoResp = calculosClient.post()
                    .uri("/api/v1/precio/solicitud/" + solicitudId + "/costo")
                    .headers(h -> { if (token != null) h.setBearerAuth(token); })
                    .retrieve()
                    .toEntity(com.backend.tpi.ms_solicitudes.dtos.CostoResponseDTO.class);
                com.backend.tpi.ms_solicitudes.dtos.CostoResponseDTO resp = costoResp != null ? costoResp.getBody() : null;
                if (resp != null) {
                    logger.info("Precio calculado exitosamente para solicitud ID: {} - Costo total: {}", solicitudId, resp.getCostoTotal());
                    return resp;
                }
            } catch (Exception ex) {
                logger.warn("Falló la llamada remota para calcular precio de solicitud ID: {} - Usando cálculo local como fallback", solicitudId);
                // si falla la llamada remota, caeremos al cálculo local
            }


            Optional<Solicitud> optionalSolicitud = solicitudRepository.findById(solicitudId);
            if (optionalSolicitud.isEmpty()) {
                logger.error("No se puede calcular precio - Solicitud no encontrada con ID: {}", solicitudId);
                throw new IllegalArgumentException("Solicitud not found: " + solicitudId);
            }
            Solicitud solicitud = optionalSolicitud.get();

            // Call calculos to compute distance (via RestClient)
            // Prioridad: coordenadas > dirección de texto
            Map<String, String> distanciaReq = new HashMap<>();
            
            // Determinar origen
            String origen;
            if (solicitud.getOrigenLat() != null && solicitud.getOrigenLong() != null) {
                // Usar coordenadas en formato "lat,lon"
                origen = solicitud.getOrigenLat() + "," + solicitud.getOrigenLong();
                logger.debug("Usando coordenadas de origen: {}", origen);
            } else if (solicitud.getDireccionOrigen() != null) {
                // Fallback: usar dirección de texto (legacy - puede no funcionar)
                origen = solicitud.getDireccionOrigen();
                logger.warn("Solicitud {} sin coordenadas de origen - usando dirección texto (puede fallar): {}", 
                        solicitudId, origen);
            } else {
                logger.error("Solicitud {} no tiene ni coordenadas ni dirección de origen", solicitudId);
                throw new IllegalArgumentException("Solicitud no tiene información de origen");
            }
            
            // Determinar destino
            String destino;
            if (solicitud.getDestinoLat() != null && solicitud.getDestinoLong() != null) {
                // Usar coordenadas en formato "lat,lon"
                destino = solicitud.getDestinoLat() + "," + solicitud.getDestinoLong();
                logger.debug("Usando coordenadas de destino: {}", destino);
            } else if (solicitud.getDireccionDestino() != null) {
                // Fallback: usar dirección de texto (legacy - puede no funcionar)
                destino = solicitud.getDireccionDestino();
                logger.warn("Solicitud {} sin coordenadas de destino - usando dirección texto (puede fallar): {}", 
                        solicitudId, destino);
            } else {
                logger.error("Solicitud {} no tiene ni coordenadas ni dirección de destino", solicitudId);
                throw new IllegalArgumentException("Solicitud no tiene información de destino");
            }
            
            distanciaReq.put("origen", origen);
            distanciaReq.put("destino", destino);

            logger.debug("Calculando distancia para solicitud ID: {} - origen: {}, destino: {}", 
                    solicitudId, origen, destino);
            Map<String, Object> distanciaResp = null;
            String token = extractBearerToken();
            ResponseEntity<Map<String, Object>> distanciaEntity = calculosClient.post()
                .uri("/api/v1/gestion/distancia")
                .headers(h -> { if (token != null) h.setBearerAuth(token); })
                .body(distanciaReq)
                .retrieve()
                .toEntity(new ParameterizedTypeReference<Map<String, Object>>() {});
            distanciaResp = distanciaEntity != null ? distanciaEntity.getBody() : null;
            Double distancia = null;
            if (distanciaResp != null && distanciaResp.get("distancia") instanceof Number) {
                distancia = ((Number) distanciaResp.get("distancia")).doubleValue();
            }

            // Get tarifas from calculos service (via RestClient)
            List<Map<String, Object>> tarifas = null;
            ResponseEntity<java.util.List<java.util.Map<String, Object>>> tarifasEntity = calculosClient.get()
                .uri("/api/v1/tarifas")
                .headers(h -> { if (token != null) h.setBearerAuth(token); })
                .retrieve()
                .toEntity(new ParameterizedTypeReference<java.util.List<java.util.Map<String, Object>>>() {});
            tarifas = tarifasEntity != null ? tarifasEntity.getBody() : null;

            Double precioPorKm = null;
            if (tarifas != null && !tarifas.isEmpty()) {
                Object maybePrecio = tarifas.get(0).get("precioPorKm");
                if (maybePrecio instanceof Number) {
                    precioPorKm = ((Number) maybePrecio).doubleValue();
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("solicitudId", solicitudId);
            result.put("distancia", distancia);
            if (distancia != null && precioPorKm != null) {
                result.put("precio", distancia * precioPorKm);
                result.put("precioPorKm", precioPorKm);
            } else {
                result.put("precio", null);
                result.put("precioPorKm", precioPorKm);
            }
            return result;
        }

        /**
         * Asigna un transportista (camión) a una solicitud delegando al microservicio ms-rutas-transportistas
         * Pasos: 1) Busca la ruta por solicitudId, 2) Asigna el camión a un tramo de esa ruta
         * @param solicitudId ID de la solicitud
         * @param transportistaId ID del camión/transportista a asignar
         * @return Respuesta del microservicio de rutas
         */
        public Object assignTransport(Long solicitudId, Long transportistaId) {
            logger.info("Asignando transportista ID: {} a solicitud ID: {}", transportistaId, solicitudId);
            // 1) find route for solicitud
            logger.debug("Buscando ruta para solicitud ID: {}", solicitudId);
            Map<String, Object> ruta = null;
            String token = extractBearerToken();
            ResponseEntity<Map<String, Object>> rutaEntity = rutasClient.get()
                .uri("/api/v1/rutas/por-solicitud/{id}", solicitudId)
                .headers(h -> { if (token != null) h.setBearerAuth(token); })
                .retrieve()
                .toEntity(new ParameterizedTypeReference<Map<String, Object>>() {});
            ruta = rutaEntity != null ? rutaEntity.getBody() : null;
            if (ruta == null || ruta.get("id") == null) {
                logger.error("No se encontró ruta para solicitud ID: {}", solicitudId);
                throw new IllegalArgumentException("No ruta found for solicitud: " + solicitudId);
            }
            Object rutaIdObj = ruta.get("id");
            Long rutaId;
            if (rutaIdObj instanceof Number) rutaId = ((Number) rutaIdObj).longValue();
            else rutaId = Long.valueOf(rutaIdObj.toString());
            logger.debug("Ruta encontrada con ID: {} para solicitud ID: {}", rutaId, solicitudId);

            // 2) get tramos for the route and pick an unassigned tramo
            logger.debug("Buscando tramos para ruta ID: {}", rutaId);
            java.util.List<Map<String, Object>> tramos = null;
            ResponseEntity<java.util.List<java.util.Map<String, Object>>> tramosEntity = rutasClient.get()
                .uri("/api/v1/tramos/por-ruta/{id}", rutaId)
                .headers(h -> { if (token != null) h.setBearerAuth(token); })
                .retrieve()
                .toEntity(new ParameterizedTypeReference<java.util.List<java.util.Map<String, Object>>>() {});
            tramos = tramosEntity != null ? tramosEntity.getBody() : null;
            if (tramos == null || tramos.isEmpty()) {
                logger.error("No se encontraron tramos para ruta ID: {}", rutaId);
                throw new IllegalArgumentException("No tramos found for ruta: " + rutaId);
            }

            Long tramoIdToAssign = null;
            for (Map<String, Object> t : tramos) {
                Object camionDominio = t.get("camionDominio");
                if (camionDominio == null) {
                    Object idObj = t.get("id");
                    if (idObj instanceof Number) tramoIdToAssign = ((Number) idObj).longValue();
                    else tramoIdToAssign = Long.valueOf(idObj.toString());
                    logger.debug("Tramo sin asignar encontrado - ID: {}", tramoIdToAssign);
                    break;
                }
            }
            if (tramoIdToAssign == null) {
                // no unassigned tramo found; pick the first tramo
                Object idObj = tramos.get(0).get("id");
                if (idObj instanceof Number) tramoIdToAssign = ((Number) idObj).longValue();
                else tramoIdToAssign = Long.valueOf(idObj.toString());
                logger.debug("No hay tramos sin asignar, usando el primero - ID: {}", tramoIdToAssign);
            }

            // 3) call assign endpoint for the tramo
            logger.debug("Asignando transportista ID: {} al tramo ID: {}", transportistaId, tramoIdToAssign);
            ResponseEntity<Object> assignResp = rutasClient.post()
                .uri("/api/v1/tramos/" + tramoIdToAssign + "/asignar-transportista?camionId=" + transportistaId)
                .headers(h -> { if (token != null) h.setBearerAuth(token); })
                .retrieve()
                .toEntity(Object.class);
            logger.info("Transportista ID: {} asignado exitosamente a solicitud ID: {}", transportistaId, solicitudId);
            return assignResp != null ? assignResp.getBody() : null;
        }

        /**
         * Actualiza el estado de una solicitud con validación de transición
         * @param id ID de la solicitud
         * @param estadoId ID del nuevo estado
         * @return DTO de la solicitud actualizada
         * @throws IllegalStateException si la transición no es válida
         */
        @org.springframework.transaction.annotation.Transactional
        public SolicitudDTO updateEstado(Long id, Long estadoId) {
            logger.info("Actualizando estado de solicitud ID: {} a estado ID: {}", id, estadoId);
            Solicitud solicitud = solicitudRepository.findById(id)
                    .orElseThrow(() -> {
                        logger.error("No se puede actualizar estado - Solicitud no encontrada con ID: {}", id);
                        return new RuntimeException("Solicitud no encontrada con ID: " + id);
                    });
        
            // Obtener el estado destino
            com.backend.tpi.ms_solicitudes.models.EstadoSolicitud estadoDestino = estadoSolicitudRepository.findById(estadoId)
                .orElseThrow(() -> {
                    logger.error("Estado no encontrado con ID: {}", estadoId);
                    return new IllegalArgumentException("Estado no encontrado con ID: " + estadoId);
                });
            
            // Validar transición si hay estado actual
            if (solicitud.getEstado() != null) {
                String estadoOrigenNombre = solicitud.getEstado().getNombre();
                String estadoDestinoNombre = estadoDestino.getNombre();
                
                if (!estadoTransicionService.esTransicionSolicitudValida(estadoOrigenNombre, estadoDestinoNombre)) {
                    logger.error("Transición de estado inválida de {} a {}", estadoOrigenNombre, estadoDestinoNombre);
                    throw new IllegalStateException(
                        String.format("No se puede cambiar el estado de '%s' a '%s'. Transición no permitida.", 
                            estadoOrigenNombre, estadoDestinoNombre)
                    );
                }
                logger.debug("Transición válida de {} a {}", estadoOrigenNombre, estadoDestinoNombre);
            }
            
            solicitud.setEstado(estadoDestino);
        
            solicitud = solicitudRepository.save(solicitud);
            logger.info("Estado de solicitud ID: {} actualizado exitosamente", id);
            return toDto(solicitud);
        }

        /**
         * Programa una solicitud asignándole costo y tiempo estimados
         * @param id ID de la solicitud
         * @param costoEstimado Costo estimado del transporte
         * @param tiempoEstimado Tiempo estimado del transporte
         * @return DTO de la solicitud programada
         */
        @org.springframework.transaction.annotation.Transactional
        public SolicitudDTO programar(Long id, java.math.BigDecimal costoEstimado, java.math.BigDecimal tiempoEstimado) {
            logger.info("Programando solicitud ID: {} con costo: {} y tiempo: {}", id, costoEstimado, tiempoEstimado);
            Solicitud solicitud = solicitudRepository.findById(id)
                    .orElseThrow(() -> {
                        logger.error("No se puede programar - Solicitud no encontrada con ID: {}", id);
                        return new RuntimeException("Solicitud no encontrada con ID: " + id);
                    });
        
            solicitud.setCostoEstimado(costoEstimado);
            solicitud.setTiempoEstimado(tiempoEstimado);
        
        solicitud = solicitudRepository.save(solicitud);
        logger.info("Solicitud ID: {} programada exitosamente", id);
        return toDto(solicitud);
    }

    /**
     * Consulta los estados a los que puede transicionar una solicitud desde su estado actual
     * @param id ID de la solicitud
     * @return Lista de nombres de estados permitidos
     */
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<String> getEstadosPermitidos(Long id) {
        logger.info("Consultando estados permitidos para solicitud ID: {}", id);
        Solicitud solicitud = solicitudRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Solicitud no encontrada con ID: {}", id);
                    return new RuntimeException("Solicitud no encontrada con ID: " + id);
                });
        
        if (solicitud.getEstado() == null) {
            logger.warn("La solicitud ID: {} no tiene estado actual asignado", id);
            return List.of(); // Sin estado actual, no hay transiciones
        }
        
        String estadoActual = solicitud.getEstado().getNombre();
        List<String> permitidos = estadoTransicionService.getEstadosPermitidosSolicitud(estadoActual);
        logger.info("Solicitud ID: {} en estado '{}' puede transicionar a: {}", id, estadoActual, permitidos);
        return permitidos;
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