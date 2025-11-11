package com.backend.tpi.ms_solicitudes.repositories;

import com.backend.tpi.ms_solicitudes.models.EstadoSolicitud;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EstadoSolicitudRepository extends JpaRepository<EstadoSolicitud, Long> {
	java.util.Optional<EstadoSolicitud> findByNombre(String nombre);
}
