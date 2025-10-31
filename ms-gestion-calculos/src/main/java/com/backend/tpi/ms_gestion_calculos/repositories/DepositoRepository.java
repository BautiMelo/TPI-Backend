package com.backend.tpi.ms_gestion_calculos.repositories;

import com.backend.tpi.ms_gestion_calculos.models.Deposito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepositoRepository extends JpaRepository<Deposito, Long> {
}
