package com.urbanactive.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.urbanactive.model.Reserva;
import com.urbanactive.model.Actividad;
import com.urbanactive.model.Usuario;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, String> {
    long countByActividad(Actividad actividad);
    List<Reserva> findByUsuario(Usuario usuario);
}
