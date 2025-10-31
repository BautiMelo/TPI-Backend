package com.backend.tpi.ms_rutas_transportistas.models;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Tramo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String origen;
    private String destino;
    private Double distancia;

    @ManyToOne
    private Ruta ruta;

    @ManyToOne
    private TipoTramo tipoTramo;

    @ManyToOne
    private EstadoTramo estado;
}
