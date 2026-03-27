package com.urbanactive.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.urbanactive.model.Actividad;

@Repository
public interface ActividadRepository extends JpaRepository<Actividad, String> {
}
