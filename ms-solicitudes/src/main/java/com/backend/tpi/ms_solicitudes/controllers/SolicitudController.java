package com.backend.tpi.ms_solicitudes.controllers;

import com.backend.tpi.ms_solicitudes.dtos.CreateSolicitudDTO;
import com.backend.tpi.ms_solicitudes.dtos.SolicitudDTO;
import com.backend.tpi.ms_solicitudes.services.SolicitudService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestionar Solicitudes de transporte
 * Expone endpoints CRUD y operaciones de integración con otros microservicios
 */
@RestController
@RequestMapping("/api/v1/solicitudes")
public class SolicitudController {

    private static final Logger logger = LoggerFactory.getLogger(SolicitudController.class);

    @Autowired
    private SolicitudService solicitudService;

    /**
     * POST /api/v1/solicitudes - Crea una nueva solicitud de transporte
     * Requiere rol CLIENTE
     * @param createSolicitudDTO Datos de la solicitud a crear
     * @return Solicitud creada con código 200
     */
    @PostMapping
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<SolicitudDTO> create(@RequestBody CreateSolicitudDTO createSolicitudDTO) {
        logger.info("POST /api/v1/solicitudes - Creando nueva solicitud");
        SolicitudDTO result = solicitudService.create(createSolicitudDTO);
        logger.info("POST /api/v1/solicitudes - Respuesta: 200 - Solicitud creada con ID: {}", result.getId());
        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/v1/solicitudes - Obtiene lista de solicitudes con filtros opcionales
     * Requiere rol RESPONSABLE o ADMIN
     * @param estado Filtro por nombre de estado (opcional)
     * @param clienteId Filtro por ID de cliente (opcional)
     * @return Lista de solicitudes filtradas
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('RESPONSABLE','ADMIN')")
    public ResponseEntity<List<SolicitudDTO>> findAll(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Long clienteId) {
        logger.info("GET /api/v1/solicitudes - Consultando solicitudes con filtros - estado: {}, clienteId: {}", estado, clienteId);
        List<SolicitudDTO> result = solicitudService.findAllWithFilters(estado, clienteId);
        logger.info("GET /api/v1/solicitudes - Respuesta: 200 - {} solicitudes encontradas", result.size());
        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/v1/solicitudes/{id} - Obtiene una solicitud específica por ID
     * Requiere rol CLIENTE, RESPONSABLE o ADMIN
     * @param id ID de la solicitud
     * @return Solicitud encontrada (200) o Not Found (404)
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENTE','RESPONSABLE','ADMIN')")
    public ResponseEntity<SolicitudDTO> findById(@PathVariable Long id) {
        logger.info("GET /api/v1/solicitudes/{} - Buscando solicitud por ID", id);
        SolicitudDTO solicitudDTO = solicitudService.findById(id);
        if (solicitudDTO == null) {
            logger.warn("GET /api/v1/solicitudes/{} - Respuesta: 404 - Solicitud no encontrada", id);
            return ResponseEntity.notFound().build();
        }
        logger.info("GET /api/v1/solicitudes/{} - Respuesta: 200 - Solicitud encontrada", id);
        return ResponseEntity.ok(solicitudDTO);
    }

    /**
     * PUT /api/v1/solicitudes/{id} - Actualiza los datos de una solicitud
     * Requiere rol RESPONSABLE o ADMIN
     * @param id ID de la solicitud a actualizar
     * @param createSolicitudDTO Nuevos datos de la solicitud
     * @return Solicitud actualizada (200) o Not Found (404)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('RESPONSABLE','ADMIN')")
    public ResponseEntity<SolicitudDTO> update(@PathVariable Long id, @RequestBody CreateSolicitudDTO createSolicitudDTO) {
        logger.info("PUT /api/v1/solicitudes/{} - Actualizando solicitud", id);
        SolicitudDTO solicitudDTO = solicitudService.update(id, createSolicitudDTO);
        if (solicitudDTO == null) {
            logger.warn("PUT /api/v1/solicitudes/{} - Respuesta: 404 - Solicitud no encontrada", id);
            return ResponseEntity.notFound().build();
        }
        logger.info("PUT /api/v1/solicitudes/{} - Respuesta: 200 - Solicitud actualizada", id);
        return ResponseEntity.ok(solicitudDTO);
    }

    /**
     * DELETE /api/v1/solicitudes/{id} - Elimina una solicitud
     * Requiere rol RESPONSABLE o ADMIN
     * @param id ID de la solicitud a eliminar
     * @return No Content (204)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('RESPONSABLE','ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        logger.info("DELETE /api/v1/solicitudes/{} - Eliminando solicitud", id);
        solicitudService.delete(id);
        logger.info("DELETE /api/v1/solicitudes/{} - Respuesta: 204 - Solicitud eliminada", id);
        return ResponseEntity.noContent().build();
    }

    // ---- Integration endpoints (delegan al service) ----

    /**
     * POST /api/v1/solicitudes/{id}/solicitar-ruta - Solicita una ruta para la solicitud
     * Requiere rol RESPONSABLE o ADMIN
     * @param id ID de la solicitud
     * @return Respuesta del microservicio de rutas
     */
    @PostMapping("/{id}/solicitar-ruta")
    @PreAuthorize("hasAnyRole('RESPONSABLE','ADMIN')")
    public ResponseEntity<Object> requestRoute(@PathVariable Long id) {
        logger.info("POST /api/v1/solicitudes/{}/solicitar-ruta - Solicitando ruta", id);
        Object result = solicitudService.requestRoute(id);
        logger.info("POST /api/v1/solicitudes/{}/solicitar-ruta - Respuesta: 200 - Ruta solicitada", id);
        return ResponseEntity.ok(result);
    }

    /**
     * POST /api/v1/solicitudes/{id}/calcular-precio - Calcula el precio de una solicitud
     * Requiere rol RESPONSABLE o ADMIN
     * @param id ID de la solicitud
     * @return Información de costos calculados
     */
    @PostMapping("/{id}/calcular-precio")
    @PreAuthorize("hasAnyRole('RESPONSABLE','ADMIN')")
    public ResponseEntity<Object> calculatePrice(@PathVariable Long id) {
        logger.info("POST /api/v1/solicitudes/{}/calcular-precio - Calculando precio", id);
        Object result = solicitudService.calculatePrice(id);
        logger.info("POST /api/v1/solicitudes/{}/calcular-precio - Respuesta: 200 - Precio calculado", id);
        return ResponseEntity.ok(result);
    }

    /**
     * POST /api/v1/solicitudes/{id}/asignar-transporte - Asigna un camión/transportista a la solicitud
     * Requiere rol RESPONSABLE o ADMIN
     * @param id ID de la solicitud
     * @param transportistaId ID del camión a asignar
     * @return Respuesta de la asignación
     */
    @PostMapping("/{id}/asignar-transporte")
    @PreAuthorize("hasAnyRole('RESPONSABLE','ADMIN')")
    public ResponseEntity<Object> assignTransport(@PathVariable Long id, @RequestParam Long transportistaId) {
        logger.info("POST /api/v1/solicitudes/{}/asignar-transporte - Asignando transporte - transportistaId: {}", id, transportistaId);
        Object result = solicitudService.assignTransport(id, transportistaId);
        logger.info("POST /api/v1/solicitudes/{}/asignar-transporte - Respuesta: 200 - Transporte asignado", id);
        return ResponseEntity.ok(result);
    }

    /**
     * PATCH /api/v1/solicitudes/{id}/estado - Actualiza el estado de una solicitud
     * Requiere rol RESPONSABLE o ADMIN
     * @param id ID de la solicitud
     * @param estadoId ID del nuevo estado
     * @return Solicitud con estado actualizado
     */
    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('RESPONSABLE','ADMIN')")
    public ResponseEntity<SolicitudDTO> updateEstado(@PathVariable Long id, @RequestParam Long estadoId) {
        logger.info("PATCH /api/v1/solicitudes/{}/estado - Actualizando estado - nuevoEstadoId: {}", id, estadoId);
        SolicitudDTO solicitudDTO = solicitudService.updateEstado(id, estadoId);
        logger.info("PATCH /api/v1/solicitudes/{}/estado - Respuesta: 200 - Estado actualizado", id);
        return ResponseEntity.ok(solicitudDTO);
    }

    /**
     * PATCH /api/v1/solicitudes/{id}/programar - Programa una solicitud asignando costo y tiempo estimados
     * Requiere rol RESPONSABLE o ADMIN
     * @param id ID de la solicitud
     * @param costoEstimado Costo estimado del transporte
     * @param tiempoEstimado Tiempo estimado del transporte
     * @return Solicitud programada
     */
    @PatchMapping("/{id}/programar")
    @PreAuthorize("hasAnyRole('RESPONSABLE','ADMIN')")
    public ResponseEntity<SolicitudDTO> programar(
            @PathVariable Long id, 
            @RequestParam java.math.BigDecimal costoEstimado,
            @RequestParam java.math.BigDecimal tiempoEstimado) {
        logger.info("PATCH /api/v1/solicitudes/{}/programar - Programando solicitud - costoEstimado: {}, tiempoEstimado: {}", 
            id, costoEstimado, tiempoEstimado);
        SolicitudDTO solicitudDTO = solicitudService.programar(id, costoEstimado, tiempoEstimado);
        logger.info("PATCH /api/v1/solicitudes/{}/programar - Respuesta: 200 - Solicitud programada", id);
        return ResponseEntity.ok(solicitudDTO);
    }

    /**
     * GET /api/v1/solicitudes/{id}/estados-permitidos - Consulta los estados a los que puede transicionar la solicitud
     * Requiere rol RESPONSABLE o ADMIN
     * @param id ID de la solicitud
     * @return Lista de nombres de estados permitidos
     */
    @GetMapping("/{id}/estados-permitidos")
    @PreAuthorize("hasAnyRole('RESPONSABLE','ADMIN')")
    public ResponseEntity<List<String>> getEstadosPermitidos(@PathVariable Long id) {
        logger.info("GET /api/v1/solicitudes/{}/estados-permitidos - Consultando transiciones permitidas", id);
        List<String> estadosPermitidos = solicitudService.getEstadosPermitidos(id);
        logger.info("GET /api/v1/solicitudes/{}/estados-permitidos - Respuesta: 200 - {} estados permitidos", id, estadosPermitidos.size());
        return ResponseEntity.ok(estadosPermitidos);
    }
}
