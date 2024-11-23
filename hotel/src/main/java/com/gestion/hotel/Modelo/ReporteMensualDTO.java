package com.gestion.hotel.Modelo;

public class ReporteMensualDTO {
    private String mes;
    private double ingresos;

    public ReporteMensualDTO(String mes, double ingresos) {
        this.mes = mes;
        this.ingresos = ingresos;
    }

    public String getMes() {
        return mes;
    }

    public void setMes(String mes) {
        this.mes = mes;
    }

    public double getIngresos() {
        return ingresos;
    }

    public void setIngresos(double ingresos) {
        this.ingresos = ingresos;
    }

}