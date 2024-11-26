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
        validarInformacionCliente(cliente);

        if (clienteRepositorio.existsByCorreoElectronico(cliente.getCorreoElectronico())) {
            throw new ClienteExistenteExcepcion(cliente.getCorreoElectronico());
        }

        clienteRepositorio.save(cliente);
    }


    public void actualizarCliente(Long clienteId, Cliente cliente) {
        Cliente clienteActualizar = clienteRepositorio.findById(clienteId)
                .orElseThrow(() -> new ClienteNoEncontradoExcepcion(clienteId));

        validarInformacionCliente(cliente);

        clienteActualizar.setNombre(cliente.getNombre());
        clienteActualizar.setApellido(cliente.getApellido());
        clienteActualizar.setCorreoElectronico(cliente.getCorreoElectronico());
        clienteActualizar.setTelefono(cliente.getTelefono());
        clienteActualizar.setConsentimiento(cliente.isConsentimiento());

        clienteRepositorio.save(clienteActualizar);
    }

    private void validarInformacionCliente(Cliente cliente) {
        if (cliente.getNombre() == null || cliente.getApellido() == null ||
                cliente.getCorreoElectronico() == null || cliente.getTelefono() == null) {
            throw new InformacionIncompletaExcepcion();
        }
    }

    public List<Cliente> obtenerClientes() {
        return clienteRepositorio.findAll();
    }

    public Cliente obtenerClientesPorId(Long clienteId) {
        return clienteRepositorio.findById(clienteId)
                .orElseThrow(() -> new ClienteNoEncontradoExcepcion(clienteId));
    }

    public Cliente obtenerPorCorreoElectronico(String correoElectronico) {
        return clienteRepositorio.findByCorreoElectronico(correoElectronico)
                .orElseThrow(() -> new RuntimeException(
                        "No se encontró un cliente con el correo electrónico: " + correoElectronico));
    }

    public void eliminarCliente(Long clienteId) {
        Cliente cliente = clienteRepositorio.findById(clienteId)
                .orElseThrow(() -> new ClienteNoEncontradoExcepcion(clienteId));
        clienteRepositorio.delete(cliente);
    }
}
