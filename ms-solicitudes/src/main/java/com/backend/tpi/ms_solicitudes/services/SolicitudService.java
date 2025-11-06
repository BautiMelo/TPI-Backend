package com.backend.tpi.ms_solicitudes.services;

import com.backend.tpi.ms_solicitudes.dtos.CreateSolicitudDTO;
import com.backend.tpi.ms_solicitudes.dtos.SolicitudDTO;
import com.backend.tpi.ms_solicitudes.models.Solicitud;
import com.backend.tpi.ms_solicitudes.repositories.SolicitudRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.core.ParameterizedTypeReference;

@Service
public class SolicitudService {

    @Autowired
    private SolicitudRepository solicitudRepository;

    @Autowired
    private org.springframework.web.client.RestClient calculosClient;

    @Autowired
    private org.springframework.web.client.RestClient rutasClient;

    // Base URLs for other microservices (provide defaults for local/docker environment)
    @Value("${app.calculos.base-url:http://ms-gestion-calculos:8081}")
    private String calculosBaseUrl;

    @Value("${app.rutas.base-url:http://ms-rutas-transportistas:8082}")
    private String rutasBaseUrl;

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

    // ----- Integration points (basic implementations) -----
    /**
     * Request a route for the given solicitud by calling ms-rutas-transportistas.
     */
    public Object requestRoute(Long solicitudId) {
    Map<String, Object> body = new HashMap<>();
    body.put("idSolicitud", solicitudId);
    // usar rutasClient (baseUrl ya configurada)
    ResponseEntity<Map<String, Object>> rutasResp = rutasClient.post()
        .uri("/api/v1/rutas")
        .body(body, new ParameterizedTypeReference<Map<String, Object>>() {})
        .retrieve()
        .toEntity(new ParameterizedTypeReference<Map<String, Object>>() {});
    return rutasResp != null ? rutasResp.getBody() : null;
    }

    /**
     * Calculate price for the given solicitud by calling ms-gestion-calculos for distance
     * and tarifa endpoints and computing a simple price = distancia * precioPorKm.
     */
    @SuppressWarnings("unchecked")
    public Object calculatePrice(Long solicitudId) {
        // Prefer delegar el cálculo al microservicio ms-gestion-calculos
        try {
            // delegar la llamada al cálculo remoto
        ResponseEntity<com.backend.tpi.ms_solicitudes.dtos.CostoResponseDTO> costoResp = calculosClient.post()
            .uri("/api/v1/precio/solicitud/" + solicitudId + "/costo")
            .retrieve()
            .toEntity(com.backend.tpi.ms_solicitudes.dtos.CostoResponseDTO.class);
        com.backend.tpi.ms_solicitudes.dtos.CostoResponseDTO resp = costoResp != null ? costoResp.getBody() : null;
        if (resp != null) return resp;
        } catch (Exception ex) {
            // si falla la llamada remota, caeremos al cálculo local
        }

        // Fallback: cálculo local (mantener compatibilidad)
        Optional<Solicitud> optionalSolicitud = solicitudRepository.findById(solicitudId);
        if (optionalSolicitud.isEmpty()) {
            throw new IllegalArgumentException("Solicitud not found: " + solicitudId);
        }
        Solicitud solicitud = optionalSolicitud.get();

        // Call calculos to compute distance (via RestClient)
        Map<String, String> distanciaReq = new HashMap<>();
        distanciaReq.put("origen", solicitud.getDireccionOrigen());
        distanciaReq.put("destino", solicitud.getDireccionDestino());

        Map<String, Object> distanciaResp = null;
    ResponseEntity<Map<String, Object>> distanciaEntity = calculosClient.post()
        .uri("/api/v1/gestion/distancia")
        .body(distanciaReq, new ParameterizedTypeReference<Map<String, String>>() {})
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
     * Assign a transportista by delegating to ms-rutas-transportistas. Steps:
     * 1) fetch route by solicitud id
     * 2) call rutas service to assign transportista to that route
     */
    @SuppressWarnings("unchecked")
    public Object assignTransport(Long solicitudId, Long transportistaId) {
        // 1) find route for solicitud
        Map<String, Object> ruta = null;
    ResponseEntity<Map<String, Object>> rutaEntity = rutasClient.get()
        .uri("/api/v1/rutas/por-solicitud/{id}", solicitudId)
        .retrieve()
        .toEntity(new ParameterizedTypeReference<Map<String, Object>>() {});
    ruta = rutaEntity != null ? rutaEntity.getBody() : null;
        if (ruta == null || ruta.get("id") == null) {
            throw new IllegalArgumentException("No ruta found for solicitud: " + solicitudId);
        }
        Object rutaIdObj = ruta.get("id");
        Long rutaId;
        if (rutaIdObj instanceof Number) rutaId = ((Number) rutaIdObj).longValue();
        else rutaId = Long.valueOf(rutaIdObj.toString());

        // 2) get tramos for the route and pick an unassigned tramo
        java.util.List<Map<String, Object>> tramos = null;
    ResponseEntity<java.util.List<java.util.Map<String, Object>>> tramosEntity = rutasClient.get()
        .uri("/api/v1/tramos/por-ruta/{id}", rutaId)
        .retrieve()
        .toEntity(new ParameterizedTypeReference<java.util.List<java.util.Map<String, Object>>>() {});
    tramos = tramosEntity != null ? tramosEntity.getBody() : null;
        if (tramos == null || tramos.isEmpty()) {
            throw new IllegalArgumentException("No tramos found for ruta: " + rutaId);
        }

        Long tramoIdToAssign = null;
        for (Map<String, Object> t : tramos) {
            Object camionDominio = t.get("camionDominio");
            if (camionDominio == null) {
                Object idObj = t.get("id");
                if (idObj instanceof Number) tramoIdToAssign = ((Number) idObj).longValue();
                else tramoIdToAssign = Long.valueOf(idObj.toString());
                break;
            }
        }
        if (tramoIdToAssign == null) {
            // no unassigned tramo found; pick the first tramo
            Object idObj = tramos.get(0).get("id");
            if (idObj instanceof Number) tramoIdToAssign = ((Number) idObj).longValue();
            else tramoIdToAssign = Long.valueOf(idObj.toString());
        }

        // 3) call assign endpoint for the tramo
    ResponseEntity<Object> assignResp = rutasClient.post()
        .uri("/api/v1/tramos/" + tramoIdToAssign + "/asignar-transportista?camionId=" + transportistaId)
        .retrieve()
        .toEntity(Object.class);
    return assignResp != null ? assignResp.getBody() : null;
    }
}
