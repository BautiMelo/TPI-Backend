package com.backend.tpi.ms_rutas_transportistas.services;

import com.backend.tpi.ms_rutas_transportistas.dtos.DistanciaResponseDTO;
import com.backend.tpi.ms_rutas_transportistas.dtos.osrm.CoordenadaDTO;
import com.backend.tpi.ms_rutas_transportistas.dtos.osrm.RutaCalculadaDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Servicio para integración con servicios de mapas y cálculo de distancias.
 * Puede usar OSRM directamente o delegar al microservicio de cálculos.
 */
@Service
@Slf4j
public class MapsService {

    @Autowired
    private OSRMService osrmService;

    /**
     * Obtiene distancia llamando al microservicio de cálculos (Google Maps)
     * @deprecated Usar getDistanciaConOSRM para mejor rendimiento
     */
    @Deprecated
    public DistanciaResponseDTO getDistancia(String origen, String destino) {
        // Legacy endpoint kept for compatibility. Now delegates to OSRM by parsing "lat,lon" strings.
        try {
            if (origen == null || destino == null) {
                return new DistanciaResponseDTO(0.0, 0.0);
            }
            String[] oParts = origen.split("[,;]");
            String[] dParts = destino.split("[,;]");
            if (oParts.length < 2 || dParts.length < 2) {
                log.warn("Formato de coordenadas inválido en getDistancia: {} - {}", origen, destino);
                return new DistanciaResponseDTO(0.0, 0.0);
            }
            double oLat = Double.parseDouble(oParts[0].trim());
            double oLon = Double.parseDouble(oParts[1].trim());
            double dLat = Double.parseDouble(dParts[0].trim());
            double dLon = Double.parseDouble(dParts[1].trim());

            CoordenadaDTO co = new CoordenadaDTO(oLat, oLon);
            CoordenadaDTO cd = new CoordenadaDTO(dLat, dLon);

            RutaCalculadaDTO ruta = osrmService.calcularRuta(co, cd);
            if (ruta != null && ruta.isExitoso()) {
                return DistanciaResponseDTO.builder()
                        .distancia(ruta.getDistanciaKm())
                        .duracion(ruta.getDuracionHoras())
                        .build();
            }
            return DistanciaResponseDTO.builder().distancia(0.0).duracion(0.0).build();
        } catch (Exception ex) {
            log.error("Error al obtener distancia delegando a OSRM", ex);
            return DistanciaResponseDTO.builder().distancia(0.0).duracion(0.0).build();
        }
    }

    /**
     * Calcula distancia y duración usando OSRM (más rápido y gratuito)
     * @param origen Coordenadas de origen (lat, lon)
     * @param destino Coordenadas de destino (lat, lon)
     * @return Distancia en km y duración en horas
     */
    public DistanciaResponseDTO getDistanciaConOSRM(CoordenadaDTO origen, CoordenadaDTO destino) {
        try {
            RutaCalculadaDTO ruta = osrmService.calcularRuta(origen, destino);
            
            if (ruta.isExitoso()) {
                return DistanciaResponseDTO.builder()
                        .distancia(ruta.getDistanciaKm())
                        .duracion(ruta.getDuracionHoras())
                        .build();
            } else {
                log.warn("No se pudo calcular ruta con OSRM: {}", ruta.getMensaje());
                return DistanciaResponseDTO.builder()
                        .distancia(0.0)
                        .duracion(0.0)
                        .build();
            }
        } catch (Exception ex) {
            log.error("Error al calcular distancia con OSRM", ex);
            return DistanciaResponseDTO.builder()
                    .distancia(0.0)
                    .duracion(0.0)
                    .build();
        }
    }
}
