package com.urbanactive.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.urbanactive.model.Actividad;
import com.urbanactive.model.Reserva;
import com.urbanactive.model.Usuario;
import com.urbanactive.repository.ReservaRepository;
import com.urbanactive.service.ActividadService;

@Service
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final UsuarioService usuarioService;
    private final ActividadService actividadService;

    public ReservaService(ReservaRepository reservaRepository, UsuarioService usuarioService, ActividadService actividadService) {
        this.reservaRepository = reservaRepository;
        this.usuarioService = usuarioService;
        this.actividadService = actividadService;
    }

    /** Crea una reserva para el usuario autenticado (identificado por email). */
    public Reserva crearParaUsuario(String actividadId, String email) {
        Usuario usuario = usuarioService.obtenerPorEmail(email);
        Actividad actividad = actividadService.obtenerPorId(actividadId);

        long ocupadas = reservaRepository.countByActividad(actividad);
        if (ocupadas >= actividad.getPlazasTotal()) {
            throw new IllegalArgumentException("No hay plazas disponibles para esta actividad.");
        }

        // ID único de 10 chars, letras y números
        String nuevaId = UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();

        Reserva reserva = new Reserva();
        reserva.setId(nuevaId);
        reserva.setFechaReserva(LocalDateTime.now());
        reserva.setEstado("CONFIRMADA");
        reserva.setUsuario(usuario);
        reserva.setActividad(actividad);
        return reservaRepository.save(reserva);
    }

    public Reserva crear(Reserva reserva) {
        long ocupadas = reservaRepository.countByActividad(reserva.getActividad());
        if (ocupadas >= reserva.getActividad().getPlazasTotal()) {
            throw new IllegalArgumentException("No hay plazas disponibles para esta actividad.");
        }
        reserva.setId(java.util.UUID.randomUUID().toString().substring(0, 10));
        reserva.setFechaReserva(java.time.LocalDateTime.now());
        reserva.setEstado("Confirmada");
        return reservaRepository.save(reserva);
    }

    public List<Reserva> obtenerPorUsuario(com.urbanactive.model.Usuario usuario) {
        return reservaRepository.findByUsuario(usuario);
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
        existente.setUsuario(cambios.getUsuario());
        existente.setActividad(cambios.getActividad());
        return reservaRepository.save(existente);
    }

    public void borrar(String id) {
        if (!reservaRepository.existsById(id)) {
            throw new IllegalArgumentException("Reserva no encontrada con id: " + id);
        }
        reservaRepository.deleteById(id);
    }
}
