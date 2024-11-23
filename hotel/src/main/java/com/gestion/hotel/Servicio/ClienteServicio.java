package com.gestion.hotel.Servicio;

import com.gestion.hotel.Excepciones.ClienteExistenteExcepcion;
import com.gestion.hotel.Excepciones.ClienteNoEncontradoExcepcion;
import com.gestion.hotel.Excepciones.InformacionIncompletaExcepcion;
import com.gestion.hotel.Modelo.Cliente;
import com.gestion.hotel.Repositorio.ClienteRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClienteServicio {

    private final ClienteRepositorio clienteRepositorio;

    @Autowired
    public ClienteServicio(ClienteRepositorio clienteRepositorio) {
        this.clienteRepositorio = clienteRepositorio;
    }

    public void crearCliente(Cliente cliente) {
        // Verificar que toda la información requerida esté presente
        if (cliente.getNombre() == null || cliente.getApellido() == null ||
                cliente.getCorreoElectronico() == null || cliente.getTelefono() == null) {
            throw new InformacionIncompletaExcepcion();
        }

        // Verificar que el correo electrónico no esté registrado
        if (clienteRepositorio.existsByCorreoElectronico(cliente.getCorreoElectronico())) {
            throw new ClienteExistenteExcepcion(cliente.getCorreoElectronico());
        }

        // Guardar el cliente si todas las verificaciones han pasado
        clienteRepositorio.save(cliente);
    }

    public void actualizarCliente(Long clienteId, Cliente cliente) {
        Cliente clienteActualizar = clienteRepositorio.findById(clienteId)
                .orElseThrow(() -> new ClienteNoEncontradoExcepcion(clienteId));

        clienteActualizar.setNombre(cliente.getNombre());
        clienteActualizar.setApellido(cliente.getApellido());
        clienteActualizar.setCorreoElectronico(cliente.getCorreoElectronico());
        clienteActualizar.setTelefono(clienteActualizar.getTelefono());

        if ((cliente.getNombre() == null) ||
                (cliente.getApellido() == null) ||
                (cliente.getCorreoElectronico() == null) ||
                (cliente.getTelefono() == null)) {
            throw new InformacionIncompletaExcepcion();
        }

        clienteRepositorio.save(clienteActualizar);
    }

    public List<Cliente> obtenerClientes() {
        return clienteRepositorio.findAll();
    }

    public Cliente obtenerClientesPorId(Long clienteId) {
        return clienteRepositorio.findById(clienteId)
                .orElseThrow(() -> new ClienteNoEncontradoExcepcion(clienteId));
    }

    public void eliminarCliente(Long clienteId) {
        Cliente cliente = clienteRepositorio.findById(clienteId)
                .orElseThrow(() -> new ClienteNoEncontradoExcepcion(clienteId));
        clienteRepositorio.delete(cliente);
    }
}
