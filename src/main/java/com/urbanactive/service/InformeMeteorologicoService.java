package com.urbanactive.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.urbanactive.model.InformeMeteorologico;
import com.urbanactive.repository.InformeMeteorologicoRepository;

@Service
public class InformeMeteorologicoService {

    private final InformeMeteorologicoRepository informeMeteorologicoRepository;

    public InformeMeteorologicoService(InformeMeteorologicoRepository informeMeteorologicoRepository) {
        this.informeMeteorologicoRepository = informeMeteorologicoRepository;
    }

    public InformeMeteorologico crear(InformeMeteorologico informe) {
        return informeMeteorologicoRepository.save(informe);
    }

    public InformeMeteorologico obtenerPorId(String id) {
        return informeMeteorologicoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("InformeMeteorologico no encontrado con id: " + id));
    }

    public List<InformeMeteorologico> obtenerTodos() {
        return informeMeteorologicoRepository.findAll();
    }

    public InformeMeteorologico actualizar(String id, InformeMeteorologico cambios) {
        InformeMeteorologico existente = obtenerPorId(id);
        existente.setTemperatura(cambios.getTemperatura());
        existente.setCalidadAire(cambios.getCalidadAire());
        existente.setFechaDatos(cambios.getFechaDatos());
        existente.setId_ubicacion(cambios.getId_ubicacion());
        return informeMeteorologicoRepository.save(existente);
    }

    public void borrar(String id) {
        if (!informeMeteorologicoRepository.existsById(id)) {
            throw new IllegalArgumentException("InformeMeteorologico no encontrado con id: " + id);
        }
        informeMeteorologicoRepository.deleteById(id);
    }
}
