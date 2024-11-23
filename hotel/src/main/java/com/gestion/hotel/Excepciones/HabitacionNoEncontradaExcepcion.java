package com.gestion.hotel.Excepciones;

public class HabitacionNoEncontradaExcepcion extends RuntimeException {
    public HabitacionNoEncontradaExcepcion(Long habitacionId) {
        super("La habitación " + habitacionId + " no se encuentra resgistrada en el hotel.");
    }
}
