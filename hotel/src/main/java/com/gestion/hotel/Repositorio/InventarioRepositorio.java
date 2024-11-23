package com.gestion.hotel.Repositorio;

import com.gestion.hotel.Modelo.Inventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventarioRepositorio extends JpaRepository<Inventario, Long> {
    List<Inventario> findByHabitacionId(Long habitacionId);
}
