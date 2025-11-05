package com.backend.tpi.ms_rutas_transportistas.dtos;

import lombok.Data;

@Data
public class TramoDTO {
    private Long id;
    private Long idRuta;
    private Long origenDepositoId;
    private Long destinoDepositoId;
    private Double distancia;
}
