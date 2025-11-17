package com.backend.tpi.ms_solicitudes.controllers;

import com.backend.tpi.ms_solicitudes.dtos.SeguimientoContenedorDTO;
import com.backend.tpi.ms_solicitudes.models.Contenedor;
import com.backend.tpi.ms_solicitudes.services.ContenedorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestionar Contenedores
 * Permite gestionar contenedores de carga y su seguimiento
 */
@RestController
@RequestMapping("/api/v1/contenedores")
@Tag(name = "Contenedores", description = "Gestión de contenedores")
public class ContenedorController {

    private static final Logger logger = LoggerFactory.getLogger(ContenedorController.class);

    @Autowired
    private ContenedorService contenedorService;

    /**
     * GET /api/v1/contenedores - Lista todos los contenedores del sistema
     * Requiere rol RESPONSABLE o ADMIN
     * @return Lista de todos los contenedores
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('RESPONSABLE', 'ADMIN')")
    @Operation(summary = "Listar todos los contenedores")
    public ResponseEntity<List<Contenedor>> getAllContenedores() {
        logger.info("GET /api/v1/contenedores - Listando todos los contenedores");
        List<Contenedor> contenedores = contenedorService.findAll();
        logger.info("GET /api/v1/contenedores - Respuesta: 200 - {} contenedores encontrados", contenedores.size());
        return ResponseEntity.ok(contenedores);
    }

    /**
     * GET /api/v1/contenedores/{id} - Obtiene un contenedor específico por ID
     * Requiere rol CLIENTE, RESPONSABLE o ADMIN
     * @param id ID del contenedor
     * @return Contenedor encontrado
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENTE', 'RESPONSABLE', 'ADMIN')")
    @Operation(summary = "Obtener contenedor por ID")
    public ResponseEntity<Contenedor> getContenedorById(@PathVariable Long id) {
        logger.info("GET /api/v1/contenedores/{} - Buscando contenedor por ID", id);
        Contenedor contenedor = contenedorService.findById(id);
        logger.info("GET /api/v1/contenedores/{} - Respuesta: 200 - Contenedor encontrado", id);
        return ResponseEntity.ok(contenedor);
    }

    /**
     * GET /api/v1/contenedores/cliente/{clienteId} - Lista contenedores de un cliente
     * Requiere rol CLIENTE, RESPONSABLE o ADMIN
     * @param clienteId ID del cliente
     * @return Lista de contenedores del cliente
     */
    @GetMapping("/cliente/{clienteId}")
    @PreAuthorize("hasAnyRole('CLIENTE', 'RESPONSABLE', 'ADMIN')")
    @Operation(summary = "Listar contenedores por cliente")
    public ResponseEntity<List<Contenedor>> getContenedoresByCliente(@PathVariable Long clienteId) {
        logger.info("GET /api/v1/contenedores/cliente/{} - Buscando contenedores del cliente", clienteId);
        List<Contenedor> contenedores = contenedorService.findByClienteId(clienteId);
        logger.info("GET /api/v1/contenedores/cliente/{} - Respuesta: 200 - {} contenedores encontrados", clienteId, contenedores.size());
        return ResponseEntity.ok(contenedores);
    }

