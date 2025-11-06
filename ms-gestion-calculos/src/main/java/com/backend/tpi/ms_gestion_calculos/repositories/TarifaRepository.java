package com.backend.tpi.ms_gestion_calculos.repositories;

import com.backend.tpi.ms_gestion_calculos.models.Tarifa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TarifaRepository extends JpaRepository<Tarifa, Long> {
	// Devuelve la tarifa más reciente (por id) para usar como tarifa activa por defecto
	Tarifa findTopByOrderByIdDesc();
}
