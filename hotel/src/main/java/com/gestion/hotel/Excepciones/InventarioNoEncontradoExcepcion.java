package com.gestion.hotel.Excepciones;

public class InventarioNoEncontradoExcepcion extends RuntimeException {
    public InventarioNoEncontradoExcepcion(Long inventarioId) {
        super("Inventario: " + inventarioId + " no encontrado");
    }
}
