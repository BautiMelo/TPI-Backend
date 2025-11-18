package com.backend.tpi.ms_rutas_transportistas.controllers;

import com.backend.tpi.ms_rutas_transportistas.dtos.*;
import com.backend.tpi.ms_rutas_transportistas.services.RutaService;
import com.backend.tpi.ms_rutas_transportistas.services.RutaTentativaService;
import com.backend.tpi.ms_rutas_transportistas.services.TramoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestionar Rutas
 * Maneja la creación de rutas, asignación de transportistas y gestión de tramos
 */
@RestController
@RequestMapping("/api/v1/rutas")
@Tag(name = "Rutas", description = "Gestión de rutas y tramos de transporte")
public class RutaController {

    private static final Logger logger = LoggerFactory.getLogger(RutaController.class);

    @Autowired
    private RutaService rutaService;

    @Autowired
    private TramoService tramoService;
    
    @Autowired
    private RutaTentativaService rutaTentativaService;

    /**
     * Crea una nueva ruta para una solicitud de transporte
     * @param createRutaDTO Datos de la ruta a crear
     * @return Ruta creada
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('RESPONSABLE','ADMIN')")
    public ResponseEntity<RutaDTO> create(@RequestBody CreateRutaDTO createRutaDTO) {
        logger.info("POST /api/v1/rutas - Creando nueva ruta para solicitud ID: {}", createRutaDTO.getIdSolicitud());
        RutaDTO result = rutaService.create(createRutaDTO);
        logger.info("POST /api/v1/rutas - Respuesta: 200 - Ruta creada con ID: {}", result.getId());
        return ResponseEntity.ok(result);
    }

    /**
     * Obtiene la lista de todas las rutas registradas
     * @return Lista de rutas
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('CLIENTE','RESPONSABLE','ADMIN','TRANSPORTISTA')")
    public ResponseEntity<List<RutaDTO>> findAll() {
        logger.info("GET /api/v1/rutas - Listando todas las rutas");
        List<RutaDTO> result = rutaService.findAll();
        logger.info("GET /api/v1/rutas - Respuesta: 200 - {} rutas encontradas", result.size());
        return ResponseEntity.ok(result);
    }

    /**
     * Obtiene una ruta específica por su ID
     * @param id ID de la ruta
     * @return Ruta encontrada o 404 si no existe
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENTE','RESPONSABLE','ADMIN','TRANSPORTISTA')")
    public ResponseEntity<RutaDTO> findById(@PathVariable Long id) {
        logger.info("GET /api/v1/rutas/{} - Buscando ruta por ID", id);
        RutaDTO rutaDTO = rutaService.findById(id);
        if (rutaDTO == null) {
            logger.warn("GET /api/v1/rutas/{} - Respuesta: 404 - Ruta no encontrada", id);
            return ResponseEntity.notFound().build();
        }
        logger.info("GET /api/v1/rutas/{} - Respuesta: 200 - Ruta encontrada", id);
        return ResponseEntity.ok(rutaDTO);
    }

    /**
     * Elimina una ruta del sistema
     * @param id ID de la ruta a eliminar
     * @return Respuesta sin contenido
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('RESPONSABLE','ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        logger.info("DELETE /api/v1/rutas/{} - Eliminando ruta", id);
        rutaService.delete(id);
        logger.info("DELETE /api/v1/rutas/{} - Respuesta: 204 - Ruta eliminada", id);
        return ResponseEntity.noContent().build();
    }

    // ---- Integration endpoints (delegan al service) ----

    /**
     * Asigna un transportista a una ruta específica
     * @param id ID de la ruta
     * @param transportistaId ID del transportista a asignar
     * @return Resultado de la asignación
     */
    @PostMapping("/{id}/asignar-transportista")
    @PreAuthorize("hasAnyRole('RESPONSABLE','ADMIN')")
    public ResponseEntity<Object> asignarTransportista(@PathVariable Long id, @RequestParam Long transportistaId) {
        logger.info("POST /api/v1/rutas/{}/asignar-transportista - Asignando transportista ID: {}", id, transportistaId);
        Object result = rutaService.assignTransportista(id, transportistaId);
        logger.info("POST /api/v1/rutas/{}/asignar-transportista - Respuesta: 200 - Transportista asignado", id);
        return ResponseEntity.ok(result);
    }

