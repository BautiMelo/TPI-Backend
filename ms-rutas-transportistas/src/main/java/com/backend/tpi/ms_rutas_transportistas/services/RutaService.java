package com.backend.tpi.ms_rutas_transportistas.services;

import com.backend.tpi.ms_rutas_transportistas.dtos.CreateRutaDTO;
import com.backend.tpi.ms_rutas_transportistas.dtos.RutaDTO;
import com.backend.tpi.ms_rutas_transportistas.dtos.RutaTentativaDTO;
import com.backend.tpi.ms_rutas_transportistas.dtos.TramoTentativoDTO;
import com.backend.tpi.ms_rutas_transportistas.models.Ruta;
import com.backend.tpi.ms_rutas_transportistas.models.Tramo;
import com.backend.tpi.ms_rutas_transportistas.repositories.RutaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Servicio de negocio para Rutas
 * Gestiona la creación de rutas y la asignación de transportistas a tramos
 */
@Service
public class RutaService {

    private static final Logger logger = LoggerFactory.getLogger(RutaService.class);

    @Autowired
    private RutaRepository rutaRepository;
    
    @Autowired
    private TramoService tramoService;
    
    @Autowired
    private RutaTentativaService rutaTentativaService;
    
    @Autowired
    private OSRMService osrmService;
    
    @Autowired
    private com.backend.tpi.ms_rutas_transportistas.repositories.TramoRepository tramoRepository;
    
    @Autowired
    private org.springframework.web.client.RestClient calculosClient;

    // Manual mapping - ModelMapper removed

    /**
     * Crea una nueva ruta para una solicitud
     * Si se proporcionan IDs de depósitos (origen, destino y opcionalmente intermedios),
     * automáticamente calcula la ruta tentativa y crea los tramos correspondientes.
     * 
     * @param createRutaDTO Datos de la ruta a crear (incluye idSolicitud y datos de depósitos)
     * @return DTO de la ruta creada
     * @throws IllegalArgumentException si los datos son inválidos o ya existe una ruta para la solicitud
     */
    @org.springframework.transaction.annotation.Transactional
    public RutaDTO create(CreateRutaDTO createRutaDTO) {
        // Validar datos de entrada
        if (createRutaDTO == null) {
            logger.error("CreateRutaDTO no puede ser null");
            throw new IllegalArgumentException("Los datos de la ruta no pueden ser null");
        }
        if (createRutaDTO.getIdSolicitud() == null) {
            logger.error("IdSolicitud no puede ser null");
            throw new IllegalArgumentException("El ID de la solicitud es obligatorio");
        }
        
        // Verificar si ya existe una ruta para esta solicitud
        Optional<Ruta> rutaExistente = rutaRepository.findByIdSolicitud(createRutaDTO.getIdSolicitud());
        if (rutaExistente.isPresent()) {
            logger.error("Ya existe una ruta para la solicitud ID: {}", createRutaDTO.getIdSolicitud());
            throw new IllegalArgumentException("Ya existe una ruta para la solicitud ID: " + createRutaDTO.getIdSolicitud());
        }
        
        logger.debug("Creando nueva ruta para solicitud ID: {}", createRutaDTO.getIdSolicitud());
        Ruta ruta = new Ruta();
        ruta.setIdSolicitud(createRutaDTO.getIdSolicitud());
        ruta = rutaRepository.save(ruta);
        logger.info("Ruta creada exitosamente con ID: {} para solicitud ID: {}", ruta.getId(), createRutaDTO.getIdSolicitud());
        
        // Si se proporcionaron depósitos, calcular ruta tentativa y crear tramos automáticamente
        if (createRutaDTO.getOrigenDepositoId() != null && createRutaDTO.getDestinoDepositoId() != null) {
            logger.info("Depósitos especificados - calculando ruta tentativa automáticamente");
            try {
                // Calcular la mejor ruta (con o sin variantes)
                boolean calcularVariantes = createRutaDTO.getCalcularRutaOptima() != null && 
                                           createRutaDTO.getCalcularRutaOptima();
                
                RutaTentativaDTO rutaTentativa = rutaTentativaService.calcularMejorRuta(
                        createRutaDTO.getOrigenDepositoId(),
                        createRutaDTO.getDestinoDepositoId(),
                        createRutaDTO.getDepositosIntermediosIds(),
                        calcularVariantes
                );
                
                if (rutaTentativa.getExitoso() && rutaTentativa.getTramos() != null) {
                    logger.info("Ruta tentativa calculada: {} km, {} tramos - creando tramos automáticamente",
                            rutaTentativa.getDistanciaTotal(), rutaTentativa.getNumeroTramos());
                    
                    // Crear tramos basados en la ruta calculada
                    for (TramoTentativoDTO tramoTentativo : rutaTentativa.getTramos()) {
                        Tramo tramo = new Tramo();
                        tramo.setRuta(ruta);
                        tramo.setOrden(tramoTentativo.getOrden());
                        tramo.setOrigenDepositoId(tramoTentativo.getOrigenDepositoId());
                        tramo.setDestinoDepositoId(tramoTentativo.getDestinoDepositoId());
                        tramo.setDistancia(tramoTentativo.getDistanciaKm());
                        tramo.setDuracionHoras(tramoTentativo.getDuracionHoras());
                        tramo.setGeneradoAutomaticamente(true); // Marcar como generado automáticamente
                        
                        // Buscar y asignar estado PENDIENTE
                        // Por ahora dejamos estado null - debería buscarse de la BD
                        
                        tramoService.save(tramo);
                        logger.debug("Tramo {} creado: {} -> {} ({} km)", 
                                tramo.getOrden(),
                                tramoTentativo.getOrigenDepositoNombre(),
                                tramoTentativo.getDestinoDepositoNombre(),
                                tramoTentativo.getDistanciaKm());
                    }
                    
                    logger.info("Creados {} tramos automáticamente para la ruta ID: {}", 
                            rutaTentativa.getNumeroTramos(), ruta.getId());
                } else {
                    logger.warn("No se pudo calcular ruta tentativa: {}", rutaTentativa.getMensaje());
                }
            } catch (Exception e) {
                logger.error("Error al calcular y crear tramos automáticos: {}", e.getMessage(), e);
                // No lanzamos excepción - la ruta se creó exitosamente, solo falló la creación de tramos
            }
        } else {
            logger.info("No se especificaron depósitos - ruta creada sin tramos automáticos");
        }
        
        return toDto(ruta);
    }

