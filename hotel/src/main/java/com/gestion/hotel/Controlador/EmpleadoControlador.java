package com.gestion.hotel.Controlador;

import com.gestion.hotel.Excepciones.EmpleadoExistenteExcepcion;
import com.gestion.hotel.Excepciones.EmpleadoNoEncontradoExcepcion;
import com.gestion.hotel.Excepciones.InformacionIncompletaExcepcion;
import com.gestion.hotel.Modelo.Empleado;
import com.gestion.hotel.Servicio.EmpleadoServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/empleados")
@CrossOrigin(origins = "http://localhost:8080")
public class EmpleadoControlador {
    private final EmpleadoServicio empleadoServicio;

    @Autowired
    public EmpleadoControlador(EmpleadoServicio empleadoServicio) {
        this.empleadoServicio = empleadoServicio;
    }

    @PostMapping("/crear")
    public ResponseEntity<String> crearEmpleado(@Validated @RequestBody Empleado empleado) {
        try {
            empleadoServicio.crearEmpleado(empleado);
            return ResponseEntity.status(HttpStatus.CREATED).body("Empleado creado correctamente");
        } catch (EmpleadoExistenteExcepcion | InformacionIncompletaExcepcion e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/actualizar/{empleadoId}")
    public ResponseEntity<String> actualizarEmpleado(@PathVariable("empleadoId") Long empleadoId, @RequestBody Empleado empleado) {
        try {
            empleadoServicio.actualizarEmpleado(empleadoId, empleado);
            return ResponseEntity.ok("Empleado actualizado correctamente.");
        } catch (EmpleadoNoEncontradoExcepcion | InformacionIncompletaExcepcion e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/obtener")
    public ResponseEntity<?> obtenerEmpleados(
            @RequestParam(required = false) String rol,
            @RequestParam(required = false) Long id) {
        if (rol != null) {
            return obtenerEmpleadosPorRol(rol);
        } else if (id != null) {
            return obtenerEmpleadosPorId(id);
        }
        return ResponseEntity.ok(empleadoServicio.obtenerEmpleados());
    }

    // Endpoint para verificar disponibilidad de empleados
    @GetMapping("/disponibilidad")
    public ResponseEntity<List<Empleado>> verificarDisponibilidadEmpleados() {
        List<Empleado> empleadosDisponibles = empleadoServicio.obtenerEmpleadosDisponibles();
        return ResponseEntity.ok(empleadosDisponibles);
    }

    @GetMapping("/obtener/rol/{rol}")
    public ResponseEntity<List<Empleado>> obtenerEmpleadosPorRol(@PathVariable("rol") String rol) {
        return ResponseEntity.ok(empleadoServicio.obtenerEmpleadosPorRol(rol));
    }

    @GetMapping("/obtener/id/{empleadoId}")
    public ResponseEntity<?> obtenerEmpleadosPorId(@PathVariable("empleadoId") Long empleadoId) {
        try {
            Empleado empleado = empleadoServicio.obtenerEmpleadoPorId(empleadoId);
            return ResponseEntity.ok(empleado);
        } catch (EmpleadoNoEncontradoExcepcion e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @DeleteMapping("/eliminar/{empleadoId}")
    public ResponseEntity<String> eliminarEmpleado(@PathVariable("empleadoId") Long empleadoId) {
        try {
            empleadoServicio.eliminarEmpleado(empleadoId);
            return ResponseEntity.ok("Empleado eliminado correctamente.");
        } catch (EmpleadoNoEncontradoExcepcion e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
