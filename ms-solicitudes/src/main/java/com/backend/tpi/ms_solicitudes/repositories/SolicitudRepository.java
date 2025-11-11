package com.backend.tpi.ms_solicitudes.repositories;

import com.backend.tpi.ms_solicitudes.models.Solicitud;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {
	// Buscar solicitudes por cliente
	java.util.List<Solicitud> findByClienteId(Long clienteId);

	// Buscar solicitudes por nombre de estado (join automático por propiedad estado.nombre)
	java.util.List<Solicitud> findByEstado_Nombre(String nombre);

	// Buscar solicitudes por ruta
	java.util.List<Solicitud> findByRutaId(Long rutaId);
}
