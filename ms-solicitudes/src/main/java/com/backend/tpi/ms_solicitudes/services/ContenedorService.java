package com.backend.tpi.ms_solicitudes.services;

import com.backend.tpi.ms_solicitudes.dtos.SeguimientoContenedorDTO;
import com.backend.tpi.ms_solicitudes.models.Contenedor;
import com.backend.tpi.ms_solicitudes.models.EstadoContenedor;
import com.backend.tpi.ms_solicitudes.models.Solicitud;
import com.backend.tpi.ms_solicitudes.repositories.ContenedorRepository;
import com.backend.tpi.ms_solicitudes.repositories.SolicitudRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class ContenedorService {

    @Autowired
    private ContenedorRepository contenedorRepository;

    @Autowired
    private SolicitudRepository solicitudRepository;

    @Transactional(readOnly = true)
    public List<Contenedor> findAll() {
        return contenedorRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Contenedor findById(Long id) {
        return contenedorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contenedor no encontrado con ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<Contenedor> findByClienteId(Long clienteId) {
        return contenedorRepository.findAll().stream()
                .filter(c -> clienteId.equals(c.getClienteId()))
                .toList();
    }

    @Transactional
    public Contenedor save(Contenedor contenedor) {
        log.info("Guardando contenedor para cliente ID: {}", contenedor.getClienteId());
        return contenedorRepository.save(contenedor);
    }

    @Transactional
    public Contenedor update(Long id, Contenedor contenedorActualizado) {
        Contenedor contenedor = findById(id);
        contenedor.setPeso(contenedorActualizado.getPeso());
        contenedor.setVolumen(contenedorActualizado.getVolumen());
        contenedor.setEstado(contenedorActualizado.getEstado());
        contenedor.setClienteId(contenedorActualizado.getClienteId());
        log.info("Actualizando contenedor ID: {}", id);
        return contenedorRepository.save(contenedor);
    }

    @Transactional
    public void deleteById(Long id) {
        Contenedor contenedor = findById(id);
        log.info("Eliminando contenedor ID: {}", id);
        contenedorRepository.delete(contenedor);
    }

    @Transactional
    public Contenedor updateEstado(Long id, Long estadoId) {
        Contenedor contenedor = findById(id);
        EstadoContenedor estado = new EstadoContenedor();
        estado.setId(estadoId);
        contenedor.setEstado(estado);
        log.info("Actualizando estado del contenedor ID: {} a estado ID: {}", id, estadoId);
        return contenedorRepository.save(contenedor);
    }

    @Transactional(readOnly = true)
    public SeguimientoContenedorDTO getSeguimiento(Long id) {
        Contenedor contenedor = findById(id);
        
        SeguimientoContenedorDTO seguimiento = new SeguimientoContenedorDTO();
        seguimiento.setIdContenedor(contenedor.getId());
        
        if (contenedor.getEstado() != null) {
            seguimiento.setEstadoActual(contenedor.getEstado().getNombre());
        }
        
        // Buscar la solicitud activa más reciente del contenedor
        Optional<Solicitud> solicitudOpt = solicitudRepository.findFirstByContenedorIdOrderByIdDesc(id);
        
        if (solicitudOpt.isPresent()) {
            Solicitud solicitud = solicitudOpt.get();
            
            // Determinar la ubicación según el estado de la solicitud
            if (solicitud.getEstado() != null) {
                String estadoSolicitud = solicitud.getEstado().getNombre().toLowerCase();
                
                switch (estadoSolicitud) {
                    case "en_transito":
                    case "en_camino":
                    case "en_ruta":
                        // Si está en tránsito, usamos la ubicación de destino
                        // En una implementación real, consultarías la posición actual del camión
                        // desde ms-rutas-transportistas usando el rutaId
                        seguimiento.setUbicacionActualLat(solicitud.getDestinoLat());
                        seguimiento.setUbicacionActualLong(solicitud.getDestinoLong());
                        seguimiento.setDepositoId(null);
                        break;
                        
                    case "entregado":
                    case "finalizado":
                        // Si fue entregado, está en destino
                        seguimiento.setUbicacionActualLat(solicitud.getDestinoLat());
                        seguimiento.setUbicacionActualLong(solicitud.getDestinoLong());
                        seguimiento.setDepositoId(null);
                        break;
                        
                    case "pendiente":
                    case "programado":
                    default:
                        // Si está pendiente, está en el origen (depósito)
                        seguimiento.setUbicacionActualLat(solicitud.getOrigenLat());
                        seguimiento.setUbicacionActualLong(solicitud.getOrigenLong());
                        // TODO: Obtener depositoId desde ms-gestion-calculos si está almacenado
                        seguimiento.setDepositoId(null);
                        break;
                }
            }
        } else {
            // Si no hay solicitud activa, el contenedor está en un depósito sin solicitud
            seguimiento.setUbicacionActualLat(null);
            seguimiento.setUbicacionActualLong(null);
            seguimiento.setDepositoId(null); // TODO: Consultar tabla de ubicación de contenedores si existe
        }
        
        return seguimiento;
    }
}
