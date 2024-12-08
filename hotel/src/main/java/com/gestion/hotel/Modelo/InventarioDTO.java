package com.gestion.hotel.Modelo;

public class InventarioDTO {
    private Long id;
    private String item;
    private int cantidad;
    private double costo;
    private Long habitacionId; // Solo el id de la habitación

    public InventarioDTO(Long id, String item, int cantidad, double costo, Long habitacionId) {
        this.id = id;
        this.item = item;
        this.cantidad = cantidad;
        this.costo = costo;
        this.habitacionId = habitacionId;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public double getCosto() {
        return costo;
    }

    public void setCosto(double costo) {
        this.costo = costo;
    }

    public Long getHabitacionId() {
        return habitacionId;
    }

    public void setHabitacionId(Long habitacionId) {
        this.habitacionId = habitacionId;
    }
}
