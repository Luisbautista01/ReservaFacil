package com.gestion.hotel.Excepciones;

public class EmpleadoNoEncontradoExcepcion extends RuntimeException {
    public EmpleadoNoEncontradoExcepcion(Long empleadoId) {
        super("Empleado no encontrado con ID: " + empleadoId);
    }
}
