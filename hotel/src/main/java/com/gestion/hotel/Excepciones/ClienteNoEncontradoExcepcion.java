package com.gestion.hotel.Excepciones;

public class ClienteNoEncontradoExcepcion extends RuntimeException{
    public ClienteNoEncontradoExcepcion(Long clienteId) {
        super("Cliente con ID " + clienteId + " no encontrado.");
    }
}
