package com.gestion.hotel.Repositorio;

import com.gestion.hotel.Modelo.Habitacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HabitacionRepositorio extends JpaRepository<Habitacion, Long> {
    List<Habitacion> findByTipo(String tipo);

    List<Habitacion> findByDisponibleTrue();
}
