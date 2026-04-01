package com.urbanactive.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.urbanactive.model.Reserva;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, String> {

    @Query("SELECT COUNT(r) FROM Reserva r WHERE r.id_actividad = :idActividad AND r.estado = 'CONFIRMADA'")
    int countActivasPorActividad(@Param("idActividad") String idActividad);
}
