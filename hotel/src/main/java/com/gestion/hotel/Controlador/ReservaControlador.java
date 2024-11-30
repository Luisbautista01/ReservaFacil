package com.gestion.hotel.Controlador;

import com.gestion.hotel.Excepciones.ReservaNoEncontradaExcepcion;
import com.gestion.hotel.Modelo.Reserva;
import com.gestion.hotel.Repositorio.ClienteRepositorio;
import com.gestion.hotel.Repositorio.EmpleadoRepositorio;
import com.gestion.hotel.Repositorio.HabitacionRepositorio;
import com.gestion.hotel.Servicio.ReservaServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/v1/reservas")
@CrossOrigin(origins = "http://localhost:8080")
public class ReservaControlador {
    private final ReservaServicio reservaServicio;
    private final HabitacionRepositorio habitacionRepositorio;
    private final ClienteRepositorio clienteRepositorio;
    private final EmpleadoRepositorio empleadoRepositorio;

    @Autowired
    public ReservaControlador(ReservaServicio reservaServicio, HabitacionRepositorio habitacionRepositorio, ClienteRepositorio clienteRepositorio, EmpleadoRepositorio empleadoRepositorio) {
        this.reservaServicio = reservaServicio;
        this.habitacionRepositorio = habitacionRepositorio;
        this.clienteRepositorio = clienteRepositorio;
        this.empleadoRepositorio = empleadoRepositorio;
    }

    @PostMapping("/crear")
    public ResponseEntity<String> crearReserva(@RequestBody Reserva reserva) {
        if (reserva.getId() != null) {
            return ResponseEntity.badRequest().body("No se puede crear una reserva con un ID existente.");
        }
        reservaServicio.crearReserva(reserva);
        return ResponseEntity.ok("Reserva creada correctamente.");
    }


    @PutMapping("/actualizar/{reservaId}")
    public ResponseEntity<String> actualizarReserva(@PathVariable Long reservaId, @RequestBody Reserva reserva) {
        try {
            Reserva reservaActualizada = reservaServicio.actualizarReserva(reservaId, reserva);
            if (reservaActualizada == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("La reserva no se encontró.");
            }
            return ResponseEntity.ok("Reserva actualizada correctamente.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al actualizar la reserva.");
        }
    }

    @GetMapping("/obtener")
    public ResponseEntity<List<Reserva>> obtenerReservas() {
        List<Reserva> reservas = reservaServicio.obtenerReservas();
        return new ResponseEntity<>(reservas, HttpStatus.OK);
    }

    @GetMapping("/obtener/{reservaId}")
    public Reserva obtenerReservaPorId(@PathVariable Long reservaId) {
        return reservaServicio.obtenerReservaPorId(reservaId);

    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<Reserva>> obtenerReservasPorClienteId(@PathVariable Long clienteId) {
        List<Reserva> reservas = reservaServicio.obtenerReservasPorClienteId(clienteId);
        return ResponseEntity.ok(reservas);
    }

    @GetMapping("/empleado/{empleadoId}")
    public ResponseEntity<List<Reserva>> obtenerReservasPorEmpleadoId(@PathVariable Long empleadoId) {
        List<Reserva> reservas = reservaServicio.obtenerReservasPorEmpleadoId(empleadoId);
        return ResponseEntity.ok(reservas);
    }

    @DeleteMapping("/eliminar/{reservaId}")
    public ResponseEntity<String> eliminarReserva(@PathVariable Long reservaId) {
        try {
            reservaServicio.eliminarReserva(reservaId);
            return ResponseEntity.ok("Reserva eliminada correctamente.");
        } catch (ReservaNoEncontradaExcepcion e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
