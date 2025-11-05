package com.backend.tpi.ms_solicitudes.repositories;

import com.backend.tpi.ms_solicitudes.models.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
}