    /**
     * Obtiene todas las rutas del sistema
     * @return Lista de DTOs de rutas
     */
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<RutaDTO> findAll() {
        logger.debug("Buscando todas las rutas");
        List<RutaDTO> rutas = rutaRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        logger.debug("Encontradas {} rutas", rutas.size());
        return rutas;
    }

    /**
     * Busca una ruta por su ID
     * @param id ID de la ruta
     * @return DTO de la ruta encontrada, o null si no existe
     */
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public RutaDTO findById(Long id) {
        logger.debug("Buscando ruta por ID: {}", id);
        Optional<Ruta> ruta = rutaRepository.findById(id);
        if (ruta.isPresent()) {
            logger.debug("Ruta encontrada con ID: {}", id);
        } else {
            logger.warn("Ruta no encontrada con ID: {}", id);
        }
        return ruta.map(this::toDto).orElse(null);
    }

    /**
     * Elimina una ruta por su ID
     * @param id ID de la ruta a eliminar
     */
    @org.springframework.transaction.annotation.Transactional
    public void delete(Long id) {
        logger.info("Eliminando ruta ID: {}", id);
        rutaRepository.deleteById(id);
        logger.debug("Ruta ID: {} eliminada de la base de datos", id);
    }

    /**
     * Convierte una entidad Ruta a su DTO
     * @param ruta Entidad ruta
     * @return DTO de la ruta
     */
    private RutaDTO toDto(Ruta ruta) {
        if (ruta == null) return null;
        RutaDTO dto = new RutaDTO();
        dto.setId(ruta.getId());
        dto.setIdSolicitud(ruta.getIdSolicitud());
        dto.setFechaCreacion(ruta.getFechaCreacion());
        return dto;
    }

