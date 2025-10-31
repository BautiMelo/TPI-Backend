package com.backend.tpi.ms_gestion_calculos.models;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Data
@Table(name = "tarifas")
public class Tarifa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tarifa")
    private Long id;

    @Column(name = "costo_base_gestion_fijo")
    private BigDecimal costoBaseGestionFijo;

    @Column(name = "valor_litro_combustible")
    private BigDecimal valorLitroCombustible;
}