    /**
     * Busca la ruta asociada a una solicitud específica
     * @param solicitudId ID de la solicitud
     * @return Ruta asociada a la solicitud
     */
    @GetMapping("/por-solicitud/{solicitudId}")
    @PreAuthorize("hasAnyRole('RESPONSABLE','ADMIN','TRANSPORTISTA')")
    public ResponseEntity<Object> findBySolicitud(@PathVariable Long solicitudId) {
        logger.info("GET /api/v1/rutas/por-solicitud/{} - Buscando ruta por solicitud", solicitudId);
        Object result = rutaService.findBySolicitudId(solicitudId);
        logger.info("GET /api/v1/rutas/por-solicitud/{} - Respuesta: 200 - Ruta encontrada", solicitudId);
        return ResponseEntity.ok(result);
    }

    /**
     * Agrega un nuevo tramo a una ruta existente
     * @param id ID de la ruta
     * @param tramoRequest Datos del tramo a agregar
     * @return Tramo creado
     */
    @PostMapping("/{id}/tramos")
    @PreAuthorize("hasAnyRole('RESPONSABLE','ADMIN')")
    @Operation(summary = "Agregar un nuevo tramo a una ruta")
    public ResponseEntity<TramoDTO> agregarTramo(
            @PathVariable Long id,
            @RequestBody TramoRequestDTO tramoRequest) {
        logger.info("POST /api/v1/rutas/{}/tramos - Agregando nuevo tramo a ruta", id);
        tramoRequest.setIdRuta(id); // Asegurar que el tramo se asocie a esta ruta
        TramoDTO tramo = tramoService.create(tramoRequest);
        if (tramo == null) {
            logger.warn("POST /api/v1/rutas/{}/tramos - Respuesta: 400 - Error al crear tramo", id);
            return ResponseEntity.badRequest().build();
        }
        logger.info("POST /api/v1/rutas/{}/tramos - Respuesta: 200 - Tramo creado con ID: {}", id, tramo.getId());
        return ResponseEntity.ok(tramo);
    }

    /**
     * Marca el inicio de un tramo de transporte
     * @param id ID de la ruta
     * @param tramoId ID del tramo a iniciar
     * @return Tramo iniciado
     */
    @PostMapping("/{id}/tramos/{tramoId}/iniciar")
    @PreAuthorize("hasAnyRole('TRANSPORTISTA','RESPONSABLE','ADMIN')")
    @Operation(summary = "Marcar el inicio de un tramo de transporte")
    public ResponseEntity<TramoDTO> iniciarTramo(
            @PathVariable Long id,
            @PathVariable Long tramoId) {
        logger.info("POST /api/v1/rutas/{}/tramos/{}/iniciar - Iniciando tramo", id, tramoId);
        TramoDTO tramo = tramoService.iniciarTramo(id, tramoId);
        if (tramo == null) {
            logger.warn("POST /api/v1/rutas/{}/tramos/{}/iniciar - Respuesta: 404 - Tramo no encontrado", id, tramoId);
            return ResponseEntity.notFound().build();
        }
        logger.info("POST /api/v1/rutas/{}/tramos/{}/iniciar - Respuesta: 200 - Tramo iniciado", id, tramoId);
        return ResponseEntity.ok(tramo);
    }

    /**
     * Marca la finalización de un tramo de transporte
     * @param id ID de la ruta
     * @param tramoId ID del tramo a finalizar
     * @param fechaHoraReal Fecha y hora de finalización (opcional)
     * @return Tramo finalizado
     */
    @PostMapping("/{id}/tramos/{tramoId}/finalizar")
    @PreAuthorize("hasAnyRole('TRANSPORTISTA','RESPONSABLE','ADMIN')")
    @Operation(summary = "Marcar la finalización de un tramo de transporte")
    public ResponseEntity<TramoDTO> finalizarTramo(
            @PathVariable Long id,
            @PathVariable Long tramoId,
            @RequestParam(required = false) String fechaHoraReal) {
        logger.info("POST /api/v1/rutas/{}/tramos/{}/finalizar - Finalizando tramo", id, tramoId);
        
        java.time.LocalDateTime fechaHora = null;
        if (fechaHoraReal != null && !fechaHoraReal.isEmpty()) {
            try {
                fechaHora = java.time.LocalDateTime.parse(fechaHoraReal);
            } catch (Exception e) {
                logger.warn("POST /api/v1/rutas/{}/tramos/{}/finalizar - Respuesta: 400 - Formato de fecha inválido", id, tramoId);
                return ResponseEntity.badRequest().build();
            }
        }
        
        TramoDTO tramo = tramoService.finalizarTramo(id, tramoId, fechaHora);
        if (tramo == null) {
            logger.warn("POST /api/v1/rutas/{}/tramos/{}/finalizar - Respuesta: 404 - Tramo no encontrado", id, tramoId);
            return ResponseEntity.notFound().build();
        }
        logger.info("POST /api/v1/rutas/{}/tramos/{}/finalizar - Respuesta: 200 - Tramo finalizado", id, tramoId);
        return ResponseEntity.ok(tramo);
    }
    
