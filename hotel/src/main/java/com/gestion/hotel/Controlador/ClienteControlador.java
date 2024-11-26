package com.gestion.hotel.Controlador;

import com.gestion.hotel.Excepciones.ClienteExistenteExcepcion;
import com.gestion.hotel.Excepciones.ClienteNoEncontradoExcepcion;
import com.gestion.hotel.Excepciones.InformacionIncompletaExcepcion;
import com.gestion.hotel.Modelo.Cliente;
import com.gestion.hotel.Servicio.ClienteServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/clientes")
@CrossOrigin(origins = "http://localhost:8080")
public class ClienteControlador {
    private final ClienteServicio clienteServicio;

    @Autowired
    public ClienteControlador(ClienteServicio clienteServicio) {
        this.clienteServicio = clienteServicio;
    }

    @PostMapping("/crear")
    public ResponseEntity<String> crearCliente(@RequestBody Cliente cliente) {
        if (!cliente.isConsentimiento()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Debe aceptar el consentimiento de datos.");
        }
        try {
            clienteServicio.crearCliente(cliente);
            return ResponseEntity.status(HttpStatus.CREATED).body("Cliente creado correctamente.");
        } catch (ClienteExistenteExcepcion | InformacionIncompletaExcepcion e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/actualizar/{clienteId}")
    public ResponseEntity<String> actualizarCliente(@PathVariable("clienteId") Long clienteId, @RequestBody Cliente cliente) {
        try {
            clienteServicio.actualizarCliente(clienteId, cliente);
            return ResponseEntity.ok("Cliente actualizado correctamente.");
        } catch (ClienteNoEncontradoExcepcion | InformacionIncompletaExcepcion e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/obtener")
    public ResponseEntity<List<Cliente>> obtenerClientes() {
        List<Cliente> clientes = clienteServicio.obtenerClientes();
        return ResponseEntity.ok(clientes);
    }

    @GetMapping("/obtener/{clienteId}")
    public ResponseEntity<?> obtenerClientePorId(@PathVariable("clienteId") Long clienteId) {
        try {
            Cliente cliente = clienteServicio.obtenerClientesPorId(clienteId);
            return ResponseEntity.ok(cliente);
        } catch (ClienteNoEncontradoExcepcion e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/obtener-por-correo")
    public ResponseEntity<?> obtenerClientePorCorreo(@RequestParam("correo") String correoElectronico) {
        try {
            Cliente cliente = clienteServicio.obtenerPorCorreoElectronico(correoElectronico);
            return ResponseEntity.ok(cliente);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @DeleteMapping("/eliminar/{clienteId}")
    public ResponseEntity<String> eliminarCliente(@PathVariable("clienteId") Long clienteId) {
        try {
            clienteServicio.eliminarCliente(clienteId);
            return ResponseEntity.ok("Cliente: " + clienteId + " eliminado correctamente.");
        } catch (ClienteNoEncontradoExcepcion e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
