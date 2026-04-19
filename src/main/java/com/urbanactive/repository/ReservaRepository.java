package com.urbanactive.repository;

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
    List<Reserva> findByActividad(Actividad actividad);

    @org.springframework.data.jpa.repository.Modifying
    @Query("DELETE FROM Reserva r WHERE r.actividad.id = :actividadId")
    void deleteAllByActividadId(@Param("actividadId") String actividadId);

    @Query("SELECT COUNT(r) FROM Reserva r WHERE r.actividad.id = :idActividad AND r.estado = 'CONFIRMADA'")
    int countActivasPorActividad(@Param("idActividad") String idActividad);
}
