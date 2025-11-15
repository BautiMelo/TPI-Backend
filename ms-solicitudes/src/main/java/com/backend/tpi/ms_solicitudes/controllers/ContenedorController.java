package com.backend.tpi.ms_solicitudes.controllers;

import com.backend.tpi.ms_solicitudes.dtos.SeguimientoContenedorDTO;
import com.backend.tpi.ms_solicitudes.models.Contenedor;
import com.backend.tpi.ms_solicitudes.services.ContenedorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/contenedores")
@Tag(name = "Contenedores", description = "Gestión de contenedores")
public class ContenedorController {

    @Autowired
    private ContenedorService contenedorService;

    @GetMapping
    @PreAuthorize("hasAnyRole('RESPONSABLE', 'ADMIN')")
    @Operation(summary = "Listar todos los contenedores")
    public ResponseEntity<List<Contenedor>> getAllContenedores() {
        List<Contenedor> contenedores = contenedorService.findAll();
        return ResponseEntity.ok(contenedores);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENTE', 'RESPONSABLE', 'ADMIN')")
    @Operation(summary = "Obtener contenedor por ID")
    public ResponseEntity<Contenedor> getContenedorById(@PathVariable Long id) {
        Contenedor contenedor = contenedorService.findById(id);
        return ResponseEntity.ok(contenedor);
    }

    @GetMapping("/cliente/{clienteId}")
    @PreAuthorize("hasAnyRole('CLIENTE', 'RESPONSABLE', 'ADMIN')")
    @Operation(summary = "Listar contenedores por cliente")
    public ResponseEntity<List<Contenedor>> getContenedoresByCliente(@PathVariable Long clienteId) {
        List<Contenedor> contenedores = contenedorService.findByClienteId(clienteId);
        return ResponseEntity.ok(contenedores);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('CLIENTE', 'RESPONSABLE', 'ADMIN')")
    @Operation(summary = "Crear nuevo contenedor")
    public ResponseEntity<Contenedor> createContenedor(@RequestBody Contenedor contenedor) {
        Contenedor nuevoContenedor = contenedorService.save(contenedor);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoContenedor);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('RESPONSABLE', 'ADMIN')")
    @Operation(summary = "Actualizar contenedor existente")
    public ResponseEntity<Contenedor> updateContenedor(@PathVariable Long id, @RequestBody Contenedor contenedor) {
        Contenedor contenedorActualizado = contenedorService.update(id, contenedor);
        return ResponseEntity.ok(contenedorActualizado);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar contenedor")
    public ResponseEntity<Void> deleteContenedor(@PathVariable Long id) {
        contenedorService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('RESPONSABLE', 'ADMIN')")
    @Operation(summary = "Actualizar estado del contenedor")
    public ResponseEntity<Contenedor> updateEstadoContenedor(@PathVariable Long id, @RequestParam Long estadoId) {
        Contenedor contenedor = contenedorService.updateEstado(id, estadoId);
        return ResponseEntity.ok(contenedor);
    }

    @GetMapping("/{id}/seguimiento")
    @PreAuthorize("hasAnyRole('CLIENTE', 'RESPONSABLE', 'ADMIN')")
    @Operation(summary = "Consultar seguimiento del contenedor")
    public ResponseEntity<SeguimientoContenedorDTO> getSeguimiento(@PathVariable Long id) {
        SeguimientoContenedorDTO seguimiento = contenedorService.getSeguimiento(id);
        return ResponseEntity.ok(seguimiento);
    }
}
