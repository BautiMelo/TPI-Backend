package com.backend.tpi.ms_rutas_transportistas.dtos;

import lombok.Data;

@Data
public class CamionDTO {
    private Long id;
    private String patente;
    private Double capacidad;
    private String estado;
}
