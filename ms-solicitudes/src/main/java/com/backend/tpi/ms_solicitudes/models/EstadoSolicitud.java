package com.backend.tpi.ms_solicitudes.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

/**
 * Estado de una solicitud
 * Ej: "Pendiente", "En Proceso", "Completada", etc.
 */
@Entity
@Data
public class EstadoSolicitud {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
}
