package com.gestion.hotel.Servicio;

import com.gestion.hotel.Excepciones.InformacionIncompletaExcepcion;
import com.gestion.hotel.Excepciones.ReservaNoEncontradaExcepcion;
import com.gestion.hotel.Modelo.Empleado;
import com.gestion.hotel.Modelo.Habitacion;
import com.gestion.hotel.Modelo.Reserva;
import com.gestion.hotel.Repositorio.EmpleadoRepositorio;
import com.gestion.hotel.Repositorio.HabitacionRepositorio;
import com.gestion.hotel.Repositorio.ReservaRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class ReservaServicio {
    private final ReservaRepositorio reservaRepositorio;
    private final HabitacionRepositorio habitacionRepositorio;
    private final EmpleadoRepositorio empleadoRepositorio;

    @Autowired
    public ReservaServicio(ReservaRepositorio reservaRepositorio, HabitacionRepositorio habitacionRepositorio, EmpleadoRepositorio empleadoRepositorio) {
        this.reservaRepositorio = reservaRepositorio;
        this.habitacionRepositorio = habitacionRepositorio;
        this.empleadoRepositorio = empleadoRepositorio;
    }

    @Transactional
    public Reserva crearReserva(Reserva reserva) {
        Habitacion habitacion = habitacionRepositorio.findById(reserva.getHabitacion().getId())
                .orElseThrow(() -> new IllegalStateException("La habitación no existe."));

        if (!habitacion.isDisponible()) {
            throw new IllegalStateException("La habitación ya está ocupada.");
        }

        Empleado empleado = empleadoRepositorio.findFirstByDisponibleTrue()
                .orElseThrow(() -> new IllegalStateException("No hay empleados disponibles."));

        // Validar fechas
        if (reserva.getFechaIngreso().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de ingreso no puede ser anterior a la fecha actual.");
        }

        if (reserva.getFechaIngreso().isAfter(reserva.getFechaSalida())) {
            throw new IllegalArgumentException("La fecha de ingreso debe ser antes de la fecha de salida.");
        }

        // Calcular total
        calcularTotalReserva(reserva);
        if (reserva.getMetodoPago() == null) {
            throw new IllegalArgumentException("El método de pago es obligatorio.");
        }

        reservaRepositorio.save(reserva);

        // Marcar empleado y habitación como no disponibles
        habitacion.setDisponible(false);
        empleado.setDisponible(false);

        reserva.setEmpleado(empleado);

        habitacionRepositorio.save(habitacion);
        empleadoRepositorio.save(empleado);

        return reservaRepositorio.save(reserva);
    }

    @Transactional
    public Reserva actualizarReserva(Long reservaId, Reserva reserva) {
        Reserva reservaActualizar = reservaRepositorio.findById(reservaId)
                .orElseThrow(() -> new ReservaNoEncontradaExcepcion(reservaId));

        reservaActualizar.setCliente(reserva.getCliente());
        reservaActualizar.setHabitacion(reserva.getHabitacion());
        reservaActualizar.setEmpleado(reserva.getEmpleado());
        reservaActualizar.setFechaIngreso(reserva.getFechaIngreso());
        reservaActualizar.setFechaSalida(reserva.getFechaSalida());
        reservaActualizar.setTotal(reserva.getTotal());
        reservaActualizar.setMetodoPago(reserva.getMetodoPago());

        if (!reserva.getHabitacion().isDisponible() &&
                !reserva.getHabitacion().getId().equals(reservaActualizar.getHabitacion().getId())) {
            throw new IllegalStateException("La habitación: " + reservaActualizar.getHabitacion().getId() + " no está disponible.");
        }

        verificarInformacion(reservaActualizar);

        // Calcular total
        calcularTotalReserva(reservaActualizar);

        return reservaRepositorio.save(reservaActualizar);
    }

    @Transactional
    private void calcularTotalReserva(Reserva reserva) {
        Habitacion habitacion = habitacionRepositorio.findById(reserva.getHabitacion().getId())
                .orElseThrow(() -> new IllegalStateException("La habitación no existe."));

        long diasEstadia = ChronoUnit.DAYS.between(reserva.getFechaIngreso(), reserva.getFechaSalida());
        if (diasEstadia <= 0) {
            throw new IllegalArgumentException("La estancia debe durar al menos un día.");
        }

        double total = diasEstadia * habitacion.getPrecioPorNoche();
        reserva.setTotal(total);
    }

    @Transactional
    private void verificarInformacion(Reserva reserva) {
        if (reserva.getFechaIngreso() == null || reserva.getFechaSalida() == null ||
                reserva.getCliente() == null || reserva.getHabitacion() == null || reserva.getEmpleado() == null) {
            throw new InformacionIncompletaExcepcion();
        }
        if (!reserva.getFechaIngreso().isBefore(reserva.getFechaSalida())) {
            throw new IllegalArgumentException("La fecha de ingreso debe ser anterior a la fecha de salida.");
        }
    }

    @Transactional
    public List<Reserva> obtenerReservas() {
        return reservaRepositorio.findAll();
    }

    @Transactional
    public Reserva obtenerReservaPorId(Long reservaId) {
        return reservaRepositorio.findById(reservaId)
                .orElseThrow(() -> new ReservaNoEncontradaExcepcion(reservaId));
    }

    @Transactional
    public List<Reserva> obtenerReservasPorClienteId(Long clienteId) {
        return reservaRepositorio.findByClienteId(clienteId);
    }

    @Transactional
    public List<Reserva> obtenerReservasPorEmpleadoId(Long empleadoId) {
        return reservaRepositorio.findByEmpleadoId(empleadoId);  // Cambiado para que use el campo adecuado
    }

    @Transactional
    public void eliminarReserva(Long reservaId) {
        Reserva reserva = reservaRepositorio.findById(reservaId)
                .orElseThrow(() -> new ReservaNoEncontradaExcepcion(reservaId));
        Habitacion habitacion = reserva.getHabitacion();

        // Marcar la habitación como disponible
        habitacion.setDisponible(true);
        habitacionRepositorio.save(habitacion);

        // Eliminar la reserva
        reservaRepositorio.delete(reserva);
    }

}