    // ----- Integration/stub methods -----
    /**
     * Asigna un transportista (camión) a una ruta
     * Busca el primer tramo sin asignar y le asigna el transportista
     * @param rutaId ID de la ruta
     * @param transportistaId ID del camión a asignar
     * @return DTO del tramo con el transportista asignado
     * @throws IllegalArgumentException si la ruta no existe o el transportistaId es null
     */
    @org.springframework.transaction.annotation.Transactional
    public Object assignTransportista(Long rutaId, Long transportistaId) {
        // Validar que transportistaId no sea null
        if (transportistaId == null) {
            logger.error("TransportistaId no puede ser null");
            throw new IllegalArgumentException("El ID del transportista no puede ser null");
        }
        
        logger.info("Asignando transportista ID: {} a ruta ID: {}", transportistaId, rutaId);
        Optional<Ruta> optionalRuta = rutaRepository.findById(rutaId);
        if (optionalRuta.isEmpty()) {
            logger.error("No se puede asignar transportista - Ruta no encontrada con ID: {}", rutaId);
            throw new IllegalArgumentException("Ruta not found: " + rutaId);
        }
        // Find first unassigned tramo for this ruta and delegate assignment to TramoService
        logger.debug("Buscando tramos sin asignar para ruta ID: {}", rutaId);
        java.util.List<com.backend.tpi.ms_rutas_transportistas.dtos.TramoDTO> tramos = tramoService.findByRutaId(rutaId);
        if (tramos == null || tramos.isEmpty()) {
            logger.warn("No se encontraron tramos para ruta ID: {}", rutaId);
            Map<String, Object> result = new HashMap<>();
            result.put("rutaId", rutaId);
            result.put("status", "no_tramos");
            return result;
        }
        for (com.backend.tpi.ms_rutas_transportistas.dtos.TramoDTO t : tramos) {
            if (t.getCamionDominio() == null || t.getCamionDominio().isEmpty()) {
                logger.debug("Tramo sin asignar encontrado - ID: {}, asignando transportista", t.getId());
                // delegate to TramoService to assign the camion and persist
                com.backend.tpi.ms_rutas_transportistas.dtos.TramoDTO assigned = tramoService.assignTransportista(t.getId(), transportistaId);
                logger.info("Transportista ID: {} asignado exitosamente a ruta ID: {}", transportistaId, rutaId);
                return assigned != null ? assigned : java.util.Collections.emptyMap();
            }
        }
        logger.info("Todos los tramos de la ruta ID: {} ya tienen transportista asignado", rutaId);
        Map<String, Object> result = new HashMap<>();
        result.put("rutaId", rutaId);
        result.put("status", "all_tramos_assigned");
        return result;
    }

    /**
     * Busca una ruta por el ID de la solicitud asociada
     * @param solicitudId ID de la solicitud
     * @return DTO de la ruta encontrada, o null si no existe
     */
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Object findBySolicitudId(Long solicitudId) {
        logger.debug("Buscando ruta por solicitud ID: {}", solicitudId);
        Optional<Ruta> ruta = rutaRepository.findByIdSolicitud(solicitudId);
        if (ruta.isPresent()) {
            logger.debug("Ruta encontrada para solicitud ID: {} - ruta ID: {}", solicitudId, ruta.get().getId());
        } else {
            logger.warn("No se encontró ruta para solicitud ID: {}", solicitudId);
        }
        return ruta.map(this::toDto).orElse(null);
    }

