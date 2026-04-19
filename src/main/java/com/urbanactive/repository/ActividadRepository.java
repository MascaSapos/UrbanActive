package com.urbanactive.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.urbanactive.model.Actividad;

@Repository
public interface ActividadRepository extends JpaRepository<Actividad, String> {
    @org.springframework.data.jpa.repository.Query("SELECT a FROM Actividad a WHERE a.organizadorEmail = :email OR a.organizadorEmail IS NULL OR TRIM(a.organizadorEmail) = ''")
    List<Actividad> findByOrganizadorEmailIncludeNulls(@org.springframework.data.repository.query.Param("email") String email);
}
