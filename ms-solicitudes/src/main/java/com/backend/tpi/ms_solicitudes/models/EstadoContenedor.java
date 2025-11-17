package com.backend.tpi.ms_solicitudes.models;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Estado de un contenedor
 * Ej: "Disponible", "En Tránsito", "Entregado", etc.
 */
@Entity
@Data
@Table(name = "estados_contenedor")
public class EstadoContenedor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_estado")
    private Long id;

    private String nombre;
}
