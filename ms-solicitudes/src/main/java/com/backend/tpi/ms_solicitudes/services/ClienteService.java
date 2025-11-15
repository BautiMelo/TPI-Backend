package com.backend.tpi.ms_solicitudes.services;

import com.backend.tpi.ms_solicitudes.models.Cliente;
import com.backend.tpi.ms_solicitudes.repositories.ClienteRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Transactional(readOnly = true)
    public List<Cliente> findAll() {
        return clienteRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Cliente findByEmail(String email) {
        return clienteRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con email: " + email));
    }

    @Transactional(readOnly = true)
    public Cliente findById(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + id));
    }

    @Transactional
    public Cliente save(Cliente cliente) {
        log.info("Guardando cliente: {}", cliente.getNombre());
        return clienteRepository.save(cliente);
    }

    @Transactional
    public Cliente update(Long id, Cliente clienteActualizado) {
        Cliente cliente = findById(id);
        cliente.setNombre(clienteActualizado.getNombre());
        cliente.setEmail(clienteActualizado.getEmail());
        cliente.setTelefono(clienteActualizado.getTelefono());
        log.info("Actualizando cliente ID: {}", id);
        return clienteRepository.save(cliente);
    }

    @Transactional
    public void deleteById(Long id) {
        Cliente cliente = findById(id);
        log.info("Eliminando cliente ID: {}", id);
        clienteRepository.delete(cliente);
    }
}
