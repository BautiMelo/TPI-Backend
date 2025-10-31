package com.backend.tpi.ms_rutas_transportistas.repositories;

import com.backend.tpi.ms_rutas_transportistas.models.TipoTramo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TipoTramoRepository extends JpaRepository<TipoTramo, Long> {
}
