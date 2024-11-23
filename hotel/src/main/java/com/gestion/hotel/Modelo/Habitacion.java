package com.gestion.hotel.Modelo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "habitacion")
public class Habitacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String tipo;
    private int capacidad;
    private double precioPorNoche;
    private boolean disponible = true;
    private String imagenUrl;

    // Una habitación puede tener múltiples reservas a lo largo del tiempo, pero cada reserva está asociada a una única habitación.
    @OneToMany(mappedBy = "habitacion")
    @JsonIgnore
    private List<Reserva> reservas;

    @OneToMany(mappedBy = "habitacion")
    @JsonIgnore
    private List<Inventario> inventarios;

    public Habitacion() {
    }

    public Habitacion(Long id, String tipo, int capacidad, double precioPorNoche, boolean disponible, String imagenUrl, List<Reserva> reservas, List<Inventario> inventarios) {
        this.id = id;
        this.tipo = tipo;
        this.capacidad = capacidad;
        this.precioPorNoche = precioPorNoche;
        this.disponible = disponible;
        this.imagenUrl = imagenUrl;
        this.reservas = reservas;
        this.inventarios = inventarios;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public int getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(int capacidad) {
        this.capacidad = capacidad;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }

    public double getPrecioPorNoche() {
        return precioPorNoche;
    }

    public void setPrecioPorNoche(double precioPorNoche) {
        this.precioPorNoche = precioPorNoche;
    }

    public List<Reserva> getReservas() {
        return reservas;
    }

    public void setReservas(List<Reserva> reservas) {
        this.reservas = reservas;
    }

    public List<Inventario> getInventarios() {
        return inventarios;
    }

    public void setInventarios(List<Inventario> inventarios) {
        this.inventarios = inventarios;
    }
}