    /**
     * Calcula las distancias y duraciones de todos los tramos de una ruta usando OSRM con ruta múltiple
     * Este método busca todos los tramos ordenados de la ruta, extrae sus coordenadas,
     * llama al endpoint de ruta múltiple de OSRM para calcular la ruta óptima completa,
     * y actualiza cada tramo con las distancias y duraciones calculadas.
     * 
     * @param rutaId ID de la ruta
     * @return Map con información de la ruta calculada (distanciaTotal, duracionTotal, tramosActualizados)
     * @throws IllegalArgumentException si la ruta no existe o no tiene tramos
     */
    @org.springframework.transaction.annotation.Transactional
    public Map<String, Object> calcularRutaCompleta(Long rutaId) {
        logger.info("Calculando ruta completa para ruta ID: {}", rutaId);
        
        // Verificar que la ruta existe
        Optional<Ruta> optionalRuta = rutaRepository.findById(rutaId);
        if (optionalRuta.isEmpty()) {
            logger.error("No se puede calcular - Ruta no encontrada con ID: {}", rutaId);
            throw new IllegalArgumentException("Ruta no encontrada con ID: " + rutaId);
        }
        
        // Buscar todos los tramos de la ruta ordenados
        List<Tramo> tramos = tramoRepository.findByRutaId(rutaId);
        if (tramos == null || tramos.isEmpty()) {
            logger.error("No se puede calcular - Ruta ID: {} no tiene tramos", rutaId);
            throw new IllegalArgumentException("La ruta no tiene tramos para calcular");
        }
        
        // Ordenar tramos por número de orden
        tramos.sort((t1, t2) -> {
            Integer orden1 = t1.getOrden() != null ? t1.getOrden() : Integer.MAX_VALUE;
            Integer orden2 = t2.getOrden() != null ? t2.getOrden() : Integer.MAX_VALUE;
            return orden1.compareTo(orden2);
        });
        
        logger.debug("Encontrados {} tramos para la ruta ID: {}", tramos.size(), rutaId);
        
        // Extraer coordenadas de todos los puntos (origen del primer tramo + destino de cada tramo)
        List<com.backend.tpi.ms_rutas_transportistas.dtos.osrm.CoordenadaDTO> coordenadas = new java.util.ArrayList<>();
        
        // Agregar origen del primer tramo
        Tramo primerTramo = tramos.get(0);
        if (primerTramo.getOrigenLat() != null && primerTramo.getOrigenLong() != null) {
            coordenadas.add(new com.backend.tpi.ms_rutas_transportistas.dtos.osrm.CoordenadaDTO(
                primerTramo.getOrigenLat().doubleValue(),
                primerTramo.getOrigenLong().doubleValue()
            ));
        } else {
            logger.error("Primer tramo (orden {}) no tiene coordenadas de origen", primerTramo.getOrden());
            throw new IllegalArgumentException("El primer tramo no tiene coordenadas de origen");
        }
        
        // Agregar destino de cada tramo
        for (Tramo tramo : tramos) {
            if (tramo.getDestinoLat() != null && tramo.getDestinoLong() != null) {
                coordenadas.add(new com.backend.tpi.ms_rutas_transportistas.dtos.osrm.CoordenadaDTO(
                    tramo.getDestinoLat().doubleValue(),
                    tramo.getDestinoLong().doubleValue()
                ));
            } else {
                logger.error("Tramo (orden {}) no tiene coordenadas de destino", tramo.getOrden());
                throw new IllegalArgumentException("El tramo con orden " + tramo.getOrden() + " no tiene coordenadas de destino");
            }
        }
        
        logger.debug("Calculando ruta múltiple con {} puntos de coordenadas", coordenadas.size());
        
        // Llamar a OSRM para calcular la ruta completa
        com.backend.tpi.ms_rutas_transportistas.dtos.osrm.RutaCalculadaDTO rutaCalculada = 
            osrmService.calcularRutaMultiple(coordenadas.toArray(new com.backend.tpi.ms_rutas_transportistas.dtos.osrm.CoordenadaDTO[0]));
        
        if (!rutaCalculada.isExitoso()) {
            logger.error("Error al calcular ruta múltiple: {}", rutaCalculada.getMensaje());
            throw new RuntimeException("Error al calcular ruta con OSRM: " + rutaCalculada.getMensaje());
        }
        
        logger.info("Ruta múltiple calculada exitosamente: {} km, {} horas", 
            rutaCalculada.getDistanciaKm(), rutaCalculada.getDuracionHoras());
        
        // Ahora calcular distancias individuales de cada tramo
        double distanciaTotal = 0.0;
        double duracionTotal = 0.0;
        int tramosActualizados = 0;
        
        for (int i = 0; i < tramos.size(); i++) {
            Tramo tramo = tramos.get(i);
            
            // Calcular distancia y duración de este tramo específico (desde su origen a su destino)
            com.backend.tpi.ms_rutas_transportistas.dtos.osrm.CoordenadaDTO origen = new com.backend.tpi.ms_rutas_transportistas.dtos.osrm.CoordenadaDTO(
                tramo.getOrigenLat().doubleValue(),
                tramo.getOrigenLong().doubleValue()
            );
            com.backend.tpi.ms_rutas_transportistas.dtos.osrm.CoordenadaDTO destino = new com.backend.tpi.ms_rutas_transportistas.dtos.osrm.CoordenadaDTO(
                tramo.getDestinoLat().doubleValue(),
                tramo.getDestinoLong().doubleValue()
            );
            
            com.backend.tpi.ms_rutas_transportistas.dtos.osrm.RutaCalculadaDTO rutaTramo = 
                osrmService.calcularRuta(origen, destino);
            
            if (rutaTramo.isExitoso()) {
                // Actualizar distancia y duración del tramo
                tramo.setDistancia(rutaTramo.getDistanciaKm());
                tramo.setDuracionHoras(rutaTramo.getDuracionHoras());
                tramoRepository.save(tramo);
                
                distanciaTotal += rutaTramo.getDistanciaKm();
                duracionTotal += rutaTramo.getDuracionHoras();
                tramosActualizados++;
                
                logger.debug("Tramo {} actualizado: {} km, {} horas", 
                    tramo.getOrden(), rutaTramo.getDistanciaKm(), rutaTramo.getDuracionHoras());
            } else {
                logger.warn("No se pudo calcular distancia del tramo {}: {}", 
                    tramo.getOrden(), rutaTramo.getMensaje());
            }
        }
        
        // Preparar respuesta
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("rutaId", rutaId);
        resultado.put("distanciaTotal", Math.round(distanciaTotal * 100.0) / 100.0);
        resultado.put("duracionTotalHoras", Math.round(duracionTotal * 100.0) / 100.0);
        resultado.put("duracionTotalMinutos", Math.round(duracionTotal * 60.0 * 100.0) / 100.0);
        resultado.put("numeroTramos", tramos.size());
        resultado.put("tramosActualizados", tramosActualizados);
        resultado.put("exitoso", tramosActualizados == tramos.size());
        resultado.put("mensaje", String.format("Ruta calculada: %d/%d tramos actualizados", tramosActualizados, tramos.size()));
        
        logger.info("Cálculo de ruta completa finalizado - Distancia total: {} km, Duración: {} horas, Tramos actualizados: {}/{}",
            resultado.get("distanciaTotal"), resultado.get("duracionTotalHoras"), tramosActualizados, tramos.size());
        
        return resultado;
    }

