package com.gestion.hotel.Chatbot_Estado;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioEstadoService {

    private UsuarioEstadoRepositorio usuarioEstadoRepositorio;

    @Autowired
    public UsuarioEstadoService(UsuarioEstadoRepositorio usuarioEstadoRepositorio) {
        this.usuarioEstadoRepositorio = usuarioEstadoRepositorio;
    }

    @Transactional
    public UsuarioEstado guardarEstado(UsuarioEstado usuarioEstado) {
        return usuarioEstadoRepositorio.save(usuarioEstado);
    }

    @Transactional
    public List<UsuarioEstado> obtenerEstadosPorCliente(Long clienteId) {
        return usuarioEstadoRepositorio.findByClienteId(clienteId);
    }

    @Transactional
    public Optional<UsuarioEstado> obtenerEstadoPorId(Long id) {
        return usuarioEstadoRepositorio.findById(id);
    }
}


