package com.backend.tpi.ms_gestion_calculos.controllers;

import com.backend.tpi.ms_gestion_calculos.models.Deposito;
import com.backend.tpi.ms_gestion_calculos.services.DepositoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/depositos")
public class DepositoController {

    @Autowired
    private DepositoService depositoService;

    @GetMapping
    public List<Deposito> getAllDepositos() {
        return depositoService.findAll();
    }

    @PostMapping
    public Deposito createDeposito(@RequestBody Deposito deposito) {
        return depositoService.save(deposito);
    }
}
