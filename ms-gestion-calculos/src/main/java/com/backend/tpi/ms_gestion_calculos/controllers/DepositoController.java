package com.backend.tpi.ms_gestion_calculos.controllers;

import com.backend.tpi.ms_gestion_calculos.dtos.DepositoDTO;
import com.backend.tpi.ms_gestion_calculos.services.DepositoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/depositos")
public class DepositoController {

    @Autowired
    private DepositoService depositoService;

    @GetMapping
    @PreAuthorize("hasAnyRole('CLIENTE','RESPONSABLE','ADMIN')")
    public List<DepositoDTO> getAllDepositos() {
        return depositoService.findAll();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('RESPONSABLE','ADMIN')")
    public DepositoDTO createDeposito(@RequestBody DepositoDTO deposito) {
        return depositoService.save(deposito);
    }
}
