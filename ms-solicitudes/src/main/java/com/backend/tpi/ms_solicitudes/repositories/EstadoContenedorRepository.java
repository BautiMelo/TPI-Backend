package com.backend.tpi.ms_solicitudes.repositories;

import com.backend.tpi.ms_solicitudes.models.EstadoContenedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EstadoContenedorRepository extends JpaRepository<EstadoContenedor, Long> {
	java.util.Optional<EstadoContenedor> findByNombre(String nombre);
}
