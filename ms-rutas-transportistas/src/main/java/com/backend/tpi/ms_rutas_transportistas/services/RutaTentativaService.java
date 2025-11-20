package com.backend.tpi.ms_rutas_transportistas.services;

import com.backend.tpi.ms_rutas_transportistas.dtos.RutaTentativaDTO;
import com.backend.tpi.ms_rutas_transportistas.dtos.TramoTentativoDTO;
import com.backend.tpi.ms_rutas_transportistas.dtos.osrm.CoordenadaDTO;
import com.backend.tpi.ms_rutas_transportistas.dtos.osrm.RutaCalculadaDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.*;

/**
 * Servicio para calcular rutas tentativas considerando depósitos intermedios
 * Usa OSRM para calcular distancias reales entre depósitos
 * Puede calcular múltiples opciones y elegir la ruta más corta
 */
@Service
public class RutaTentativaService {

    private static final Logger logger = LoggerFactory.getLogger(RutaTentativaService.class);
    
    @Autowired
    private RestClient calculosClient;
    
    @Autowired
    private OSRMService osrmService;

    /**
     * Calcula la mejor ruta entre origen y destino
     * Si depositosIntermediosIds es null, busca automáticamente depósitos intermedios
     * y calcula múltiples opciones para elegir la más corta
     * 
     * @param origenDepositoId ID del depósito origen
     * @param destinoDepositoId ID del depósito destino
     * @param depositosIntermediosIds IDs de depósitos intermedios opcionales (en orden)
     * @param calcularVariantes Si es true, calcula múltiples rutas y elige la más corta
     * @return RutaTentativaDTO con la mejor ruta calculada
     */
    public RutaTentativaDTO calcularMejorRuta(
            Long origenDepositoId, 
            Long destinoDepositoId,
            List<Long> depositosIntermediosIds,
            boolean calcularVariantes) {
        
        logger.info("Calculando mejor ruta: origen={}, destino={}, intermedios={}, calcularVariantes={}", 
                origenDepositoId, destinoDepositoId, depositosIntermediosIds, calcularVariantes);
        
        try {
            // Si se especificaron depósitos intermedios, calcular solo esa ruta
            if (depositosIntermediosIds != null && !depositosIntermediosIds.isEmpty()) {
                logger.info("Calculando ruta con depósitos intermedios especificados");
                return calcularRutaTentativa(origenDepositoId, destinoDepositoId, depositosIntermediosIds);
            }
            
            // Si no se solicita calcular variantes, calcular ruta directa
            if (!calcularVariantes) {
                logger.info("Calculando ruta directa sin variantes");
                return calcularRutaTentativa(origenDepositoId, destinoDepositoId, null);
            }
            
            // Calcular múltiples variantes y elegir la más corta
            logger.info("Calculando múltiples variantes de ruta");
            List<RutaTentativaDTO> variantes = new ArrayList<>();
            
            // Variante 1: Ruta directa (sin intermedios)
            RutaTentativaDTO rutaDirecta = calcularRutaTentativa(origenDepositoId, destinoDepositoId, null);
            if (rutaDirecta.getExitoso()) {
                variantes.add(rutaDirecta);
                logger.debug("Variante directa: {} km", rutaDirecta.getDistanciaTotal());
            }
            
            // Variante 2-N: Probar con todos los depósitos disponibles como intermedios
            // (Para producción, esto debería ser más inteligente - solo depósitos cercanos a la ruta)
            List<Long> todosDepositos = obtenerTodosDepositosIds();
            todosDepositos.remove(origenDepositoId);
            todosDepositos.remove(destinoDepositoId);
            
            // Limitar a máximo 3 depósitos intermedios para evitar explosión combinatoria
            int maxIntermediarios = Math.min(3, todosDepositos.size());
            
            for (int i = 0; i < Math.min(maxIntermediarios, todosDepositos.size()); i++) {
                Long depositoIntermedio = todosDepositos.get(i);
                List<Long> intermedios = List.of(depositoIntermedio);
                
                RutaTentativaDTO rutaConIntermedio = calcularRutaTentativa(
                        origenDepositoId, destinoDepositoId, intermedios);
                
                if (rutaConIntermedio.getExitoso()) {
                    variantes.add(rutaConIntermedio);
                    logger.debug("Variante con depósito {}: {} km", 
                            depositoIntermedio, rutaConIntermedio.getDistanciaTotal());
                }
            }
            
            // Elegir la ruta más corta
            if (variantes.isEmpty()) {
                logger.error("No se pudo calcular ninguna variante de ruta");
                return RutaTentativaDTO.builder()
                        .exitoso(false)
                        .mensaje("No se pudo calcular ninguna ruta válida")
                        .build();
            }
            
            RutaTentativaDTO mejorRuta = variantes.stream()
                    .min(Comparator.comparing(RutaTentativaDTO::getDistanciaTotal))
                    .orElse(variantes.get(0));
            
            logger.info("Mejor ruta seleccionada: {} km de {} variantes calculadas", 
                    mejorRuta.getDistanciaTotal(), variantes.size());
            
            mejorRuta.setMensaje(String.format(
                    "Ruta óptima seleccionada (%d km) de %d variantes calculadas",
                    mejorRuta.getDistanciaTotal().intValue(), variantes.size()));
            
            return mejorRuta;
            
        } catch (Exception e) {
            logger.error("Error al calcular mejor ruta: {}", e.getMessage(), e);
            return RutaTentativaDTO.builder()
                    .exitoso(false)
                    .mensaje("Error al calcular ruta: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Calcula múltiples variantes de ruta (sin elegir la mejor) y devuelve la lista de opciones
     */
    public List<RutaTentativaDTO> calcularVariantes(Long origenDepositoId, Long destinoDepositoId) {
        List<RutaTentativaDTO> variantes = new ArrayList<>();
        try {
            // Variante directa
            RutaTentativaDTO directa = calcularRutaTentativa(origenDepositoId, destinoDepositoId, null);
            if (directa.getExitoso()) variantes.add(directa);

            List<Long> todosDepositos = obtenerTodosDepositosIds();
            todosDepositos.remove(origenDepositoId);
            todosDepositos.remove(destinoDepositoId);
            int maxIntermediarios = Math.min(3, todosDepositos.size());
            for (int i = 0; i < Math.min(maxIntermediarios, todosDepositos.size()); i++) {
                Long depositoIntermedio = todosDepositos.get(i);
                List<Long> intermedios = List.of(depositoIntermedio);
                RutaTentativaDTO rutaConIntermedio = calcularRutaTentativa(origenDepositoId, destinoDepositoId, intermedios);
                if (rutaConIntermedio.getExitoso()) variantes.add(rutaConIntermedio);
            }
        } catch (Exception e) {
            logger.error("Error al calcular variantes: {}", e.getMessage());
        }
        return variantes;
    }
    
    /**
     * Obtiene lista de todos los IDs de depósitos disponibles
     * @return Lista de IDs de depósitos
     */
    private List<Long> obtenerTodosDepositosIds() {
        try {
            String token = extractBearerToken();
            ResponseEntity<List<Map<String, Object>>> response = calculosClient.get()
                    .uri("/api/v1/depositos")
                    .headers(h -> { if (token != null) h.setBearerAuth(token); })
                    .retrieve()
                    .toEntity(new ParameterizedTypeReference<List<Map<String, Object>>>() {});
            
            if (response.getBody() != null) {
                return response.getBody().stream()
                        .map(dep -> ((Number) dep.get("id")).longValue())
                        .toList();
            }
        } catch (Exception e) {
            logger.warn("Error al obtener lista de depósitos: {}", e.getMessage());
        }
        return new ArrayList<>();
    }

    /**
     * Calcula una ruta tentativa entre origen y destino, considerando depósitos intermedios
     * Implementación simplificada: calcula la ruta directa entre origen y destino
     * 
     * @param origenDepositoId ID del depósito origen
     * @param destinoDepositoId ID del depósito destino
     * @param depositosIntermediosIds IDs de depósitos intermedios opcionales (en orden)
     * @return RutaTentativaDTO con la ruta calculada
     */
    public RutaTentativaDTO calcularRutaTentativa(
            Long origenDepositoId, 
            Long destinoDepositoId,
            List<Long> depositosIntermediosIds) {
        
        logger.info("Calculando ruta tentativa: origen={}, destino={}, intermedios={}", 
                origenDepositoId, destinoDepositoId, depositosIntermediosIds);
        
        try {
            // Construir lista completa de depósitos en orden
            List<Long> todosDepositosIds = new ArrayList<>();
            todosDepositosIds.add(origenDepositoId);
            if (depositosIntermediosIds != null && !depositosIntermediosIds.isEmpty()) {
                todosDepositosIds.addAll(depositosIntermediosIds);
            }
            todosDepositosIds.add(destinoDepositoId);
            
            // Obtener información de todos los depósitos
            Map<Long, Map<String, Object>> depositosInfo = obtenerInfoDepositos(todosDepositosIds);
            
            // Calcular tramos entre depósitos consecutivos
            List<TramoTentativoDTO> tramos = new ArrayList<>();
            double distanciaTotal = 0.0;
            double duracionTotalHoras = 0.0;
            
            for (int i = 0; i < todosDepositosIds.size() - 1; i++) {
                Long depOrigen = todosDepositosIds.get(i);
                Long depDestino = todosDepositosIds.get(i + 1);
                
                Map<String, Object> infoOrigen = depositosInfo.get(depOrigen);
                Map<String, Object> infoDestino = depositosInfo.get(depDestino);
                
                if (infoOrigen == null || infoDestino == null) {
                    logger.warn("No se pudo obtener información de depósitos {} o {}", depOrigen, depDestino);
                    continue;
                }
                
                // Calcular distancia y duración usando OSRM
                CoordenadaDTO coordOrigen = new CoordenadaDTO(
                        ((Number) infoOrigen.get("latitud")).doubleValue(),
                        ((Number) infoOrigen.get("longitud")).doubleValue()
                );
                CoordenadaDTO coordDestino = new CoordenadaDTO(
                        ((Number) infoDestino.get("latitud")).doubleValue(),
                        ((Number) infoDestino.get("longitud")).doubleValue()
                );
                
                RutaCalculadaDTO rutaCalculada = osrmService.calcularRuta(coordOrigen, coordDestino);
                
                TramoTentativoDTO tramo = TramoTentativoDTO.builder()
                        .orden(i + 1)
                        .origenDepositoId(depOrigen)
                        .origenDepositoNombre((String) infoOrigen.get("nombre"))
                        .destinoDepositoId(depDestino)
                        .destinoDepositoNombre((String) infoDestino.get("nombre"))
                        .distanciaKm(rutaCalculada.getDistanciaKm())
                        .duracionHoras(rutaCalculada.getDuracionHoras())
                        .build();
                
                tramos.add(tramo);
                distanciaTotal += rutaCalculada.getDistanciaKm();
                if (rutaCalculada.getDuracionHoras() != null) {
                    duracionTotalHoras += rutaCalculada.getDuracionHoras();
                }
            }
            
            // Construir listas de nombres
            List<String> nombresDepositos = todosDepositosIds.stream()
                    .map(id -> depositosInfo.get(id))
                    .filter(Objects::nonNull)
                    .map(info -> (String) info.get("nombre"))
                    .toList();
            
            RutaTentativaDTO resultado = RutaTentativaDTO.builder()
                    .depositosIds(todosDepositosIds)
                    .depositosNombres(nombresDepositos)
                    .distanciaTotal(Math.round(distanciaTotal * 100.0) / 100.0)
                    .duracionTotalHoras(Math.round(duracionTotalHoras * 100.0) / 100.0)
                    .numeroTramos(tramos.size())
                    .tramos(tramos)
                    .exitoso(true)
                    .mensaje("Ruta tentativa calculada exitosamente con " + tramos.size() + " tramos")
                    .build();
            
            logger.info("Ruta tentativa calculada: {} km, {} tramos", resultado.getDistanciaTotal(), resultado.getNumeroTramos());
            return resultado;
            
        } catch (Exception e) {
            logger.error("Error al calcular ruta tentativa: {}", e.getMessage(), e);
            return RutaTentativaDTO.builder()
                    .exitoso(false)
                    .mensaje("Error al calcular ruta: " + e.getMessage())
                    .build();
        }
    }
    
    /**
     * Obtiene información de múltiples depósitos desde ms-gestion-calculos
     * @param depositosIds Lista de IDs de depósitos
     * @return Mapa con ID -> información del depósito
     */
    private Map<Long, Map<String, Object>> obtenerInfoDepositos(List<Long> depositosIds) {
        Map<Long, Map<String, Object>> resultado = new HashMap<>();
        String token = extractBearerToken();
        
        for (Long depositoId : depositosIds) {
            try {
                ResponseEntity<Map<String, Object>> response = calculosClient.get()
                        .uri("/api/v1/depositos/{id}/coordenadas", depositoId)
                        .headers(h -> { if (token != null) h.setBearerAuth(token); })
                        .retrieve()
                        .toEntity(new ParameterizedTypeReference<Map<String, Object>>() {});
                
                if (response.getBody() != null) {
                    resultado.put(depositoId, response.getBody());
                    logger.debug("Información de depósito {} obtenida: {}", depositoId, response.getBody());
                }
            } catch (Exception e) {
                logger.error("Error al obtener información del depósito {}: {}", depositoId, e.getMessage());
            }
        }
        
        return resultado;
    }
    
    /**
     * Calcula una ruta directa sin depósitos intermedios
     * @param origenDepositoId ID del depósito origen
     * @param destinoDepositoId ID del depósito destino
     * @return RutaTentativaDTO con la ruta directa
     */
    public RutaTentativaDTO calcularRutaDirecta(Long origenDepositoId, Long destinoDepositoId) {
        return calcularRutaTentativa(origenDepositoId, destinoDepositoId, null);
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
