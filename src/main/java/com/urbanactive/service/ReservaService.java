package com.urbanactive.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.urbanactive.model.Reserva;
import com.urbanactive.model.Usuario;
import com.urbanactive.repository.ReservaRepository;

@Service
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final UsuarioService usuarioService;

    public ReservaService(ReservaRepository reservaRepository, UsuarioService usuarioService) {
        this.reservaRepository = reservaRepository;
        this.usuarioService = usuarioService;
    }

    /** Crea una reserva para el usuario autenticado (identificado por email). */
    public Reserva crearParaUsuario(String actividadId, String email) {
        Usuario usuario = usuarioService.obtenerPorEmail(email);

        // ID único de 10 chars, letras y números
        String nuevaId = UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();

        Reserva reserva = new Reserva();
        reserva.setId(nuevaId);
        reserva.setFechaReserva(LocalDateTime.now());
        reserva.setEstado("CONFIRMADA");
        reserva.setId_usuario(usuario.getId());
        reserva.setId_actividad(actividadId);
        return reservaRepository.save(reserva);
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
