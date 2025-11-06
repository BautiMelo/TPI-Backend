package com.backend.tpi.ms_rutas_transportistas.repositories;

import com.backend.tpi.ms_rutas_transportistas.models.Ruta;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RutaRepository extends JpaRepository<Ruta, Long> {
	Optional<Ruta> findByIdSolicitud(Long idSolicitud);
}
