package com.backend.tpi.ms_gestion_calculos.dtos;

import lombok.Data;

@Data
public class TarifaVolumenPesoDTO {
    private Long id;
    private Double volumenMax;
    private Double pesoMax;
    private Double precio;
}
