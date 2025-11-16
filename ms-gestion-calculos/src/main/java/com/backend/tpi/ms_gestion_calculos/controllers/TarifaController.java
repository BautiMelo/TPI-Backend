package com.backend.tpi.ms_gestion_calculos.controllers;

import com.backend.tpi.ms_gestion_calculos.dtos.TarifaDTO;
import com.backend.tpi.ms_gestion_calculos.dtos.TarifaVolumenPesoDTO;
import com.backend.tpi.ms_gestion_calculos.services.TarifaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tarifas")
public class TarifaController {

    private static final Logger logger = LoggerFactory.getLogger(TarifaController.class);

    @Autowired
    private TarifaService tarifaService;

    @GetMapping
    @PreAuthorize("hasAnyRole('CLIENTE','RESPONSABLE','ADMIN')")
    public List<TarifaDTO> getAllTarifas() {
        logger.info("GET /api/v1/tarifas - Listando todas las tarifas");
        List<TarifaDTO> result = tarifaService.findAll();
        logger.info("GET /api/v1/tarifas - Respuesta: 200 - {} tarifas encontradas", result.size());
        return result;
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENTE','RESPONSABLE','ADMIN')")
    public TarifaDTO getTarifaById(@PathVariable Long id) {
        logger.info("GET /api/v1/tarifas/{} - Buscando tarifa por ID", id);
        TarifaDTO result = tarifaService.findById(id);
        logger.info("GET /api/v1/tarifas/{} - Respuesta: 200 - Tarifa encontrada", id);
        return result;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('RESPONSABLE','ADMIN')")
    public TarifaDTO createTarifa(@RequestBody TarifaDTO tarifaDto) {
        logger.info("POST /api/v1/tarifas - Creando nueva tarifa");
        TarifaDTO result = tarifaService.save(tarifaDto);
        logger.info("POST /api/v1/tarifas - Respuesta: 200 - Tarifa creada con ID: {}", result.getId());
        return result;
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABLE')")
    public TarifaDTO updateTarifa(@PathVariable Long id, @RequestBody TarifaDTO tarifaDto) {
        logger.info("PATCH /api/v1/tarifas/{} - Actualizando tarifa", id);
        TarifaDTO result = tarifaService.update(id, tarifaDto);
        logger.info("PATCH /api/v1/tarifas/{} - Respuesta: 200 - Tarifa actualizada", id);
        return result;
    }

    @PostMapping("/{id}/rango")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABLE')")
    public TarifaDTO addRango(@PathVariable Long id, @RequestBody TarifaVolumenPesoDTO rangoDto) {
        logger.info("POST /api/v1/tarifas/{}/rango - Agregando rango a tarifa", id);
        TarifaDTO result = tarifaService.addRango(id, rangoDto);
        logger.info("POST /api/v1/tarifas/{}/rango - Respuesta: 200 - Rango agregado", id);
        return result;
    }

    @PatchMapping("/{idTarifa}/rango/{idRango}")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABLE')")
    public TarifaDTO updateRango(
            @PathVariable Long idTarifa,
            @PathVariable Long idRango,
            @RequestBody TarifaVolumenPesoDTO rangoDto) {
        logger.info("PATCH /api/v1/tarifas/{}/rango/{} - Actualizando rango", idTarifa, idRango);
        TarifaDTO result = tarifaService.updateRango(idTarifa, idRango, rangoDto);
        logger.info("PATCH /api/v1/tarifas/{}/rango/{} - Respuesta: 200 - Rango actualizado", idTarifa, idRango);
        return result;
    }
}
