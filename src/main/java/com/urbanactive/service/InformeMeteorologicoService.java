package com.urbanactive.service;

import java.util.List;
import java.time.LocalDateTime;
import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.urbanactive.model.InformeMeteorologico;
import com.urbanactive.model.Ubicacion;
import com.urbanactive.repository.InformeMeteorologicoRepository;
import com.urbanactive.dto.WeatherDto;

@Service
public class InformeMeteorologicoService {

    private static final Logger logger = LoggerFactory.getLogger(InformeMeteorologicoService.class);

    private final InformeMeteorologicoRepository informeMeteorologicoRepository;
    private final OpenMeteoClientService openMeteoClientService;

    public InformeMeteorologicoService(InformeMeteorologicoRepository informeMeteorologicoRepository, 
                                       OpenMeteoClientService openMeteoClientService) {
        this.informeMeteorologicoRepository = informeMeteorologicoRepository;
        this.openMeteoClientService = openMeteoClientService;
    }

    public InformeMeteorologico crear(InformeMeteorologico informe) {
        return informeMeteorologicoRepository.save(informe);
    }

    // FASE 2 y 3: Orquestar llamada API, Validar y Guardar en BD
    public InformeMeteorologico generarYGuardarInforme(String idInforme, Ubicacion ubicacion, String idActividad, LocalDateTime fechaHora) {
        // 1. Procesar JSON de la API (Fase 2) usando el cliente REST dedicado
        WeatherDto dto = openMeteoClientService.getWeather(
                ubicacion.getLatitud().doubleValue(), 
                ubicacion.getLongitud().doubleValue(), 
                fechaHora
        );

        // 2. Validador meteorológico (Fase 3): Mostrar advertencia
        boolean advertenciaT = dto.getTemperatura().compareTo(new BigDecimal("5")) < 0;
        boolean advertenciaAqi = dto.getAqi() >= 60; // AQI Europeo: a partir de 60 se considera "mala" (poor)
        
        if (advertenciaT) {
            logger.warn("⚠️ ADVERTENCIA METEOROLÓGICA: La temperatura prevista es muy baja ({}ºC) para la actividad {}.", dto.getTemperatura(), idActividad);
        }
        
        if (advertenciaAqi) {
            logger.warn("⚠️ ADVERTENCIA METEOROLÓGICA: La calidad del aire prevista es MALA o peor (AQI Nivel {}) para la actividad {}.", dto.getAqi(), idActividad);
        }

        // 3. Crear y guardar en BD
        InformeMeteorologico informe = new InformeMeteorologico(
                idInforme,
                dto.getTemperatura(),
                dto.getAqi(),
                idActividad,
                dto.getProbabilidadLluvia(),
                LocalDateTime.now(), // Ultima actualizacion
                fechaHora,           // Fecha a la que hace referencia el dato
                ubicacion
        );

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
        existente.setId_actividad(cambios.getId_actividad());
        existente.setProbabilidadLluvia(cambios.getProbabilidadLluvia());
        existente.setUltimaActualizacion(cambios.getUltimaActualizacion());
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
