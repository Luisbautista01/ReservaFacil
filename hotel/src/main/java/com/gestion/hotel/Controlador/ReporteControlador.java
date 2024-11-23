package com.gestion.hotel.Controlador;

import com.gestion.hotel.Modelo.Inventario;
import com.gestion.hotel.Modelo.ReporteAnualDTO;
import com.gestion.hotel.Servicio.EmpleadoServicio;
import com.gestion.hotel.Servicio.ReporteServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/reportes")
@CrossOrigin(origins = "http://localhost:8080")
public class ReporteControlador {
    private final ReporteServicio reporteServicio;
    private final EmpleadoServicio empleadoServicio;

    @Autowired
    public ReporteControlador(ReporteServicio reporteServicio, EmpleadoServicio empleadoServicio) {
        this.reporteServicio = reporteServicio;
        this.empleadoServicio = empleadoServicio;
    }

    @GetMapping("/ingresos")
    public ResponseEntity<Map<String, Object>> obtenerReporteIngresos() {
        Map<String, Object> reporte = reporteServicio.generarReporteIngresos();
        return ResponseEntity.ok(reporte);
    }

    @GetMapping("/ingresos/anio")
    public ResponseEntity<ReporteAnualDTO> obtenerReporteIngresosPorAnio(@RequestParam(defaultValue = "2024") int anio) {
        ReporteAnualDTO reporte = reporteServicio.generarReporteIngresosPorAnio(anio);
        System.out.println("Año recibido: " + anio);
        return ResponseEntity.ok(reporte);

    }

    @GetMapping("/inventario/{habitacionId}")
    public ResponseEntity<List<Inventario>> obtenerInventarioPorHabitacion(@PathVariable Long habitacionId) {
        List<Inventario> inventarios = reporteServicio.generarInventarioPorHabitacion(habitacionId);
        return ResponseEntity.ok(inventarios);
    }

    @GetMapping("/ingresosNetos")
    public ResponseEntity<List<Map<String, Object>>> obtenerReporteIngresosNetos() {
        return ResponseEntity.ok(reporteServicio.generarIngresosNetos());
    }

    @GetMapping("/gananciasTotales")
    public ResponseEntity<Map<String, Object>> obtenerGananciasTotales() {
        return ResponseEntity.ok(reporteServicio.generarGananciasTotales());
    }

    @GetMapping("/perdidas-y-ganancias")
    public ResponseEntity<Map<String, Object>> obtenerPerdidasYGanancias() {
        Map<String, Object> reporte = reporteServicio.calcularGananciasYPerdidasConEmpleados();
        return ResponseEntity.ok(reporte);
    }

    @GetMapping("/pagos-empleados")
    public ResponseEntity<Map<String, Object>> obtenerPagoEmpleados() {
        Map<String, Double> pagos = empleadoServicio.calcularPagoEmpleados();
        if (pagos.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        // Calculamos el total de pagos
        double totalPagos = pagos.values().stream().mapToDouble(Double::doubleValue).sum();

        // Transformamos el mapa en una lista de objetos JSON
        List<Map<String, Object>> empleados = pagos.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> empleado = new HashMap<>();
                    empleado.put("nombre", entry.getKey());
                    empleado.put("pago", entry.getValue());
                    return empleado;
                })
                .toList();

        // Construimos el reporte
        Map<String, Object> reporte = new HashMap<>();
        reporte.put("pagosTotales", totalPagos);
        reporte.put("empleados", empleados);

        return ResponseEntity.ok(reporte);
    }
}

