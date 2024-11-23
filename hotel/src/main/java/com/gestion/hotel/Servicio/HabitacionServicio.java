package com.gestion.hotel.Servicio;

import com.gestion.hotel.Excepciones.HabitacionNoEncontradaExcepcion;
import com.gestion.hotel.Modelo.Habitacion;
import com.gestion.hotel.Repositorio.HabitacionRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HabitacionServicio {
    private final HabitacionRepositorio habitacionRepositorio;

    @Autowired
    public HabitacionServicio(HabitacionRepositorio habitacionRepositorio) {
        this.habitacionRepositorio = habitacionRepositorio;
    }

    public void crearHabitacion(Habitacion habitacion) {
        habitacionRepositorio.save(habitacion);
    }

    public void actualizarHabitacion(Long habitacionId, Habitacion habitacion) {
        Habitacion habitacionActualizar = habitacionRepositorio.findById(habitacionId)
                .orElseThrow(() -> new HabitacionNoEncontradaExcepcion(habitacionId));

        habitacionActualizar.setTipo(habitacion.getTipo());
        habitacionActualizar.setCapacidad(habitacion.getCapacidad());
        habitacionActualizar.setPrecioPorNoche(habitacion.getPrecioPorNoche());
        habitacionActualizar.setDisponible(habitacion.isDisponible());
        habitacionActualizar.setImagenUrl(habitacion.getImagenUrl());

        habitacionRepositorio.save(habitacionActualizar);
    }

    public List<Habitacion> obtenerHabitaciones() {
        return habitacionRepositorio.findAll();
    }

    public List<Habitacion> obtenerHabitacionesDisponibles() {
        return habitacionRepositorio.findByDisponibleTrue();
    }

    public List<Habitacion> obtenerHabitacionesPorTipo(String tipo) {
        return habitacionRepositorio.findByTipo(tipo);
    }

    public Habitacion obtenerHabitacionPorId(Long habitacionId) {
        return habitacionRepositorio.findById(habitacionId).orElseThrow(() -> new HabitacionNoEncontradaExcepcion(habitacionId));
    }

    public void eliminarHabitacion(Long habitacionId) {
        Habitacion habitacion = habitacionRepositorio.findById(habitacionId)
                .orElseThrow(() -> new HabitacionNoEncontradaExcepcion(habitacionId));
        habitacionRepositorio.delete(habitacion);
    }
}
