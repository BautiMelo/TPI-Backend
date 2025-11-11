package com.backend.tpi.ms_solicitudes.repositories;

import com.backend.tpi.ms_solicitudes.models.Contenedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContenedorRepository extends JpaRepository<Contenedor, Long> {
	java.util.List<Contenedor> findByClienteId(Long clienteId);
}
