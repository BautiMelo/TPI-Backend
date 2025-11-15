package com.backend.tpi.ms_gestion_calculos.models;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "tarifa_volumen_peso")
public class TarifaVolumenPeso {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "id_tarifa")
    private Tarifa tarifa;
    
    private Double volumenMin;
    private Double volumenMax;
    private Double pesoMin;
    private Double pesoMax;
    private Double costoPorKmBase;
}
