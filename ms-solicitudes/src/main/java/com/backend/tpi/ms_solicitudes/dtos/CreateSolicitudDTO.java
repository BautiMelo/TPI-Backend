package com.backend.tpi.ms_solicitudes.dtos;

import lombok.Data;

@Data
public class CreateSolicitudDTO {
    private String direccionOrigen;
    private String direccionDestino;
    private Long contenedorId;
}
