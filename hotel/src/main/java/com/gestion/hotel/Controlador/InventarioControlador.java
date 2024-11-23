package com.gestion.hotel.Controlador;

import com.gestion.hotel.Excepciones.HabitacionNoEncontradaExcepcion;
import com.gestion.hotel.Excepciones.InventarioNoEncontradoExcepcion;
import com.gestion.hotel.Modelo.Inventario;
import com.gestion.hotel.Servicio.InventarioServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventarios")
@CrossOrigin(origins = "http://localhost:8080")
public class InventarioControlador {

    private final InventarioServicio inventarioServicio;

    @Autowired
    public InventarioControlador(InventarioServicio inventarioServicio) {
        this.inventarioServicio = inventarioServicio;
    }

    @PostMapping("/agregar")
    public ResponseEntity<String> agregarInventario(@RequestBody Inventario inventario) {
        try {
            inventarioServicio.agregarInventario(inventario);
            return ResponseEntity.ok("Inventario creado correctamente");
        } catch (HabitacionNoEncontradaExcepcion e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Habitación no encontrada.");
        }
    }

    @PutMapping("/actualizar/{id}")
    public ResponseEntity<String> actualizarInventario(@PathVariable Long id, @RequestBody Inventario inventario) {
        try {
            inventarioServicio.actualizarInventario(id, inventario);
            return ResponseEntity.ok("Inventario actualizado correctamente");
        } catch (InventarioNoEncontradoExcepcion e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Inventario no encontrado.");
        } catch (HabitacionNoEncontradaExcepcion e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Habitación no encontrada.");
        }
    }

    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<String> eliminarInventario(@PathVariable Long id) {
        try {
            inventarioServicio.eliminarInventario(id);
            return ResponseEntity.ok("Inventario eliminado correctamente");
        } catch (InventarioNoEncontradoExcepcion e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Inventario no encontrado.");
        }
    }

    @GetMapping("/obtener")
    public List<Inventario> obtenerInventarios() {
        return inventarioServicio.obtenerInventarios();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Inventario> obtenerInventarioPorId(@PathVariable Long id) {
        try {
            Inventario inventario = inventarioServicio.obtenerInventarioPorId(id);
            return ResponseEntity.ok(inventario);
        } catch (InventarioNoEncontradoExcepcion e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
}
