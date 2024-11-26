package com.gestion.hotel.Repositorio;

import com.gestion.hotel.Modelo.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmpleadoRepositorio extends JpaRepository<Empleado, Long> {
    Optional<Empleado> findByCorreoElectronico(String correoElectronico);

    List<Empleado> findByRol(String rol);

    @Query("SELECT e FROM Empleado e WHERE e.disponible = true")
    List<Empleado> findByDisponibleTrue();

    @Query("SELECT e FROM Empleado e WHERE e.disponible = true")
    Optional<Empleado> findFirstByDisponibleTrue();
}
