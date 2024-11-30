package com.gestion.hotel.Modelo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "reserva")
public class Reserva {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaIngreso;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaSalida;
    @Column(nullable = false)
    private double total;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MetodoPago metodoPago;

    public enum MetodoPago {
        TARJETA_CREDITO,
        TARJETA_DEBITO,
        EFECTIVO,
        TRANSFERENCIA_BANCARIA;

        @JsonCreator
        public static MetodoPago fromString(String value) {
            try {
                return MetodoPago.valueOf(value.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Valor no válido para MetodoPago: " + value);
            }
        }
    }

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

    public Reserva(Long id, LocalDate fechaIngreso, LocalDate fechaSalida, double total, Cliente cliente, Habitacion habitacion, Empleado empleado) {
        this.id = id;
        this.fechaIngreso = fechaIngreso;
        this.fechaSalida = fechaSalida;
        this.total = total;
        this.cliente = cliente;
        this.habitacion = habitacion;
        this.empleado = empleado;
    }

    public MetodoPago getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(MetodoPago metodoPago) {
        this.metodoPago = metodoPago;
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
