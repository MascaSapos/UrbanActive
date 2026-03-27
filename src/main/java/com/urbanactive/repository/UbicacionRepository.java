package com.urbanactive.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.urbanactive.model.Ubicacion;

@Repository
public interface UbicacionRepository extends JpaRepository<Ubicacion, String> {
}
