package com.gestion.hotel.Servicio;

import com.gestion.hotel.Modelo.Inventario;
import com.gestion.hotel.Modelo.ReporteAnualDTO;
import com.gestion.hotel.Modelo.ReporteMensualDTO;
import com.gestion.hotel.Modelo.Reserva;
import com.gestion.hotel.Repositorio.HabitacionRepositorio;
import com.gestion.hotel.Repositorio.InventarioRepositorio;
import com.gestion.hotel.Repositorio.ReservaRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class ReporteServicio {
    private final ReservaRepositorio reservaRepositorio;
    private final InventarioRepositorio inventarioRepositorio;
    private final HabitacionRepositorio habitacionRepositorio;
    private final EmpleadoServicio empleadoServicio; // Inyección del servicio de empleados

    @Autowired
    public ReporteServicio(ReservaRepositorio reservaRepositorio, InventarioRepositorio inventarioRepositorio,
                           HabitacionRepositorio habitacionRepositorio, EmpleadoServicio empleadoServicio) {
        this.reservaRepositorio = reservaRepositorio;
        this.inventarioRepositorio = inventarioRepositorio;
        this.habitacionRepositorio = habitacionRepositorio;
        this.empleadoServicio = empleadoServicio;
    }
    private static final Map<Integer, String> MESES;

    static {
        MESES = new HashMap<>();
        MESES.put(1, "Enero");
        MESES.put(2, "Febrero");
        MESES.put(3, "Marzo");
        MESES.put(4, "Abril");
        MESES.put(5, "Mayo");
        MESES.put(6, "Junio");
        MESES.put(7, "Julio");
        MESES.put(8, "Agosto");
        MESES.put(9, "Septiembre");
        MESES.put(10, "Octubre");
        MESES.put(11, "Noviembre");
        MESES.put(12, "Diciembre");
    }

    public Map<String, Object> generarReporteIngresos() {
        List<Object[]> ingresosMes = reservaRepositorio.obtenerIngresosPorMes();

        Map<Integer, Double> ingresosMap = ingresosMes.stream()
                .collect(Collectors.toMap(
                        obj -> ((Number) obj[0]).intValue(),
                        obj -> ((Number) obj[1]).doubleValue()
                ));

        List<Map<String, Object>> reporteMensual = IntStream.rangeClosed(1, 12)
                .mapToObj(mes -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("mes", MESES.get(mes));
                    map.put("ingresos", ingresosMap.getOrDefault(mes, 0.0));
                    return map;
                })
                .collect(Collectors.toList());

        double ingresosTotales = ingresosMap.values().stream()
                .mapToDouble(Double::doubleValue)
                .sum();

        Map<String, Object> reporte = new HashMap<>();
        reporte.put("ingresosTotales", ingresosTotales);
        reporte.put("reservasTotales", reservaRepositorio.count());
        reporte.put("reporteMensual", reporteMensual);

        return reporte;
    }

    public ReporteAnualDTO generarReporteIngresosPorAnio(int anio) {
        List<Object[]> ingresosMes = reservaRepositorio.obtenerIngresosPorMesYAnio(anio);

        if (anio < 2000 || anio > LocalDate.now().getYear()) {
            throw new IllegalArgumentException("Año inválido.");
        }

        Map<Integer, Double> ingresosMap = ingresosMes.stream()
                .collect(Collectors.toMap(
                        obj -> ((Number) obj[0]).intValue(),
                        obj -> ((Number) obj[1]).doubleValue()
                ));

        List<ReporteMensualDTO> reporteMensual = IntStream.rangeClosed(1, 12)
                .mapToObj(mes -> new ReporteMensualDTO(MESES.get(mes), ingresosMap.getOrDefault(mes, 0.0)))
                .collect(Collectors.toList());

        double ingresosTotales = ingresosMap.values().stream()
                .mapToDouble(Double::doubleValue)
                .sum();

        return new ReporteAnualDTO(ingresosTotales, reservaRepositorio.count(), reporteMensual);
    }


    public List<Inventario> generarInventarioPorHabitacion(Long habitacionId) {
        return inventarioRepositorio.findByHabitacionId(habitacionId);
    }

    // Método para calcular ingresos netos de cada habitación
    public List<Map<String, Object>> generarIngresosNetos() {
        return habitacionRepositorio.findAll().stream()
                .map(habitacion -> {
                    // Calcular ingresos por habitación (basado en el precio por noche y reservas)
                    double ingresos = habitacion.getReservas().stream()
                            .mapToDouble(reserva -> reserva.getTotal())
                            .sum();

                    // Calcular gastos por habitación (basado en inventarios)
                    double gastos = inventarioRepositorio.findByHabitacionId(habitacion.getId()).stream()
                            .mapToDouble(Inventario::getCosto)
                            .sum();

                    // Calcular ingresos netos
                    double ingresosNetos = ingresos - gastos;

                    // Crear el reporte
                    Map<String, Object> reporte = new HashMap<>();
                    reporte.put("habitacionId", habitacion.getId());
                    reporte.put("tipo", habitacion.getTipo());
                    reporte.put("ingresos", ingresos);
                    reporte.put("gastos", gastos);
                    reporte.put("ingresosNetos", ingresosNetos);
                    return reporte;
                })
                .collect(Collectors.toList());
    }

    public Map<String, Object> generarGananciasTotales() {
        List<Map<String, Object>> ingresosNetosPorHabitacion = generarIngresosNetos();

        double gananciasTotales = ingresosNetosPorHabitacion.stream()
                .mapToDouble(habitacion -> (double) habitacion.get("ingresosNetos"))
                .sum();

        Map<String, Object> reporte = new HashMap<>();
        reporte.put("gananciasTotales", gananciasTotales);
        reporte.put("detalleHabitaciones", ingresosNetosPorHabitacion);
        return reporte;
    }

    // Método para calcular ganancias y pérdidas, considerando el pago a empleados
    public Map<String, Object> calcularGananciasYPerdidasConEmpleados() {
        double ingresosTotales = calcularIngresosTotales();
        double gastosTotales = calcularGastosTotales();

        // Obtener el pago total a empleados
        Map<String, Double> pagosEmpleados = empleadoServicio.calcularPagoEmpleados();
        double totalPagosEmpleados = pagosEmpleados.values().stream().mapToDouble(Double::doubleValue).sum();

        // Calcular las ganancias netas (ingresos - gastos - pagos empleados)
        double gananciasNetas = ingresosTotales - gastosTotales - totalPagosEmpleados;

        // Calcular las pérdidas en caso de que los gastos sean mayores que los ingresos
        double perdidasTotales = gastosTotales + totalPagosEmpleados > ingresosTotales ?
                (gastosTotales + totalPagosEmpleados) - ingresosTotales : 0;

        // Crear el reporte
        Map<String, Object> reporte = new HashMap<>();
        reporte.put("gananciasTotales", gananciasNetas);
        reporte.put("perdidasTotales", perdidasTotales);
        reporte.put("totalPagosEmpleados", totalPagosEmpleados);

        return reporte;
    }

    private double calcularGastosTotales() {
        return inventarioRepositorio.findAll().stream()
                .mapToDouble(Inventario::getCosto)
                .sum();
    }

    private double calcularIngresosTotales() {
        return reservaRepositorio.findAll().stream()
                .mapToDouble(Reserva::getTotal)
                .sum();
    }
}