    /**
     * Calcula una ruta tentativa entre dos depósitos, opcionalmente con depósitos intermedios
     * @param origenDepositoId ID del depósito origen
     * @param destinoDepositoId ID del depósito destino
     * @param intermedios IDs de depósitos intermedios separados por comas (opcional)
     * @return Ruta tentativa calculada con distancias y duraciones
     */
    @GetMapping("/tentativa")
    @PreAuthorize("hasAnyRole('RESPONSABLE','ADMIN','TRANSPORTISTA')")
    @Operation(summary = "Calcular ruta tentativa entre depósitos",
            description = "Calcula una ruta tentativa con distancias reales usando OSRM. " +
                         "Permite especificar depósitos intermedios en orden.")
    public ResponseEntity<RutaTentativaDTO> calcularRutaTentativa(
            @RequestParam Long origenDepositoId,
            @RequestParam Long destinoDepositoId,
            @RequestParam(required = false) String intermedios) {
        
        logger.info("GET /api/v1/rutas/tentativa - Calculando ruta tentativa: origen={}, destino={}, intermedios={}", 
                origenDepositoId, destinoDepositoId, intermedios);
        
        // Parsear depósitos intermedios si existen
        List<Long> depositosIntermedios = null;
        if (intermedios != null && !intermedios.trim().isEmpty()) {
            try {
                depositosIntermedios = java.util.Arrays.stream(intermedios.split(","))
                        .map(String::trim)
                        .map(Long::parseLong)
                        .toList();
                logger.debug("Depósitos intermedios parseados: {}", depositosIntermedios);
            } catch (NumberFormatException e) {
                logger.warn("GET /api/v1/rutas/tentativa - Error parseando intermedios: {}", e.getMessage());
                return ResponseEntity.badRequest().build();
            }
        }
        
        RutaTentativaDTO resultado = rutaTentativaService.calcularRutaTentativa(
                origenDepositoId, destinoDepositoId, depositosIntermedios);
        
        if (!resultado.getExitoso()) {
            logger.warn("GET /api/v1/rutas/tentativa - Error: {}", resultado.getMensaje());
            return ResponseEntity.status(500).body(resultado);
        }
        
        logger.info("GET /api/v1/rutas/tentativa - Respuesta: 200 - Ruta calculada: {} km, {} tramos", 
                resultado.getDistanciaTotal(), resultado.getNumeroTramos());
        return ResponseEntity.ok(resultado);
    }

