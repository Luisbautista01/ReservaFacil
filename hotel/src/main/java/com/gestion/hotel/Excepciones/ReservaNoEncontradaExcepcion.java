package com.gestion.hotel.Excepciones;

public class ReservaNoEncontradaExcepcion extends RuntimeException {
    public ReservaNoEncontradaExcepcion(Long reservaId) {
        super("La reserva " + reservaId + " no se encuentra registrada en el hotel.");
    }
}