    /**
     * Calcula el costo total de una ruta sumando los costos de todos sus tramos
     * Obtiene la tarifa por km desde el microservicio de cálculos y calcula el costo
     * de cada tramo en base a su distancia.
     * 
     * @param rutaId ID de la ruta
     * @return Map con información de costos (costoTotal, costosPorTramo, tarifaPorKm)
     * @throws IllegalArgumentException si la ruta no existe o no tiene tramos
     */
    @org.springframework.transaction.annotation.Transactional
    public Map<String, Object> calcularCostoRuta(Long rutaId) {
        logger.info("Calculando costo total para ruta ID: {}", rutaId);
        
        // Verificar que la ruta existe
        Optional<Ruta> optionalRuta = rutaRepository.findById(rutaId);
        if (optionalRuta.isEmpty()) {
            logger.error("No se puede calcular costo - Ruta no encontrada con ID: {}", rutaId);
            throw new IllegalArgumentException("Ruta no encontrada con ID: " + rutaId);
        }
        
        // Buscar todos los tramos de la ruta
        List<Tramo> tramos = tramoRepository.findByRutaId(rutaId);
        if (tramos == null || tramos.isEmpty()) {
            logger.error("No se puede calcular costo - Ruta ID: {} no tiene tramos", rutaId);
            throw new IllegalArgumentException("La ruta no tiene tramos para calcular el costo");
        }
        
        logger.debug("Encontrados {} tramos para calcular costo", tramos.size());
        
        // Obtener tarifa por km desde el servicio de cálculos
        Double tarifaPorKm = obtenerTarifaPorKm();
        if (tarifaPorKm == null) {
            logger.warn("No se pudo obtener tarifa por km, usando tarifa por defecto: 100.0");
            tarifaPorKm = 100.0; // Tarifa por defecto
        }
        
        logger.debug("Tarifa por km: {}", tarifaPorKm);
        
        // Calcular costo de cada tramo
        double costoTotal = 0.0;
        List<Map<String, Object>> costosPorTramo = new java.util.ArrayList<>();
        int tramosCalculados = 0;
        
        for (Tramo tramo : tramos) {
            if (tramo.getDistancia() != null && tramo.getDistancia() > 0) {
                double costoTramo = tramo.getDistancia() * tarifaPorKm;
                
                // Actualizar costo aproximado del tramo
                tramo.setCostoAproximado(java.math.BigDecimal.valueOf(costoTramo));
                tramoRepository.save(tramo);
                
                costoTotal += costoTramo;
                tramosCalculados++;
                
                Map<String, Object> infoTramo = new HashMap<>();
                infoTramo.put("tramoId", tramo.getId());
                infoTramo.put("orden", tramo.getOrden());
                infoTramo.put("distancia", tramo.getDistancia());
                infoTramo.put("costo", Math.round(costoTramo * 100.0) / 100.0);
                costosPorTramo.add(infoTramo);
                
                logger.debug("Tramo {} - Distancia: {} km, Costo: ${}", 
                    tramo.getOrden(), tramo.getDistancia(), Math.round(costoTramo * 100.0) / 100.0);
            } else {
                logger.warn("Tramo {} no tiene distancia calculada, se omite del costo", tramo.getOrden());
            }
        }
        
        // Preparar respuesta
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("rutaId", rutaId);
        resultado.put("costoTotal", Math.round(costoTotal * 100.0) / 100.0);
        resultado.put("tarifaPorKm", tarifaPorKm);
        resultado.put("numeroTramos", tramos.size());
        resultado.put("tramosCalculados", tramosCalculados);
        resultado.put("costosPorTramo", costosPorTramo);
        resultado.put("exitoso", tramosCalculados > 0);
        resultado.put("mensaje", String.format("Costos calculados para %d/%d tramos", tramosCalculados, tramos.size()));
        
        logger.info("Cálculo de costos finalizado - Costo total: ${}, Tramos calculados: {}/{}",
            resultado.get("costoTotal"), tramosCalculados, tramos.size());
        
        return resultado;
    }

