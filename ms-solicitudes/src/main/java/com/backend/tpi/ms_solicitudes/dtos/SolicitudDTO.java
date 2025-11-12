package com.backend.tpi.ms_solicitudes.dtos;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SolicitudDTO {
    private Long id;
    private String direccionOrigen;
    private String direccionDestino;
    private String estado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;
}
