package com.backend.tpi.ms_solicitudes.services;

import com.backend.tpi.ms_solicitudes.dtos.CoordenadaDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

/**
 * Servicio para geocodificación de direcciones
 * Convierte direcciones de texto a coordenadas geográficas
 * utilizando el microservicio ms-gestion-calculos
 */
@Service
public class GeocodificacionService {

    private static final Logger logger = LoggerFactory.getLogger(GeocodificacionService.class);

    @Autowired
    private RestClient calculosClient;

    /**
     * Geocodifica una dirección convirtiéndola a coordenadas
     * Soporta:
     * 1. ID de depósito (número entero): consulta la base de datos vía ms-gestion-calculos
     * 2. Coordenadas directas en formato "lat,lon": las retorna parseadas
     * 
     * @param direccion ID de depósito o coordenadas en formato "lat,lon"
     * @return Coordenadas geográficas, o null si no se puede geocodificar
     */
    public CoordenadaDTO geocodificar(String direccion) {
        if (direccion == null || direccion.trim().isEmpty()) {
            logger.warn("Intento de geocodificar dirección null o vacía");
            return null;
        }
        
        direccion = direccion.trim();
        logger.debug("Geocodificando dirección: {}", direccion);
        
        // 1. Si es un número, es un ID de depósito - delegar a ms-gestion-calculos
        if (esNumeroEntero(direccion)) {
            try {
                logger.debug("Dirección detectada como ID de depósito: {}", direccion);
                return geocodificarViaCalculos(direccion);
            } catch (Exception e) {
                logger.error("Error al geocodificar ID de depósito {}: {}", direccion, e.getMessage());
                return null;
            }
        }
        
        // 2. Si contiene coma, intentar parsear como coordenadas "lat,lon"
        if (direccion.contains(",")) {
            try {
                String[] partes = direccion.split(",");
                if (partes.length == 2) {
                    double lat = Double.parseDouble(partes[0].trim());
                    double lon = Double.parseDouble(partes[1].trim());
                    
                    // Validar rangos
                    if (lat < -90 || lat > 90) {
                        logger.error("Latitud fuera de rango: {} (debe estar entre -90 y 90)", lat);
                        return null;
                    }
                    if (lon < -180 || lon > 180) {
                        logger.error("Longitud fuera de rango: {} (debe estar entre -180 y 180)", lon);
                        return null;
                    }
                    
                    logger.debug("Coordenadas parseadas directamente: lat={}, lon={}", lat, lon);
                    return new CoordenadaDTO(lat, lon);
                }
            } catch (NumberFormatException e) {
                logger.error("Error al parsear coordenadas: {}", direccion);
                return null;
            }
        }
        
        // 3. Intentar geocodificar como dirección de texto vía ms-gestion-calculos
        try {
            logger.debug("Intentando geocodificar dirección de texto vía ms-gestion-calculos: {}", direccion);
            return geocodificarViaCalculos(direccion);
        } catch (Exception e) {
            logger.error("Error al geocodificar dirección '{}': {}", direccion, e.getMessage());
            return null;
        }
    }

    /**
     * Llama al microservicio ms-gestion-calculos para obtener coordenadas de un depósito
     * Utiliza el endpoint /api/v1/depositos/{id}/coordenadas
     * 
     * @param direccion ID del depósito
     * @return Coordenadas obtenidas del servicio
     */
    private CoordenadaDTO geocodificarViaCalculos(String direccion) {
        try {
            Long depositoId = Long.parseLong(direccion);
            String token = extractBearerToken();
            
            logger.debug("Consultando coordenadas del depósito ID {} vía ms-gestion-calculos", depositoId);
            
            org.springframework.http.ResponseEntity<java.util.Map<String, Object>> response = calculosClient.get()
                .uri("/api/v1/depositos/" + depositoId + "/coordenadas")
                .headers(h -> { if (token != null) h.setBearerAuth(token); })
                .retrieve()
                .toEntity(new org.springframework.core.ParameterizedTypeReference<java.util.Map<String, Object>>() {});
            
            java.util.Map<String, Object> responseBody = response.getBody();
            
            if (responseBody != null && responseBody.containsKey("latitud") && responseBody.containsKey("longitud")) {
                Object latObj = responseBody.get("latitud");
                Object lonObj = responseBody.get("longitud");
                
                Double latitud = null;
                Double longitud = null;
                
                // Convertir a Double independientemente del tipo retornado
                if (latObj instanceof Number) {
                    latitud = ((Number) latObj).doubleValue();
                }
                if (lonObj instanceof Number) {
                    longitud = ((Number) lonObj).doubleValue();
                }
                
                if (latitud != null && longitud != null) {
                    logger.info("Coordenadas obtenidas del depósito ID {}: lat={}, lon={}", depositoId, latitud, longitud);
                    return new CoordenadaDTO(latitud, longitud);
                }
            }
            
            logger.warn("No se pudieron obtener coordenadas del depósito ID: {}", depositoId);
            return null;
        } catch (NumberFormatException e) {
            logger.error("La dirección no es un ID de depósito válido: {}", direccion);
            throw new RuntimeException("ID de depósito inválido: " + direccion, e);
        } catch (Exception e) {
            logger.error("Error al consultar coordenadas del depósito {}: {}", direccion, e.getMessage());
            throw new RuntimeException("Error al geocodificar depósito: " + direccion, e);
        }
    }

    /**
     * Verifica si una cadena representa un número entero
     * @param str Cadena a verificar
     * @return true si es un número entero válido
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
     * Convierte Double a BigDecimal para almacenamiento en base de datos
     * @param valor Valor Double
     * @return BigDecimal equivalente
     */
    public BigDecimal toBigDecimal(Double valor) {
        return valor != null ? BigDecimal.valueOf(valor) : null;
    }

    /**
     * Extrae el token Bearer del SecurityContext si existe
     * @return Token JWT o null
     */
    private String extractBearerToken() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken) {
            return ((JwtAuthenticationToken) auth).getToken().getTokenValue();
        }
        return null;
    }
}
