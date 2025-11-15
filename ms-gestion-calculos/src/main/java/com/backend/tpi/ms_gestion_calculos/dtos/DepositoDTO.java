package com.backend.tpi.ms_gestion_calculos.dtos;

import lombok.Data;

@Data
public class DepositoDTO {
    private Long id;
    private String nombre;
    private String direccion;
    private Double latitud;
    private Double longitud;
    private Long idCiudad;
}
