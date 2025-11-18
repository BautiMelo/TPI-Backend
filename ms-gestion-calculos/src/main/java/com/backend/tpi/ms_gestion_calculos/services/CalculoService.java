package com.backend.tpi.ms_gestion_calculos.services;

import com.backend.tpi.ms_gestion_calculos.dtos.CoordenadaDTO;
import com.backend.tpi.ms_gestion_calculos.dtos.DepositoDTO;
import com.backend.tpi.ms_gestion_calculos.dtos.DistanciaRequestDTO;
import com.backend.tpi.ms_gestion_calculos.dtos.DistanciaResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * Servicio de negocio para cálculo de Distancias
 * Calcula distancias entre ubicaciones usando OSRM (servicio externo) o fórmula de Haversine como fallback
 * Incluye geocodificación básica de ciudades argentinas
 */
@Service
public class CalculoService {

    private static final Logger logger = LoggerFactory.getLogger(CalculoService.class);
    
    private static final int RADIO_TIERRA = 6371; // Radio de la Tierra en kilómetros
    
    @Autowired
    private RestClient rutasClient;
    
    @Autowired
    private DepositoService depositoService;
    
    @Value("${app.osrm.base-url:http://osrm:5000}")
    private String osrmBaseUrl;

    /**
     * Calcula la distancia entre dos ubicaciones
     * Intenta usar OSRM (vía ms-rutas-transportistas) para rutas reales
     * Si falla, usa fórmula de Haversine como fallback
     * @param request Datos de origen y destino
     * @return Distancia en kilómetros y duración estimada (si está disponible)
     */
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
            String token = extractBearerToken();
            DistanciaResponseDTO distanciaResp = rutasClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/osrm/distancia")
                            .queryParam("origenLat", coordOrigen.getLatitud())
                            .queryParam("origenLong", coordOrigen.getLongitud())
                            .queryParam("destinoLat", coordDestino.getLatitud())
                            .queryParam("destinoLong", coordDestino.getLongitud())
                            .build())
                    .headers(h -> { if (token != null) h.setBearerAuth(token); })
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
    

    /**
     * Geocodifica una dirección a coordenadas lat/long
     * Soporta solo dos formatos:
     * 1. ID de depósito (número): consulta la base de datos
     * 2. Coordenadas directas en formato "lat,lon"
     * @param direccion ID de depósito o coordenadas
     * @return Coordenadas encontradas, o null si no se encuentra
     * @throws IllegalArgumentException si el formato no es válido
     */
    private CoordenadaDTO geocodificar(String direccion) {
        if (direccion == null || direccion.trim().isEmpty()) {
            logger.error("La dirección no puede ser null o vacía");
            throw new IllegalArgumentException("La dirección es obligatoria");
        }
        
        direccion = direccion.trim();
        
        // 1. Verificar si es un ID de depósito (número)
        if (esNumeroEntero(direccion)) {
            Long depositoId = Long.parseLong(direccion);
            logger.debug("Detectado ID de depósito: {}", depositoId);
            return consultarCoordenadasDeposito(depositoId);
        }
        
        // 2. Verificar si son coordenadas en formato "lat,lon"
        if (direccion.contains(",")) {
            try {
                String[] partes = direccion.split(",");
                if (partes.length == 2) {
                    double lat = Double.parseDouble(partes[0].trim());
                    double lon = Double.parseDouble(partes[1].trim());
                    
                    // Validar rangos de coordenadas
                    if (lat < -90 || lat > 90) {
                        throw new IllegalArgumentException("Latitud fuera de rango: " + lat + " (debe estar entre -90 y 90)");
                    }
                    if (lon < -180 || lon > 180) {
                        throw new IllegalArgumentException("Longitud fuera de rango: " + lon + " (debe estar entre -180 y 180)");
                    }
                    
                    logger.debug("Coordenadas parseadas directamente: lat={}, lon={}", lat, lon);
                    return new CoordenadaDTO(lat, lon);
                }
            } catch (NumberFormatException e) {
                logger.error("Error al parsear coordenadas: {}", direccion);
                throw new IllegalArgumentException("Formato de coordenadas inválido. Use: 'latitud,longitud'");
            }
        }
        
        logger.error("Formato de dirección no reconocido: {}. Use ID de depósito o coordenadas 'lat,lon'", direccion);
        throw new IllegalArgumentException("Formato inválido. Use: ID de depósito (ej: '1') o coordenadas (ej: '-34.6037,-58.3816')");
    }
    
    /**
     * Verifica si una cadena es un número entero válido
     * @param str Cadena a verificar
     * @return true si es un número entero, false en caso contrario
     */
    private boolean esNumeroEntero(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        try {
            Long.parseLong(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * Consulta las coordenadas de un depósito desde la base de datos
     * @param depositoId ID del depósito
     * @return Coordenadas del depósito
     * @throws RuntimeException si el depósito no existe o no tiene coordenadas
     */
    private CoordenadaDTO consultarCoordenadasDeposito(Long depositoId) {
        logger.debug("Consultando coordenadas del depósito ID: {}", depositoId);
        
        DepositoDTO deposito = depositoService.findById(depositoId);
        
        if (deposito.getLatitud() == null || deposito.getLongitud() == null) {
            logger.error("El depósito ID: {} no tiene coordenadas configuradas", depositoId);
            throw new RuntimeException("El depósito ID " + depositoId + " no tiene coordenadas configuradas");
        }
        
        logger.info("Coordenadas del depósito '{}' (ID: {}): lat={}, lon={}", 
                deposito.getNombre(), depositoId, deposito.getLatitud(), deposito.getLongitud());
        
        return new CoordenadaDTO(deposito.getLatitud(), deposito.getLongitud());
    }

    /**
     * Calcula distancia usando fórmula de Haversine entre dos ciudades
     * Geocodifica los nombres de las ciudades primero
     * @param origen Nombre de la ciudad origen
     * @param destino Nombre de la ciudad destino
     * @return Distancia en kilómetros (redondeada a 2 decimales)
     */
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
    
    /**
     * Calcula distancia usando fórmula de Haversine entre dos coordenadas
     * Fórmula matemática para calcular distancia en la superficie de una esfera
     * @param origen Coordenadas del punto origen
     * @param destino Coordenadas del punto destino
     * @return Distancia en kilómetros (redondeada a 2 decimales)
     */
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

    /**
     * Helper: extrae token Bearer del SecurityContext si existe
     */
    private String extractBearerToken() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken) {
            return ((JwtAuthenticationToken) auth).getToken().getTokenValue();
        }
        return null;
    }
}
