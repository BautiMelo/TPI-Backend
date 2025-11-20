package com.backend.tpi.ms_solicitudes.config;

import com.backend.tpi.ms_solicitudes.models.*;
import com.backend.tpi.ms_solicitudes.repositories.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializer implements CommandLineRunner {

    private final EstadoSolicitudRepository estadoSolicitudRepository;
    private final EstadoContenedorRepository estadoContenedorRepository;
    private final ClienteRepository clienteRepository;
    private final ContenedorRepository contenedorRepository;

    public DataInitializer(EstadoSolicitudRepository estadoSolicitudRepository,
                           EstadoContenedorRepository estadoContenedorRepository,
                           ClienteRepository clienteRepository,
                           ContenedorRepository contenedorRepository) {
        this.estadoSolicitudRepository = estadoSolicitudRepository;
        this.estadoContenedorRepository = estadoContenedorRepository;
        this.clienteRepository = clienteRepository;
        this.contenedorRepository = contenedorRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Inicializar estados de solicitud si no existen
        if (estadoSolicitudRepository.count() == 0) {
            EstadoSolicitud borrador = new EstadoSolicitud(); borrador.setNombre("BORRADOR");
            EstadoSolicitud programada = new EstadoSolicitud(); programada.setNombre("PROGRAMADA");
            EstadoSolicitud enTransito = new EstadoSolicitud(); enTransito.setNombre("EN_TRANSITO");
            EstadoSolicitud entregada = new EstadoSolicitud(); entregada.setNombre("ENTREGADA");
            estadoSolicitudRepository.save(borrador);
            estadoSolicitudRepository.save(programada);
            estadoSolicitudRepository.save(enTransito);
            estadoSolicitudRepository.save(entregada);
        }

        // Inicializar estados de contenedor si no existen
        if (estadoContenedorRepository.count() == 0) {
            EstadoContenedor libre = new EstadoContenedor(); libre.setNombre("LIBRE");
            EstadoContenedor ocupado = new EstadoContenedor(); ocupado.setNombre("OCUPADO");
            estadoContenedorRepository.save(libre);
            estadoContenedorRepository.save(ocupado);
        }

        // Crear un cliente de ejemplo para pruebas si no existen clientes
        if (clienteRepository.count() == 0) {
            Cliente c = new Cliente();
            c.setNombre("Cliente Demo");
            c.setEmail("demo@cliente.local");
            c.setTelefono("+000000000");
            clienteRepository.save(c);

            // Crear un contenedor asociado para pruebas
            Contenedor cont = new Contenedor();
            cont.setClienteId(c.getId());
            cont.setPeso(new java.math.BigDecimal("100.0"));
            cont.setVolumen(new java.math.BigDecimal("1.5"));
            // asignar estado 'LIBRE' si existe
            estadoContenedorRepository.findByNombre("LIBRE").ifPresent(cont::setEstado);
            contenedorRepository.save(cont);
        }
    }
}
