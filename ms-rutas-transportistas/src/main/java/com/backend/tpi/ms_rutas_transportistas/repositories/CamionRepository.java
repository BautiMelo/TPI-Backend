package com.backend.tpi.ms_rutas_transportistas.repositories;

import com.backend.tpi.ms_rutas_transportistas.models.Camion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio JPA para la entidad Camion
 * Gestiona las operaciones de persistencia de los camiones y sus asignaciones
 */
@Repository
public interface CamionRepository extends JpaRepository<Camion, Long> {
}
