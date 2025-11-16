package com.backend.tpi.ms_gestion_calculos.controllers;

import com.backend.tpi.ms_gestion_calculos.dtos.DepositoDTO;
import com.backend.tpi.ms_gestion_calculos.services.DepositoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/depositos")
public class DepositoController {

    private static final Logger logger = LoggerFactory.getLogger(DepositoController.class);

    @Autowired
    private DepositoService depositoService;

    @GetMapping
    @PreAuthorize("hasAnyRole('CLIENTE','RESPONSABLE','ADMIN')")
    public List<DepositoDTO> getAllDepositos() {
        logger.info("GET /api/v1/depositos - Listando todos los depósitos");
        List<DepositoDTO> result = depositoService.findAll();
        logger.info("GET /api/v1/depositos - Respuesta: 200 - {} depósitos encontrados", result.size());
        return result;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('RESPONSABLE','ADMIN')")
    public DepositoDTO createDeposito(@RequestBody DepositoDTO deposito) {
        logger.info("POST /api/v1/depositos - Creando nuevo depósito");
        DepositoDTO result = depositoService.save(deposito);
        logger.info("POST /api/v1/depositos - Respuesta: 200 - Depósito creado con ID: {}", result.getId());
        return result;
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','OPERADOR','RESPONSABLE')")
    public DepositoDTO getDepositoById(@PathVariable Long id) {
        logger.info("GET /api/v1/depositos/{} - Buscando depósito por ID", id);
        DepositoDTO result = depositoService.findById(id);
        logger.info("GET /api/v1/depositos/{} - Respuesta: 200 - Depósito encontrado", id);
        return result;
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','OPERADOR','RESPONSABLE')")
    public DepositoDTO updateDeposito(@PathVariable Long id, @RequestBody DepositoDTO depositoDto) {
        logger.info("PATCH /api/v1/depositos/{} - Actualizando depósito", id);
        DepositoDTO result = depositoService.update(id, depositoDto);
        logger.info("PATCH /api/v1/depositos/{} - Respuesta: 200 - Depósito actualizado", id);
        return result;
    }
}
