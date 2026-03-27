package com.urbanactive.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.urbanactive.model.Ubicacion;
import com.urbanactive.repository.UbicacionRepository;

@Service
public class UbicacionService {

    private final UbicacionRepository ubicacionRepository;

    public UbicacionService(UbicacionRepository ubicacionRepository) {
        this.ubicacionRepository = ubicacionRepository;
    }

    public Ubicacion crear(Ubicacion ubicacion) {
        return ubicacionRepository.save(ubicacion);
    }

    public Ubicacion obtenerPorId(String id) {
        return ubicacionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ubicacion no encontrada con id: " + id));
    }

    public List<Ubicacion> obtenerTodos() {
        return ubicacionRepository.findAll();
    }

    public Ubicacion actualizar(String id, Ubicacion cambios) {
        Ubicacion existente = obtenerPorId(id);
        existente.setNombre(cambios.getNombre());
        existente.setTipoEspacio(cambios.getTipoEspacio());
        existente.setLatitud(cambios.getLatitud());
        existente.setLongitud(cambios.getLongitud());
        return ubicacionRepository.save(existente);
    }

    public void borrar(String id) {
        if (!ubicacionRepository.existsById(id)) {
            throw new IllegalArgumentException("Ubicacion no encontrada con id: " + id);
        }
        ubicacionRepository.deleteById(id);
    }
}

