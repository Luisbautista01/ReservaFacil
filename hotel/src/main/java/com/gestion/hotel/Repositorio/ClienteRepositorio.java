package com.gestion.hotel.Repositorio;

import com.gestion.hotel.Modelo.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClienteRepositorio extends JpaRepository<Cliente, Long> {
    boolean existsByCorreoElectronico(String correoElectronico);
    Optional<Cliente> findByCorreoElectronico(String correoElectronico);
}
