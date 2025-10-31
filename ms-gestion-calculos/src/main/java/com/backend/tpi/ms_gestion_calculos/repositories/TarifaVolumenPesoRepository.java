package com.backend.tpi.ms_gestion_calculos.repositories;

import com.backend.tpi.ms_gestion_calculos.models.TarifaVolumenPeso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TarifaVolumenPesoRepository extends JpaRepository<TarifaVolumenPeso, Long> {
}
