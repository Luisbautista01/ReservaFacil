package com.gestion.hotel.Excepciones;

public class ClienteExistenteExcepcion extends RuntimeException {
    public ClienteExistenteExcepcion(String correoElectronico) {
        super("El cliente con correo electrónico " + correoElectronico + " ya se encuentra registrado.");
    }
}
