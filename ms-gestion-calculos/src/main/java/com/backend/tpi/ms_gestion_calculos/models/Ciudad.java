package com.backend.tpi.ms_gestion_calculos.models;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "ciudades")
public class Ciudad {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ciudad")
    private Long id;

    private String nombre;
}
