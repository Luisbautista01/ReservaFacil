package com.gestion.hotel.Excepciones;

public class EmpleadoExistenteExcepcion extends RuntimeException {
    public EmpleadoExistenteExcepcion(String correoElectronico) {
        super("El cliente con correo electrónico " + correoElectronico + " ya se encuentra registrado.");
    }
}
