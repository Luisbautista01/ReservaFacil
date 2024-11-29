package com.gestion.hotel.Controlador;

import com.gestion.hotel.Excepciones.ReservaNoEncontradaExcepcion;
import com.gestion.hotel.Modelo.Reserva;
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

    @Autowired
    public ReservaControlador(ReservaServicio reservaServicio) {
        this.reservaServicio = reservaServicio;
    }

    @PostMapping("/crear")
    public ResponseEntity<Reserva> crearReserva(@RequestBody Reserva reserva) {
        try {
            Reserva nuevaReserva = reservaServicio.crearReserva(reserva);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevaReserva);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
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
