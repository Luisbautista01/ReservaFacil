package com.gestion.hotel.Excepciones;

public class InformacionIncompletaExcepcion extends RuntimeException {
    public InformacionIncompletaExcepcion() {
        super("Información incompleta. Todos los campos son obligatorios.");
    }
}
