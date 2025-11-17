package com.backend.tpi.ms_solicitudes.repositories;

import com.backend.tpi.ms_solicitudes.models.Contenedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para acceso a datos de Contenedores
 */
@Repository
public interface ContenedorRepository extends JpaRepository<Contenedor, Long> {
	/**
	 * Busca contenedores por cliente
	 * @param clienteId ID del cliente
	 * @return Lista de contenedores del cliente
	 */
	java.util.List<Contenedor> findByClienteId(Long clienteId);
}
