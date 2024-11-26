package com.gestion.hotel.Controlador;

import com.gestion.hotel.Excepciones.HabitacionNoEncontradaExcepcion;
import com.gestion.hotel.Modelo.Habitacion;
import com.gestion.hotel.Servicio.HabitacionServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/habitaciones")
@CrossOrigin(origins = "http://localhost:8080")

public class HabitacionControlador {
    private final HabitacionServicio habitacionServicio;

    @Autowired
    public HabitacionControlador(HabitacionServicio habitacionServicio) {
        this.habitacionServicio = habitacionServicio;
    }

    @PostMapping("/crear")
    public ResponseEntity<String> crearHabitacion(@RequestBody Habitacion habitacion) {
        try {
            habitacionServicio.crearHabitacion(habitacion);
            return ResponseEntity.ok("Habitación creada correctamente.");
        } catch (HabitacionNoEncontradaExcepcion e) { // Cambia Exception a la excepción específica que necesitas
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/actualizar/{habitacionId}")
    public ResponseEntity<String> actualizarHabitacion(@PathVariable("habitacionId") Long habitacionId, @RequestBody Habitacion habitacion) {
        System.out.println("Actualizando habitación con ID: " + habitacionId); // Agregar esto para depuración
        try {
            habitacionServicio.actualizarHabitacion(habitacionId, habitacion);
            return ResponseEntity.ok("Habitación actualizada correctamente.");
        } catch (HabitacionNoEncontradaExcepcion e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/obtener")
    public ResponseEntity<List<Habitacion>> obtenerHabitaciones() {
        return ResponseEntity.ok(habitacionServicio.obtenerHabitaciones());
    }

    // Endpoint para verificar disponibilidad de habitaciones
    @GetMapping("/disponibilidad")
    public ResponseEntity<List<Habitacion>> verificarDisponibilidadHabitaciones() {
        List<Habitacion> habitacionesDisponibles = habitacionServicio.obtenerHabitacionesDisponibles();
        return ResponseEntity.ok(habitacionesDisponibles);
    }

    @GetMapping("/obtener/tipo/{tipo}")
    public ResponseEntity<List<Habitacion>> obtenerHabitacionesPorTipo(@PathVariable("tipo") String tipo) {
        return ResponseEntity.ok(habitacionServicio.obtenerHabitacionesPorTipo(tipo));
    }

    @GetMapping("/obtener/{habitacionId}")
    public ResponseEntity<?> obtenerHabitacionesPorId(@PathVariable("habitacionId") Long habitacionId) {
        try {
            Habitacion habitacion = habitacionServicio.obtenerHabitacionPorId(habitacionId);
            return ResponseEntity.ok(habitacion);
        } catch (HabitacionNoEncontradaExcepcion e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @DeleteMapping("/eliminar/{habitacionId}")
    public ResponseEntity<String> eliminarHabitacion(@PathVariable("habitacionId") Long habitacionId) {
        try {
            habitacionServicio.eliminarHabitacion(habitacionId);
            return ResponseEntity.ok("Habitación elimanada correctamente");

        } catch (HabitacionNoEncontradaExcepcion e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

}

