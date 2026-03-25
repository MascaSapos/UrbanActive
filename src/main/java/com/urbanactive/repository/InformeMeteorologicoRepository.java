package com.urbanactive.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.urbanactive.model.InformeMeteorologico;

@Repository
public interface InformeMeteorologicoRepository extends JpaRepository<InformeMeteorologico, String> {
}
