package com.gestion.hotel.Chatbot_Estado;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UsuarioEstadoRepositorio extends JpaRepository<UsuarioEstado, Long> {
    List<UsuarioEstado> findByClienteId(Long clienteId);
}
