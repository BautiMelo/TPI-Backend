package com.backend.tpi.ms_gestion_calculos.dtos;

import lombok.Data;

@Data
public class TarifaDTO {
    private Long id;
    private String nombre;
    private Double precioPorKm;
}
