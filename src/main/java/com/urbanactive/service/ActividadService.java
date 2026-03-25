package com.urbanactive.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.urbanactive.model.Actividad;
import com.urbanactive.repository.ActividadRepository;

@Service
public class ActividadService {

    private final ActividadRepository actividadRepository;

    public ActividadService(ActividadRepository actividadRepository) {
        this.actividadRepository = actividadRepository;
    }

    public Actividad crear(Actividad actividad) {
        return actividadRepository.save(actividad);
    }

    public Actividad obtenerPorId(String id) {
        return actividadRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Actividad no encontrada con id: " + id));
    }

    public List<Actividad> obtenerTodas() {
        return actividadRepository.findAll();
    }

    public Actividad actualizar(String id, Actividad cambios) {
        Actividad existente = obtenerPorId(id);
        existente.setTipoDeporte(cambios.getTipoDeporte());
        existente.setFechaHora(cambios.getFechaHora());
        existente.setPlazasTotal(cambios.getPlazasTotal());
        existente.setId_ubicacion(cambios.getId_ubicacion());
        return actividadRepository.save(existente);
    }

    public void borrar(String id) {
        if (!actividadRepository.existsById(id)) {
            throw new IllegalArgumentException("Actividad no encontrada con id: " + id);
        }
        actividadRepository.deleteById(id);
    }
}
