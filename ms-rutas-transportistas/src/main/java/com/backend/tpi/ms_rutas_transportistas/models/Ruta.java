package com.backend.tpi.ms_rutas_transportistas.models;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Ruta de transporte
 * Representa el camino completo desde origen a destino para una solicitud
 * Está compuesta por varios tramos
 */
@Entity
@Table(name = "rutas")
@Data
public class Ruta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ruta")
    private Long id;

    private Long idSolicitud;

    @CreationTimestamp
    private LocalDateTime fechaCreacion;

    @OneToMany(mappedBy = "ruta", cascade = CascadeType.ALL)
    private List<Tramo> tramos;
}
