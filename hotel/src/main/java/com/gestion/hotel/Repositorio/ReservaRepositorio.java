package com.gestion.hotel.Repositorio;

import com.gestion.hotel.Modelo.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservaRepositorio extends JpaRepository<Reserva, Long> {
    List<Reserva> findByClienteId(Long clienteId);

    List<Reserva> findByEmpleadoId(Long empleadoId);

    @Query("SELECT MONTH(r.fechaIngreso) AS mes, SUM(r.total) AS ingresos FROM Reserva r WHERE r.fechaIngreso IS NOT NULL GROUP BY MONTH(r.fechaIngreso)")
    List<Object[]> obtenerIngresosPorMes();

    @Query("SELECT EXTRACT(MONTH FROM r.fechaIngreso) AS mes, SUM(r.total) FROM Reserva r WHERE EXTRACT(YEAR FROM r.fechaIngreso) = :anio GROUP BY mes ORDER BY mes")
    List<Object[]> obtenerIngresosPorMesYAnio(@Param("anio") int anio);
}
