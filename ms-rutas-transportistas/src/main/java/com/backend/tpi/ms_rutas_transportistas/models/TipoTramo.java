package com.backend.tpi.ms_rutas_transportistas.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

/**
 * Tipo de tramo
 * Ej: "Nacional", "Internacional", "Urbano", etc.
 */
@Entity
@Data
public class TipoTramo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
}