    /**
     * POST /api/v1/contenedores - Crea un nuevo contenedor
     * Requiere rol CLIENTE, RESPONSABLE o ADMIN
     * @param contenedor Datos del contenedor a crear
     * @return Contenedor creado con código 201
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('CLIENTE', 'RESPONSABLE', 'ADMIN')")
    @Operation(summary = "Crear nuevo contenedor")
    public ResponseEntity<Contenedor> createContenedor(@RequestBody Contenedor contenedor) {
        logger.info("POST /api/v1/contenedores - Creando nuevo contenedor");
        Contenedor nuevoContenedor = contenedorService.save(contenedor);
        logger.info("POST /api/v1/contenedores - Respuesta: 201 - Contenedor creado con ID: {}", nuevoContenedor.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoContenedor);
    }

    /**
     * PUT /api/v1/contenedores/{id} - Actualiza un contenedor existente
     * Requiere rol RESPONSABLE o ADMIN
     * @param id ID del contenedor a actualizar
     * @param contenedor Datos actualizados del contenedor
     * @return Contenedor actualizado
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('RESPONSABLE', 'ADMIN')")
    @Operation(summary = "Actualizar contenedor existente")
    public ResponseEntity<Contenedor> updateContenedor(@PathVariable Long id, @RequestBody Contenedor contenedor) {
        logger.info("PUT /api/v1/contenedores/{} - Actualizando contenedor", id);
        Contenedor contenedorActualizado = contenedorService.update(id, contenedor);
        logger.info("PUT /api/v1/contenedores/{} - Respuesta: 200 - Contenedor actualizado", id);
        return ResponseEntity.ok(contenedorActualizado);
    }

    /**
     * DELETE /api/v1/contenedores/{id} - Elimina un contenedor
     * Requiere rol ADMIN
     * @param id ID del contenedor a eliminar
     * @return No Content (204)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar contenedor")
    public ResponseEntity<Void> deleteContenedor(@PathVariable Long id) {
        logger.info("DELETE /api/v1/contenedores/{} - Eliminando contenedor", id);
        contenedorService.deleteById(id);
        logger.info("DELETE /api/v1/contenedores/{} - Respuesta: 204 - Contenedor eliminado", id);
        return ResponseEntity.noContent().build();
    }

    /**
     * PATCH /api/v1/contenedores/{id} - Actualiza el estado de un contenedor
     * Requiere rol RESPONSABLE o ADMIN
     * @param id ID del contenedor
     * @param estadoId ID del nuevo estado
     * @return Contenedor con estado actualizado
     */
    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('RESPONSABLE', 'ADMIN')")
    @Operation(summary = "Actualizar estado del contenedor")
    public ResponseEntity<Contenedor> updateEstadoContenedor(@PathVariable Long id, @RequestParam Long estadoId) {
        logger.info("PATCH /api/v1/contenedores/{} - Actualizando estado - nuevoEstadoId: {}", id, estadoId);
        Contenedor contenedor = contenedorService.updateEstado(id, estadoId);
        logger.info("PATCH /api/v1/contenedores/{} - Respuesta: 200 - Estado actualizado", id);
        return ResponseEntity.ok(contenedor);
    }

    /**
     * GET /api/v1/contenedores/{id}/seguimiento - Consulta la ubicación y estado actual de un contenedor
     * Requiere rol CLIENTE, RESPONSABLE o ADMIN
     * @param id ID del contenedor
     * @return Información de seguimiento del contenedor
     */
    @GetMapping("/{id}/seguimiento")
    @PreAuthorize("hasAnyRole('CLIENTE', 'RESPONSABLE', 'ADMIN')")
    @Operation(summary = "Consultar seguimiento del contenedor")
    public ResponseEntity<SeguimientoContenedorDTO> getSeguimiento(@PathVariable Long id) {
        logger.info("GET /api/v1/contenedores/{}/seguimiento - Consultando seguimiento", id);
        SeguimientoContenedorDTO seguimiento = contenedorService.getSeguimiento(id);
        logger.info("GET /api/v1/contenedores/{}/seguimiento - Respuesta: 200 - Seguimiento obtenido", id);
        return ResponseEntity.ok(seguimiento);
    }

    /**
     * GET /api/v1/contenedores/{id}/estados-permitidos - Consulta los estados a los que puede transicionar el contenedor
     * Requiere rol RESPONSABLE o ADMIN
     * @param id ID del contenedor
     * @return Lista de nombres de estados permitidos
     */
    @GetMapping("/{id}/estados-permitidos")
    @PreAuthorize("hasAnyRole('RESPONSABLE', 'ADMIN')")
    @Operation(summary = "Consultar estados permitidos para el contenedor")
    public ResponseEntity<List<String>> getEstadosPermitidos(@PathVariable Long id) {
        logger.info("GET /api/v1/contenedores/{}/estados-permitidos - Consultando transiciones permitidas", id);
        List<String> estadosPermitidos = contenedorService.getEstadosPermitidos(id);
        logger.info("GET /api/v1/contenedores/{}/estados-permitidos - Respuesta: 200 - {} estados permitidos", id, estadosPermitidos.size());
        return ResponseEntity.ok(estadosPermitidos);
    }
}
