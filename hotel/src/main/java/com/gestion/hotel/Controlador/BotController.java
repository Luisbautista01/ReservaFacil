package com.gestion.hotel.Controlador;

import com.gestion.hotel.Modelo.*;
import com.gestion.hotel.Repositorio.HabitacionRepositorio;
import com.gestion.hotel.Servicio.ClienteServicio;
import com.gestion.hotel.Servicio.EmpleadoServicio;
import com.gestion.hotel.Servicio.ReservaServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api/v1/bot")
@CrossOrigin(origins = "http://localhost:8080")
public class BotController {

    private final ClienteServicio clienteServicio;
    private final ReservaServicio reservaServicio;
    private final HabitacionRepositorio habitacionRepositorio;
    private final EmpleadoServicio empleadoServicio;

    @Autowired
    public BotController(ClienteServicio clienteServicio, ReservaServicio reservaServicio,
                         HabitacionRepositorio habitacionRepositorio, EmpleadoServicio empleadoServicio) {
        this.clienteServicio = clienteServicio;
        this.reservaServicio = reservaServicio;
        this.habitacionRepositorio = habitacionRepositorio;
        this.empleadoServicio = empleadoServicio;
    }

    @PostMapping("/handleMessage")
    public ResponseEntity<String> handleMessage(@RequestBody BotRequest botRequest) {
        String intent = botRequest.getIntent();

        switch (intent) {
            case "saludo":
                return ResponseEntity.ok("¡Hola! ¿En qué puedo ayudarte hoy?");

            case "registro_cliente":
                return ResponseEntity.ok("¿Ya estás registrado? Si no, ¿te gustaría registrarte?");

            case "crear_reserva":
                // Obtener habitaciones disponibles
                List<Habitacion> habitacionesDisponibles = habitacionRepositorio.findByDisponibleTrue();
                String habitacionesResponse = "Las habitaciones disponibles son:\n";
                for (Habitacion habitacion : habitacionesDisponibles) {
                    habitacionesResponse += "ID: " + habitacion.getId() + " | Tipo: " + habitacion.getTipo() + " | Precio: " + habitacion.getPrecioPorNoche() + "\n";
                }
                return ResponseEntity.ok(habitacionesResponse + "¿Qué habitación te gustaría reservar?");

            case "consultar_reserva":
                // Consultar reservas
                List<Reserva> reservas = reservaServicio.obtenerReservasPorClienteId(botRequest.getClienteId());
                return ResponseEntity.ok("Tus reservas son: " + reservas);

            case "confirmar_registro":
                // Proceso de registro
                Cliente cliente = new Cliente();
                cliente.setNombre(botRequest.getNombre());
                cliente.setCorreoElectronico(botRequest.getCorreo());
                cliente.setTelefono(botRequest.getTelefono());
                clienteServicio.crearCliente(cliente);
                return ResponseEntity.ok("¡Te has registrado exitosamente!");

            case "confirmar_reserva":
                // Proceso de reserva
                Reserva reserva = new Reserva();

                // Obtener el cliente usando el clienteId de la solicitud
                cliente = clienteServicio.obtenerClientesPorId(botRequest.getClienteId());
                reserva.setCliente(cliente);
                reserva.setFechaIngreso(botRequest.getFechaIngreso());
                reserva.setFechaSalida(botRequest.getFechaSalida());
                reserva.setTotal(botRequest.getTotal());

                // Asignar un empleado disponible
                List<Empleado> empleado = empleadoServicio.obtenerEmpleadosDisponibles();

                // Obtener la habitación seleccionada por el cliente
                Habitacion habitacion = habitacionRepositorio.findById(botRequest.getHabitacionId())
                        .orElseThrow(() -> new IllegalArgumentException("Habitación no encontrada"));
                reserva.setHabitacion(habitacion);

                reservaServicio.crearReserva(reserva);
                return ResponseEntity.ok("Tu reserva ha sido confirmada con el empleado " + empleado + ".");

            default:
                return ResponseEntity.badRequest().body("No entendí tu solicitud. ¿Puedes intentar nuevamente?");
        }
    }
}




