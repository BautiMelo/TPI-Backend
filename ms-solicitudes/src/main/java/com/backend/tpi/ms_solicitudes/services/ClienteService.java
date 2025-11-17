package com.backend.tpi.ms_solicitudes.services;

import com.backend.tpi.ms_solicitudes.models.Cliente;
import com.backend.tpi.ms_solicitudes.repositories.ClienteRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio de negocio para Clientes
 * Maneja operaciones CRUD de clientes
 */
@Service
@Slf4j
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    /**
     * Obtiene todos los clientes del sistema
     * @return Lista con todos los clientes
     */
    @Transactional(readOnly = true)
    public List<Cliente> findAll() {
        return clienteRepository.findAll();
    }

    /**
     * Busca un cliente por su email
     * @param email Email del cliente a buscar
     * @return Cliente encontrado
     * @throws RuntimeException si no se encuentra el cliente
     */
    @Transactional(readOnly = true)
    public Cliente findByEmail(String email) {
        return clienteRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con email: " + email));
    }

    /**
     * Busca un cliente por su ID
     * @param id ID del cliente
     * @return Cliente encontrado
     * @throws RuntimeException si no se encuentra el cliente
     */
    @Transactional(readOnly = true)
    public Cliente findById(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + id));
    }

    /**
     * Guarda un nuevo cliente en la base de datos
     * @param cliente Cliente a guardar
     * @return Cliente guardado con su ID asignado
     */
    @Transactional
    public Cliente save(Cliente cliente) {
        log.info("Guardando cliente: {}", cliente.getNombre());
        return clienteRepository.save(cliente);
    }

    /**
     * Actualiza los datos de un cliente existente
     * @param id ID del cliente a actualizar
     * @param clienteActualizado Datos actualizados del cliente
     * @return Cliente actualizado
     */
    @Transactional
    public Cliente update(Long id, Cliente clienteActualizado) {
        Cliente cliente = findById(id);
        cliente.setNombre(clienteActualizado.getNombre());
        cliente.setEmail(clienteActualizado.getEmail());
        cliente.setTelefono(clienteActualizado.getTelefono());
        log.info("Actualizando cliente ID: {}", id);
        return clienteRepository.save(cliente);
    }

    /**
     * Elimina un cliente por su ID
     * @param id ID del cliente a eliminar
     */
    @Transactional
    public void deleteById(Long id) {
        Cliente cliente = findById(id);
        log.info("Eliminando cliente ID: {}", id);
        clienteRepository.delete(cliente);
    }
}
