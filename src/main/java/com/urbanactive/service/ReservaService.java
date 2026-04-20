package com.urbanactive.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.urbanactive.model.Actividad;
import com.urbanactive.model.Reserva;
import com.urbanactive.model.Usuario;
import com.urbanactive.model.Actividad;
import com.urbanactive.repository.ReservaRepository;
import com.urbanactive.repository.ActividadRepository;

@Service
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final UsuarioService usuarioService;
    private final ActividadService actividadService;
    private final ActividadRepository actividadRepository;

    public ReservaService(ReservaRepository reservaRepository,
                          UsuarioService usuarioService,
                          ActividadService actividadService,
                          ActividadRepository actividadRepository) {
        this.reservaRepository = reservaRepository;
        this.usuarioService = usuarioService;
        this.actividadService = actividadService;
        this.actividadRepository = actividadRepository;
    }

    // Crea una reserva para el usuario autenticado (identificado por email).
    public Reserva crearParaUsuario(String actividadId, String email) {
        Usuario usuario = usuarioService.obtenerPorEmail(email);
        Actividad actividad = actividadService.obtenerPorId(actividadId);

        long ocupadas = reservaRepository.countByActividad(actividad);
        if (ocupadas >= actividad.getPlazasTotal()) {
            throw new IllegalArgumentException("No hay plazas disponibles para esta actividad.");
        }

        boolean yaInscrito = reservaRepository.findByUsuario(usuario).stream()
                .anyMatch(r -> r.getActividad().getId().equals(actividadId) && !"CANCELADA".equals(r.getEstado()));
        if (yaInscrito) {
            throw new IllegalArgumentException("Ya tienes una plaza confirmada en esta actividad.");
        }

        // ID único de 10 chars, letras y números
        String nuevaId = UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();

        Reserva reserva = new Reserva();
        reserva.setId(nuevaId);
        reserva.setFechaReserva(LocalDateTime.now());
        reserva.setEstado("CONFIRMADA");
        reserva.setUsuario(usuario);
        reserva.setActividad(actividad);

        int plazasOcupadas = reservaRepository.countActivasPorActividad(actividadId);
        if (plazasOcupadas >= actividad.getPlazasTotal()) {
            throw new IllegalArgumentException("Aforo completo");
        }

        Reserva reservaGuardada = reservaRepository.save(reserva);

        if (plazasOcupadas + 1 >= actividad.getPlazasTotal()) {
            actividad.setEstado("CERRADA/COMPLETA");
            actividadRepository.save(actividad);
        }

        return reservaGuardada;
    }

    public Reserva crear(Reserva reserva) {
        Actividad actividad = reserva.getActividad();
        if (actividad == null) {
            throw new IllegalArgumentException("Actividad no encontrada");
        }

        int plazasOcupadas = reservaRepository.countActivasPorActividad(actividad.getId());
        if (plazasOcupadas >= actividad.getPlazasTotal()) {
            throw new IllegalArgumentException("Aforo completo");
        }

        reserva.setId(java.util.UUID.randomUUID().toString().substring(0, 10));
        reserva.setFechaReserva(java.time.LocalDateTime.now());
        reserva.setEstado("CONFIRMADA");
        
        Reserva reservaGuardada = reservaRepository.save(reserva);

        if (plazasOcupadas + 1 >= actividad.getPlazasTotal()) {
            actividad.setEstado("CERRADA/COMPLETA");
            actividadRepository.save(actividad);
        }

        return reservaGuardada;
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

    public void cancelarReserva(String reservaId) {
        Reserva reserva = obtenerPorId(reservaId);
        Actividad actividad = reserva.getActividad();
        if (actividad == null) {
            throw new IllegalArgumentException("Actividad no encontrada para la reserva");
        }

        LocalDateTime limiteCancelacion = actividad.getFechaHora().minusHours(24);
        if (LocalDateTime.now().isAfter(limiteCancelacion)) {
            throw new IllegalArgumentException("No se puede cancelar con menos de 24 horas de antelación");
        }

        reserva.setEstado("CANCELADA");
        reservaRepository.save(reserva);

        if ("CERRADA/COMPLETA".equals(actividad.getEstado())) {
            actividad.setEstado("ABIERTA");
            actividadRepository.save(actividad);
        }
    }

    public boolean estaUsuarioInscrito(String actividadId, String email) {
        if (email == null) return false;
        
        try {
            Usuario usuario = usuarioService.obtenerPorEmail(email);
            if (usuario == null) return false;
            
            return reservaRepository.findByUsuario(usuario).stream()
                    .anyMatch(r -> r.getActividad().getId().equals(actividadId) && !"CANCELADA".equals(r.getEstado()));
        } catch (Exception e) {
            return false;
        }
    }
}
