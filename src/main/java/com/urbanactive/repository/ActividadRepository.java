package com.urbanactive.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.urbanactive.model.Actividad;
import java.util.List;

@Repository
public interface ActividadRepository extends JpaRepository<Actividad, String> {
    List<Actividad> findByOrganizador_Id(String organizadorId);
}
