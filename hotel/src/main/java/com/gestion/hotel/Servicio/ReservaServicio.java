package com.gestion.hotel.Servicio;

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
    public void crearReserva(Reserva reserva) {
        System.out.println("Reserva recibida: " + reserva);
        System.out.println("Habitación ID: " + reserva.getHabitacion().getId());
        System.out.println("Cliente ID: " + reserva.getCliente().getId());
        System.out.println("Empleado ID: " + reserva.getEmpleado().getId());
        System.out.println("Fecha ingreso: " + reserva.getFechaIngreso());
        System.out.println("Fecha salida: " + reserva.getFechaSalida());

        // Calcular el total de la reserva
        calcularTotalReserva(reserva);

        // Mostrar el total calculado
        System.out.println("Total Pago: " + reserva.getTotal());

        Habitacion habitacion = habitacionRepositorio.findById(reserva.getHabitacion().getId())
                .orElseThrow(() -> new IllegalStateException("La habitación no existe."));

        // Modificar esta consulta para manejar múltiples empleados disponibles
        List<Empleado> empleadosDisponibles = empleadoRepositorio.findByDisponibleTrue();
        if (empleadosDisponibles.isEmpty()) {
            throw new IllegalStateException("No hay empleados disponibles.");
        }

        // Seleccionar el primer empleado disponible
        Empleado empleado = empleadosDisponibles.get(0);  // O lógica para elegir uno según sea necesario

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

        reservaRepositorio.save(reserva);
    }

    @Transactional
    public Reserva actualizarReserva(Long reservaId, Reserva reserva) {
        Reserva reservaExistente = reservaRepositorio.findById(reservaId)
                .orElseThrow(() -> new ReservaNoEncontradaExcepcion(reservaId));

        // Verificar si la habitación o empleado están disponibles
        if (!reserva.getHabitacion().isDisponible() &&
                !reserva.getHabitacion().getId().equals(reservaExistente.getHabitacion().getId())) {
            throw new IllegalStateException("La habitación seleccionada no está disponible.");
        }

        // Actualizar los campos de la reserva
        reservaExistente.setCliente(reserva.getCliente());
        reservaExistente.setHabitacion(reserva.getHabitacion());
        reservaExistente.setEmpleado(reserva.getEmpleado());
        reservaExistente.setFechaIngreso(reserva.getFechaIngreso());
        reservaExistente.setFechaSalida(reserva.getFechaSalida());
        reservaExistente.setTotal(reserva.getTotal());
        reservaExistente.setMetodoPago(reserva.getMetodoPago());

        calcularTotalReserva(reservaExistente);

        return reservaRepositorio.save(reservaExistente);
    }

    @Transactional
    private void calcularTotalReserva(Reserva reserva) {
        Habitacion habitacion = habitacionRepositorio.findById(reserva.getHabitacion().getId())
                .orElseThrow(() -> new IllegalStateException("La habitación no existe."));

        long diasEstadia = ChronoUnit.DAYS.between(reserva.getFechaIngreso(), reserva.getFechaSalida());
        if (diasEstadia <= 0) {
            throw new IllegalArgumentException("La estancia debe durar al menos un día.");
        }

        // Calcular el total basado en el precio por noche de la habitación
        double total = diasEstadia * habitacion.getPrecioPorNoche();
        reserva.setTotal(total);
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



