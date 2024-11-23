package com.gestion.hotel.Modelo;

import java.util.List;

public class ReporteAnualDTO {
    private double ingresosTotales;
    private long reservasTotales;
    private List<ReporteMensualDTO> reporteMensual;

    public ReporteAnualDTO(double ingresosTotales, long reservasTotales, List<ReporteMensualDTO> reporteMensual) {
        this.ingresosTotales = ingresosTotales;
        this.reservasTotales = reservasTotales;
        this.reporteMensual = reporteMensual;
    }

    public double getIngresosTotales() {
        return ingresosTotales;
    }

    public void setIngresosTotales(double ingresosTotales) {
        this.ingresosTotales = ingresosTotales;
    }

    public long getReservasTotales() {
        return reservasTotales;
    }

    public void setReservasTotales(long reservasTotales) {
        this.reservasTotales = reservasTotales;
    }

    public List<ReporteMensualDTO> getReporteMensual() {
        return reporteMensual;
    }

    public void setReporteMensual(List<ReporteMensualDTO> reporteMensual) {
        this.reporteMensual = reporteMensual;
    }

}