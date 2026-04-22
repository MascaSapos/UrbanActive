package com.urbanactive.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.urbanactive.model.InformeMeteorologico;

@Repository
public interface InformeMeteorologicoRepository extends JpaRepository<InformeMeteorologico, String> {
    @Query("SELECT i FROM InformeMeteorologico i WHERE i.id_actividad = :actividadId")
    Optional<InformeMeteorologico> findByActividadId(@Param("actividadId") String actividadId);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("DELETE FROM InformeMeteorologico i WHERE i.id_actividad = :actividadId")
    void deleteByActividadId(@Param("actividadId") String actividadId);
}
