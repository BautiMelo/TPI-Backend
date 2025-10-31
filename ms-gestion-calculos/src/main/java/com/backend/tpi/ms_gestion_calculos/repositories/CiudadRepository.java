package com.backend.tpi.ms_gestion_calculos.repositories;

import com.backend.tpi.ms_gestion_calculos.models.Ciudad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CiudadRepository extends JpaRepository<Ciudad, Long> {
}
