package com.backend.tpi.ms_gestion_calculos.services;

import com.backend.tpi.ms_gestion_calculos.dtos.CoordenadaDTO;
import com.backend.tpi.ms_gestion_calculos.dtos.DistanciaRequestDTO;
import com.backend.tpi.ms_gestion_calculos.dtos.DistanciaResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.Map;

@Service
public class CalculoService {

    private static final Logger logger = LoggerFactory.getLogger(CalculoService.class);
    
    private static final int RADIO_TIERRA = 6371; // Radio de la Tierra en kilómetros
    
    @Autowired
    private RestClient rutasClient;
    
    @Value("${app.osrm.base-url:http://osrm:5000}")
    private String osrmBaseUrl;
    
    // Mapa de ciudades con sus coordenadas para el geocoding básico
    private static final Map<String, CoordenadaDTO> CIUDADES = new HashMap<>();
    
    static {
        CIUDADES.put("Buenos Aires", new CoordenadaDTO(-34.6037, -58.3816));
        CIUDADES.put("Rosario", new CoordenadaDTO(-32.9445, -60.6500));
        CIUDADES.put("Córdoba", new CoordenadaDTO(-31.4201, -64.1888));
        CIUDADES.put("Mendoza", new CoordenadaDTO(-32.8895, -68.8458));
        CIUDADES.put("La Plata", new CoordenadaDTO(-34.9215, -57.9545));
        CIUDADES.put("Mar del Plata", new CoordenadaDTO(-38.0055, -57.5426));
        CIUDADES.put("Tucumán", new CoordenadaDTO(-26.8083, -65.2176));
        CIUDADES.put("Salta", new CoordenadaDTO(-24.7859, -65.4117));
        CIUDADES.put("Santa Fe", new CoordenadaDTO(-31.6333, -60.7000));
    }

    public DistanciaResponseDTO calcularDistancia(DistanciaRequestDTO request) {
        logger.debug("Calculando distancia - origen: {}, destino: {}", request.getOrigen(), request.getDestino());
        
        try {
            // Geocodificar origen y destino
            CoordenadaDTO coordOrigen = geocodificar(request.getOrigen());
            CoordenadaDTO coordDestino = geocodificar(request.getDestino());
            
            if (coordOrigen == null || coordDestino == null) {
                logger.warn("No se pudieron geocodificar las direcciones, usando cálculo Haversine");
                double distancia = calcularDistanciaHaversine(request.getOrigen(), request.getDestino());
                logger.info("Distancia calculada (Haversine): {} km", distancia);
                return new DistanciaResponseDTO(distancia, null);
            }
            
            // Llamar a OSRM a través del microservicio de rutas
            logger.debug("Llamando a ms-rutas-transportistas para calcular distancia real con OSRM");
            DistanciaResponseDTO distanciaResp = rutasClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/osrm/distancia")
                            .queryParam("origenLat", coordOrigen.getLatitud())
                            .queryParam("origenLong", coordOrigen.getLongitud())
                            .queryParam("destinoLat", coordDestino.getLatitud())
                            .queryParam("destinoLong", coordDestino.getLongitud())
                            .build())
                    .retrieve()
                    .body(DistanciaResponseDTO.class);
            
            if (distanciaResp != null && distanciaResp.getDistancia() != null) {
                logger.info("Distancia calculada con OSRM: {} km", distanciaResp.getDistancia());
                return distanciaResp;
            } else {
                logger.warn("OSRM no devolvió resultado válido, usando cálculo Haversine como fallback");
                double distancia = calcularDistanciaHaversine(coordOrigen, coordDestino);
                logger.info("Distancia calculada (Haversine fallback): {} km", distancia);
                return new DistanciaResponseDTO(distancia, null);
            }
            
        } catch (Exception e) {
            logger.error("Error al calcular distancia con OSRM: {}", e.getMessage());
            logger.debug("Stack trace:", e);
            // Fallback a cálculo Haversine en caso de error
            double distancia = calcularDistanciaHaversine(request.getOrigen(), request.getDestino());
            logger.info("Distancia calculada (Haversine por error): {} km", distancia);
            return new DistanciaResponseDTO(distancia, null);
        }
    }
    

    private CoordenadaDTO geocodificar(String direccion) {
        if (direccion == null) {
            return null;
        }
        
        // Buscar coincidencia exacta
        if (CIUDADES.containsKey(direccion)) {
            logger.debug("Ciudad encontrada en mapa estático: {}", direccion);
            return CIUDADES.get(direccion);
        }
        
        // Buscar coincidencia parcial (case-insensitive)
        for (Map.Entry<String, CoordenadaDTO> entry : CIUDADES.entrySet()) {
            if (direccion.toLowerCase().contains(entry.getKey().toLowerCase())) {
                logger.debug("Ciudad encontrada por coincidencia parcial: {} -> {}", direccion, entry.getKey());
                return entry.getValue();
            }
        }
        
        logger.warn("No se encontró la ciudad en el mapa de geocodificación: {}", direccion);
        return null;
    }

    private double calcularDistanciaHaversine(String origen, String destino) {
        logger.debug("Aplicando fórmula de Haversine para calcular distancia");
        
        // Intentar geocodificar
        CoordenadaDTO coordOrigen = geocodificar(origen);
        CoordenadaDTO coordDestino = geocodificar(destino);
        
        if (coordOrigen != null && coordDestino != null) {
            return calcularDistanciaHaversine(coordOrigen, coordDestino);
        }
        
        // Fallback: coordenadas por defecto (Buenos Aires - Rosario)
        logger.warn("Usando coordenadas por defecto para cálculo Haversine");
        return calcularDistanciaHaversine(
                new CoordenadaDTO(-34.6037, -58.3816),
                new CoordenadaDTO(-32.9445, -60.6500)
        );
    }
    
    private double calcularDistanciaHaversine(CoordenadaDTO origen, CoordenadaDTO destino) {
        double lat1 = origen.getLatitud();
        double lon1 = origen.getLongitud();
        double lat2 = destino.getLatitud();
        double lon2 = destino.getLongitud();

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return Math.round(RADIO_TIERRA * c * 100.0) / 100.0; // Redondear a 2 decimales
    }
}