    /**
     * Calcula las distancias y duraciones de todos los tramos de una ruta usando OSRM
     * @param id ID de la ruta
     * @return Resultado del cálculo con distancias y duraciones actualizadas
     */
    @PostMapping("/{id}/calcular-distancias")
    @PreAuthorize("hasAnyRole('RESPONSABLE','ADMIN')")
    @Operation(summary = "Calcular distancias y duraciones de todos los tramos",
            description = "Usa OSRM para calcular las distancias y duraciones reales de cada tramo de la ruta")
    public ResponseEntity<java.util.Map<String, Object>> calcularDistancias(@PathVariable Long id) {
        logger.info("POST /api/v1/rutas/{}/calcular-distancias - Calculando distancias de tramos", id);
        try {
            java.util.Map<String, Object> resultado = rutaService.calcularRutaCompleta(id);
            logger.info("POST /api/v1/rutas/{}/calcular-distancias - Respuesta: 200 - {} tramos actualizados", 
                id, resultado.get("tramosActualizados"));
            return ResponseEntity.ok(resultado);
        } catch (IllegalArgumentException e) {
            logger.warn("POST /api/v1/rutas/{}/calcular-distancias - Respuesta: 400 - {}", id, e.getMessage());
            java.util.Map<String, Object> error = new java.util.HashMap<>();
            error.put("exitoso", false);
            error.put("mensaje", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            logger.error("POST /api/v1/rutas/{}/calcular-distancias - Respuesta: 500 - {}", id, e.getMessage());
            java.util.Map<String, Object> error = new java.util.HashMap<>();
            error.put("exitoso", false);
            error.put("mensaje", "Error al calcular distancias: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * Calcula el costo total de una ruta basado en las distancias de sus tramos
     * @param id ID de la ruta
     * @return Resultado del cálculo con el costo total y costos por tramo
     */
    @PostMapping("/{id}/calcular-costos")
    @PreAuthorize("hasAnyRole('RESPONSABLE','ADMIN')")
    @Operation(summary = "Calcular costos de todos los tramos de la ruta",
            description = "Calcula el costo de cada tramo basado en su distancia y la tarifa por km configurada")
    public ResponseEntity<java.util.Map<String, Object>> calcularCostos(@PathVariable Long id) {
        logger.info("POST /api/v1/rutas/{}/calcular-costos - Calculando costos", id);
        try {
            java.util.Map<String, Object> resultado = rutaService.calcularCostoRuta(id);
            logger.info("POST /api/v1/rutas/{}/calcular-costos - Respuesta: 200 - Costo total: ${}", 
                id, resultado.get("costoTotal"));
            return ResponseEntity.ok(resultado);
        } catch (IllegalArgumentException e) {
            logger.warn("POST /api/v1/rutas/{}/calcular-costos - Respuesta: 400 - {}", id, e.getMessage());
            java.util.Map<String, Object> error = new java.util.HashMap<>();
            error.put("exitoso", false);
            error.put("mensaje", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            logger.error("POST /api/v1/rutas/{}/calcular-costos - Respuesta: 500 - {}", id, e.getMessage());
            java.util.Map<String, Object> error = new java.util.HashMap<>();
            error.put("exitoso", false);
            error.put("mensaje", "Error al calcular costos: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * Calcula tanto las distancias como los costos de una ruta en una sola operación
     * @param id ID de la ruta
     * @return Resultado combinado con distancias, duraciones y costos
     */
    @PostMapping("/{id}/calcular-completo")
    @PreAuthorize("hasAnyRole('RESPONSABLE','ADMIN')")
    @Operation(summary = "Calcular distancias y costos completos de la ruta",
            description = "Calcula las distancias de todos los tramos usando OSRM y luego calcula los costos basados en esas distancias")
    public ResponseEntity<java.util.Map<String, Object>> calcularCompleto(@PathVariable Long id) {
        logger.info("POST /api/v1/rutas/{}/calcular-completo - Calculando distancias y costos", id);
        try {
            // Primero calcular distancias
            java.util.Map<String, Object> distancias = rutaService.calcularRutaCompleta(id);
            
            // Luego calcular costos
            java.util.Map<String, Object> costos = rutaService.calcularCostoRuta(id);
            
            // Combinar resultados
            java.util.Map<String, Object> resultado = new java.util.HashMap<>();
            resultado.put("rutaId", id);
            resultado.put("distanciaTotal", distancias.get("distanciaTotal"));
            resultado.put("duracionTotalHoras", distancias.get("duracionTotalHoras"));
            resultado.put("duracionTotalMinutos", distancias.get("duracionTotalMinutos"));
            resultado.put("numeroTramos", distancias.get("numeroTramos"));
            resultado.put("tramosActualizados", distancias.get("tramosActualizados"));
            resultado.put("costoTotal", costos.get("costoTotal"));
            resultado.put("tarifaPorKm", costos.get("tarifaPorKm"));
            resultado.put("costosPorTramo", costos.get("costosPorTramo"));
            resultado.put("exitoso", true);
            resultado.put("mensaje", String.format("Ruta calculada: %.2f km, %.2f horas, $%.2f", 
                resultado.get("distanciaTotal"), resultado.get("duracionTotalHoras"), resultado.get("costoTotal")));
            
            logger.info("POST /api/v1/rutas/{}/calcular-completo - Respuesta: 200 - Distancia: {} km, Costo: ${}", 
                id, resultado.get("distanciaTotal"), resultado.get("costoTotal"));
            return ResponseEntity.ok(resultado);
        } catch (IllegalArgumentException e) {
            logger.warn("POST /api/v1/rutas/{}/calcular-completo - Respuesta: 400 - {}", id, e.getMessage());
            java.util.Map<String, Object> error = new java.util.HashMap<>();
            error.put("exitoso", false);
            error.put("mensaje", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            logger.error("POST /api/v1/rutas/{}/calcular-completo - Respuesta: 500 - {}", id, e.getMessage());
            java.util.Map<String, Object> error = new java.util.HashMap<>();
            error.put("exitoso", false);
            error.put("mensaje", "Error al calcular ruta completa: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
}
