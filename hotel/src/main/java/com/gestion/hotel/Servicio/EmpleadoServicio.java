package com.gestion.hotel.Servicio;

import com.gestion.hotel.Excepciones.EmpleadoExistenteExcepcion;
import com.gestion.hotel.Excepciones.EmpleadoNoEncontradoExcepcion;
import com.gestion.hotel.Excepciones.InformacionIncompletaExcepcion;
import com.gestion.hotel.Modelo.Empleado;
import com.gestion.hotel.Modelo.Reserva;
import com.gestion.hotel.Repositorio.EmpleadoRepositorio;
import com.gestion.hotel.Repositorio.ReservaRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EmpleadoServicio {
    private final EmpleadoRepositorio empleadoRepositorio;
    private final ReservaRepositorio reservaRepositorio;

    @Autowired
    public EmpleadoServicio(EmpleadoRepositorio empleadoRepositorio, ReservaRepositorio reservaRepositorio) {
        this.empleadoRepositorio = empleadoRepositorio;
        this.reservaRepositorio = reservaRepositorio;
    }

    public void crearEmpleado(Empleado empleado) {
        if (empleadoRepositorio.findByCorreoElectronico(empleado.getCorreoElectronico()).isPresent()) {
            throw new EmpleadoExistenteExcepcion(empleado.getCorreoElectronico());
        }
        if ((empleado.getNombre() == null) ||
                (empleado.getApellido() == null) ||
                (empleado.getRol() == null) ||
                (empleado.getCorreoElectronico() == null)) {
            throw new InformacionIncompletaExcepcion();
        }
        empleadoRepositorio.save(empleado);
    }

    public void actualizarEmpleado(Long empleadoId, Empleado empleado) {
        Empleado empleadoActualizar = empleadoRepositorio.findById(empleadoId)
                .orElseThrow(() -> new EmpleadoNoEncontradoExcepcion(empleadoId));

        empleadoActualizar.setNombre(empleado.getNombre());
        empleadoActualizar.setApellido(empleado.getApellido());
        empleadoActualizar.setRol(empleado.getRol());
        empleadoActualizar.setCorreoElectronico(empleado.getCorreoElectronico());

        if ((empleado.getNombre() == null) ||
                (empleado.getApellido() == null) ||
                (empleado.getCorreoElectronico() == null)) {
            throw new InformacionIncompletaExcepcion();
        }

        empleadoRepositorio.save(empleadoActualizar);
    }

    public List<Empleado> obtenerEmpleados() {
        return empleadoRepositorio.findAll();
    }

    public List<Empleado> obtenerEmpleadosDisponibles() {
        return empleadoRepositorio.findByDisponibleTrue(); // Asumiendo que tienes este método en tu repositorio
    }

    public List<Empleado> obtenerEmpleadosPorRol(String rol) {
        return empleadoRepositorio.findByRol(rol);
    }

    public Empleado obtenerEmpleadoPorId(Long empleadoId) {
        return empleadoRepositorio.findById(empleadoId).orElseThrow(() -> new EmpleadoNoEncontradoExcepcion(empleadoId));
    }

    @Transactional
    public void eliminarEmpleado(Long empleadoId) {
        Empleado empleado = empleadoRepositorio.findById(empleadoId)
                .orElseThrow(() -> new EmpleadoNoEncontradoExcepcion(empleadoId));
        empleadoRepositorio.delete(empleado);
    }

    public Map<String, Double> calcularPagoEmpleados() {
        List<Empleado> empleados = empleadoRepositorio.findAll();
        Map<String, Double> pagos = new HashMap<>();

        // Calculamos el pago para cada empleado
        for (Empleado empleado : empleados) {
            // Obtener todas las reservas asignadas al empleado
            double totalReservasEmpleado = reservaRepositorio.findByEmpleadoId(empleado.getId()).stream()
                    .mapToDouble(Reserva::getTotal) // Asumiendo que la reserva tiene un campo `total`
                    .sum();

            // Calcular el pago como el 5% de las reservas
            double pagoEmpleado = totalReservasEmpleado * 0.1;

            // Usamos el nombre del empleado como clave
            pagos.put(empleado.getNombre(), pagoEmpleado);
        }

        return pagos;
    }


}
