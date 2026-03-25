package com.urbanactive.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.urbanactive.model.Reserva;
import com.urbanactive.repository.ReservaRepository;

@Service
public class ReservaService {

    private final ReservaRepository reservaRepository;

    public ReservaService(ReservaRepository reservaRepository) {
        this.reservaRepository = reservaRepository;
    }

    public Reserva crear(Reserva reserva) {
        return reservaRepository.save(reserva);
    }

    public Reserva obtenerPorId(String id) {
        return reservaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada con id: " + id));
    }

    public List<Reserva> obtenerTodas() {
        return reservaRepository.findAll();
    }

    public Reserva actualizar(String id, Reserva cambios) {
        Reserva existente = obtenerPorId(id);
        existente.setFechaReserva(cambios.getFechaReserva());
        existente.setEstado(cambios.getEstado());
        existente.setId_usuario(cambios.getId_usuario());
        existente.setId_actividad(cambios.getId_actividad());
        return reservaRepository.save(existente);
    }

    public void borrar(String id) {
        if (!reservaRepository.existsById(id)) {
            throw new IllegalArgumentException("Reserva no encontrada con id: " + id);
        }
        reservaRepository.deleteById(id);
    }
}
