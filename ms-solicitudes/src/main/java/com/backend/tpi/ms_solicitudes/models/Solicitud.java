package com.backend.tpi.ms_solicitudes.models;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Data
@Table(name = "solicitudes")
public class Solicitud {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_solicitud")
    private Long id;

    @Column(name = "contenedor_id")
    private Long contenedorId;

    @Column(name = "cliente_id")
    private Long clienteId;

    @Column(name = "origen_lat")
    private BigDecimal origenLat;

    @Column(name = "origen_long")
    private BigDecimal origenLong;

    @Column(name = "destino_lat")
    private BigDecimal destinoLat;

    @Column(name = "destino_long")
    private BigDecimal destinoLong;

    @Column(name = "direccion_origen")
    private String direccionOrigen;

    @Column(name = "direccion_destino")
    private String direccionDestino;

    @ManyToOne
    @JoinColumn(name = "estado_solicitud_id")
    private EstadoSolicitud estado;

    @Column(name = "costo_estimado")
    private BigDecimal costoEstimado;

    @Column(name = "costo_final")
    private BigDecimal costoFinal;

    @Column(name = "tiempo_estimado")
    private BigDecimal tiempoEstimado;

    @Column(name = "tiempo_real")
    private BigDecimal tiempoReal;

    @Column(name = "ruta_id")
    private Long rutaId;

    @Column(name = "tarifa_id")
    private Long tarifaId;
}
