package com.gestion.hotel.Servicio;

import com.gestion.hotel.Excepciones.InformacionIncompletaExcepcion;
import com.gestion.hotel.Excepciones.ReservaNoEncontradaExcepcion;
import com.gestion.hotel.Modelo.Habitacion;
import com.gestion.hotel.Modelo.Reserva;
import com.gestion.hotel.Repositorio.HabitacionRepositorio;
import com.gestion.hotel.Repositorio.ReservaRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReservaServicio {
    private final ReservaRepositorio reservaRepositorio;
    private final HabitacionRepositorio habitacionRepositorio;

    @Autowired
    public ReservaServicio(ReservaRepositorio reservaRepositorio, HabitacionRepositorio habitacionRepositorio) {
        this.reservaRepositorio = reservaRepositorio;
        this.habitacionRepositorio = habitacionRepositorio;
    }

    public Reserva crearReserva(Reserva reserva) {
        verificarInformacion(reserva);

        // Marcar la habitación como ocupada
        Habitacion habitacion = habitacionRepositorio.findById(reserva.getHabitacion().getId())
                .orElseThrow(() -> new IllegalArgumentException("Habitación no encontrada"));

        if (!habitacion.isDisponible()) {
            throw new IllegalStateException("La habitación ya está ocupada.");
        }

        habitacion.setDisponible(false);
        habitacionRepositorio.save(habitacion);

        return reservaRepositorio.save(reserva);
    }

    public List<Reserva> obtenerReservas() {
        return reservaRepositorio.findAll();
    }


    public Reserva actualizarReserva(Long reservaId, Reserva reserva) {
        Reserva reservaActualizar = reservaRepositorio.findById(reservaId)
                .orElseThrow(() -> new ReservaNoEncontradaExcepcion(reservaId));

        reservaActualizar.setFechaIngreso(reserva.getFechaIngreso());
        reservaActualizar.setFechaSalida(reserva.getFechaSalida());
        reservaActualizar.setTotal(reserva.getTotal());
        reservaActualizar.setCliente(reserva.getCliente());
        reservaActualizar.setHabitacion(reserva.getHabitacion());
        reservaActualizar.setEmpleado(reserva.getEmpleado());

        if (!reserva.getHabitacion().isDisponible() &&
                !reserva.getHabitacion().getId().equals(reservaActualizar.getHabitacion().getId())) {
            throw new IllegalStateException("La habitación seleccionada no está disponible.");
        }

        verificarInformacion(reservaActualizar);
        System.out.println("Reserva ID: " + reservaId);
        System.out.println("Datos de la reserva recibida: " + reserva);

        reservaRepositorio.save(reservaActualizar);
        return reservaActualizar;
    }

    private void verificarInformacion(Reserva reserva) {
        if (reserva.getFechaIngreso() == null || reserva.getFechaSalida() == null ||
                reserva.getTotal() <= 0 || reserva.getCliente() == null ||
                reserva.getHabitacion() == null || reserva.getEmpleado() == null) {
            throw new InformacionIncompletaExcepcion();
        }
        if (!reserva.getFechaIngreso().isBefore(reserva.getFechaSalida())) {
            throw new IllegalArgumentException("La fecha de ingreso debe ser anterior a la fecha de salida.");
        }
    }

    public Reserva obtenerReservaPorId(Long id) {
        return reservaRepositorio.findById(id)
                .orElseThrow(() -> new ReservaNoEncontradaExcepcion(id));
    }

    public List<Reserva> obtenerReservasPorClienteId(Long clienteId) {
        return reservaRepositorio.findByClienteId(clienteId);
    }

    public List<Reserva> obtenerReservasPorEmpleadoId(Long empleadoId) {
        return reservaRepositorio.findByEmpleadoId(empleadoId);  // Cambiado para que use el campo adecuado
    }

    public void eliminarReserva(Long id) {
        Reserva reserva = reservaRepositorio.findById(id)
                .orElseThrow(() -> new ReservaNoEncontradaExcepcion(id));
        Habitacion habitacion = reserva.getHabitacion();

        // Marcar la habitación como disponible
        habitacion.setDisponible(true);
        habitacionRepositorio.save(habitacion);

        // Eliminar la reserva
        reservaRepositorio.delete(reserva);
    }

}



