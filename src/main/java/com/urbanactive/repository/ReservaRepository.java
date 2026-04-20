package com.urbanactive.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.urbanactive.model.Reserva;
import com.urbanactive.model.Actividad;
import com.urbanactive.model.Usuario;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, String> {
    
    long countByActividad(Actividad actividad);
    
    List<Reserva> findByUsuario(Usuario usuario);

    @Query("SELECT COUNT(r) FROM Reserva r WHERE r.actividad.Id = :idActividad AND r.estado = 'CONFIRMADA'")
    int countActivasPorActividad(@Param("idActividad") String idActividad);

    // Novedades para DEPORTISTA: actividades canceladas en las que tiene plaza
    @Query("SELECT COUNT(r) FROM Reserva r WHERE r.usuario.Id = :userId AND r.actividad.estado = 'CANCELADA'")
    long countActividadesCanceladasDeportista(@Param("userId") String userId);

    // Novedades para ORGANIZADOR: cualquier reserva nueva/modificada reciente en sus actividades
    @Query("SELECT COUNT(r) FROM Reserva r WHERE r.actividad.organizador.Id = :orgId AND r.fechaReserva >= :desde")
    long countReservasRecientesEnMisActividades(@Param("orgId") String orgId, @Param("desde") LocalDateTime desde);
}
