package com.gestion.hotel.Modelo;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "reserva")
public class Reserva {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate fechaIngreso;
    private LocalDate fechaSalida;
    @Column(nullable = false)
    private double total;

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente; // Una reserva pertenece a un cliente específico.

    @ManyToOne
    @JoinColumn(name = "habitacion_id", nullable = false)
    private Habitacion habitacion; // Una reserva está asociada a una sola habitación.

    @ManyToOne
    @JoinColumn(name = "empleado_id")
    private Empleado empleado; // Empleado responsable de la reserva.

    public Reserva() {
    }

    public double calcularTotal(Reserva reserva) {
        long dias = ChronoUnit.DAYS.between(reserva.getFechaIngreso(), reserva.getFechaSalida());
        return dias * reserva.getHabitacion().getPrecioPorNoche();
    }


    public Reserva(Long id, LocalDate fechaIngreso, LocalDate fechaSalida, double total, Cliente cliente, Habitacion habitacion, Empleado empleado) {
        this.id = id;
        this.fechaIngreso = fechaIngreso;
        this.fechaSalida = fechaSalida;
        this.total = total;
        this.cliente = cliente;
        this.habitacion = habitacion;
        this.empleado = empleado;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getFechaIngreso() {
        return fechaIngreso;
    }

    public void setFechaIngreso(LocalDate fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }

    public LocalDate getFechaSalida() {
        return fechaSalida;
    }

    public void setFechaSalida(LocalDate fechaSalida) {
        this.fechaSalida = fechaSalida;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Habitacion getHabitacion() {
        return habitacion;
    }

    public void setHabitacion(Habitacion habitacion) {
        this.habitacion = habitacion;
    }

    public Empleado getEmpleado() {
        return empleado;
    }

    public void setEmpleado(Empleado empleado) {
        this.empleado = empleado;
    }
}
