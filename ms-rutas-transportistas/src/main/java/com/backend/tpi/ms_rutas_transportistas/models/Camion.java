package com.backend.tpi.ms_rutas_transportistas.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "camiones")
@Data
public class Camion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String patente;
    private String marca;
    private String modelo;
    private Double capacidadCarga;
}