    /**
     * Obtiene la tarifa por kilómetro desde el microservicio de cálculos
     * @return Tarifa por km, o null si no se puede obtener
     */
    private Double obtenerTarifaPorKm() {
        try {
            String token = extractBearerToken();
            
            org.springframework.http.ResponseEntity<java.util.List<java.util.Map<String, Object>>> response = 
                calculosClient.get()
                    .uri("/api/v1/tarifas")
                    .headers(h -> { if (token != null) h.setBearerAuth(token); })
                    .retrieve()
                    .toEntity(new org.springframework.core.ParameterizedTypeReference<java.util.List<java.util.Map<String, Object>>>() {});
            
            java.util.List<java.util.Map<String, Object>> tarifas = response.getBody();
            
            if (tarifas != null && !tarifas.isEmpty()) {
                Object precioPorKm = tarifas.get(0).get("precioPorKm");
                if (precioPorKm instanceof Number) {
                    return ((Number) precioPorKm).doubleValue();
                }
            }
            
            logger.warn("No se encontraron tarifas en el servicio de cálculos");
            return null;
            
        } catch (Exception e) {
            logger.error("Error al obtener tarifa por km: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Helper: extrae token Bearer del SecurityContext si existe
     */
    private String extractBearerToken() {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken) {
            return ((org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken) auth).getToken().getTokenValue();
        }
        return null;
    }
}
