package com.backend.tpi.ms_rutas_transportistas.controllers;

import com.backend.tpi.ms_rutas_transportistas.dtos.CamionDTO;
import com.backend.tpi.ms_rutas_transportistas.services.CamionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/camiones")
@Tag(name = "Camiones", description = "Gestión de camiones y transportistas")
public class CamionController {

    @Autowired
    private CamionService camionService;

    @GetMapping
    @PreAuthorize("hasAnyRole('RESPONSABLE','TRANSPORTISTA','ADMIN')")
    @Operation(summary = "Listar todos los camiones")
    public List<CamionDTO> getAllCamiones() {
        return camionService.findAll();
    }

    @GetMapping("/{dominio}")
    @PreAuthorize("hasAnyRole('RESPONSABLE','TRANSPORTISTA','ADMIN')")
    @Operation(summary = "Obtener camión por dominio/patente")
    public ResponseEntity<CamionDTO> getCamionByDominio(@PathVariable String dominio) {
        CamionDTO camion = camionService.findByDominio(dominio);
        return ResponseEntity.ok(camion);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('RESPONSABLE','ADMIN')")
    @Operation(summary = "Registrar nuevo camión con capacidad y costos")
    public CamionDTO createCamion(@RequestBody CamionDTO camion) {
        return camionService.save(camion);
    }

    @PostMapping("/{dominio}/estado")
    @PreAuthorize("hasAnyRole('TRANSPORTISTA','RESPONSABLE','ADMIN')")
    @Operation(summary = "Actualizar estado operativo del camión")
    public ResponseEntity<CamionDTO> updateEstado(
            @PathVariable String dominio,
            @RequestParam(required = false) Boolean disponible,
            @RequestParam(required = false) Boolean activo) {
        CamionDTO camion = camionService.updateEstado(dominio, disponible, activo);
        return ResponseEntity.ok(camion);
    }

    @PatchMapping("/{dominio}/asignar")
    @PreAuthorize("hasAnyRole('RESPONSABLE','ADMIN')")
    @Operation(summary = "Asignar camión a un transportista")
    public ResponseEntity<CamionDTO> asignarTransportista(
            @PathVariable String dominio,
            @RequestParam String nombreTransportista) {
        CamionDTO camion = camionService.asignarTransportista(dominio, nombreTransportista);
        return ResponseEntity.ok(camion);
    }
}
