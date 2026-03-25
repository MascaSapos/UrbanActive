package com.urbanactive.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.urbanactive.model.Reserva;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, String> {
}
