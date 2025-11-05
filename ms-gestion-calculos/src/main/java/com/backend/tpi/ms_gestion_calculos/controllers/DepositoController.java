package com.backend.tpi.ms_gestion_calculos.controllers;

import com.backend.tpi.ms_gestion_calculos.models.Deposito;
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
    public List<Deposito> getAllDepositos() {
        return depositoService.findAll();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('RESPONSABLE','ADMIN')")
    public Deposito createDeposito(@RequestBody Deposito deposito) {
        return depositoService.save(deposito);
    }
}
