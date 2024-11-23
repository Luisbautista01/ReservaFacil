package com.gestion.hotel.Servicio;

import com.gestion.hotel.Excepciones.HabitacionNoEncontradaExcepcion;
import com.gestion.hotel.Excepciones.InventarioNoEncontradoExcepcion;
import com.gestion.hotel.Modelo.Habitacion;
import com.gestion.hotel.Modelo.Inventario;
import com.gestion.hotel.Repositorio.HabitacionRepositorio;
import com.gestion.hotel.Repositorio.InventarioRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InventarioServicio {
    private final InventarioRepositorio inventarioRepositorio;
    private final HabitacionRepositorio habitacionRepositorio;

    @Autowired
    public InventarioServicio(InventarioRepositorio inventarioRepositorio, HabitacionRepositorio habitacionRepositorio) {
        this.inventarioRepositorio = inventarioRepositorio;
        this.habitacionRepositorio = habitacionRepositorio;
    }

    public Inventario agregarInventario(Inventario inventario) {
        // Validación de la existencia de la habitación a la que se asociará el inventario
        Habitacion habitacion = habitacionRepositorio.findById(inventario.getHabitacion().getId())
                .orElseThrow(() -> new HabitacionNoEncontradaExcepcion(inventario.getHabitacion().getId()));

        // Asociando la habitación al inventario antes de guardarlo
        inventario.setHabitacion(habitacion);

        // Guardando el inventario con la habitación asociada
        return inventarioRepositorio.save(inventario);
    }

    public List<Inventario> obtenerInventarios() {
        return inventarioRepositorio.findAll();
    }

    public Inventario obtenerInventarioPorId(Long inventarioId) {
        return inventarioRepositorio.findById(inventarioId)
                .orElseThrow(() -> new InventarioNoEncontradoExcepcion(inventarioId));
    }

    public void actualizarInventario(Long inventarioId, Inventario inventario) {
        Inventario inventarioActualizar = inventarioRepositorio.findById(inventarioId)
                .orElseThrow(() -> new InventarioNoEncontradoExcepcion(inventarioId));

        // Validar y asociar la nueva habitación
        Habitacion habitacion = habitacionRepositorio.findById(inventario.getHabitacion().getId())
                .orElseThrow(() -> new HabitacionNoEncontradaExcepcion(inventario.getHabitacion().getId()));

        // Actualizar datos
        inventarioActualizar.setItem(inventario.getItem());
        inventarioActualizar.setCantidad(inventario.getCantidad());
        inventarioActualizar.setCosto(inventario.getCosto());
        inventarioActualizar.setHabitacion(habitacion);

        // Guardar el inventario actualizado
        inventarioRepositorio.save(inventarioActualizar);

        System.out.println("Inventario actualizado con la habitación ID: " + habitacion.getId());
    }

    public void eliminarInventario(Long inventarioId) {
        if (!inventarioRepositorio.existsById(inventarioId)) {
            throw new InventarioNoEncontradoExcepcion(inventarioId);
        }
        inventarioRepositorio.deleteById(inventarioId);
    }
}
