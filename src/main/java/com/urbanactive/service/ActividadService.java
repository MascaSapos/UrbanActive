package com.urbanactive.service;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.urbanactive.dto.ActividadMapDto;
import com.urbanactive.model.Actividad;
import com.urbanactive.model.InformeMeteorologico;
import com.urbanactive.model.Ubicacion;
import com.urbanactive.repository.ActividadRepository;
import com.urbanactive.repository.InformeMeteorologicoRepository;

@Service
public class ActividadService {

    private static final String ICON_DEFAULT = "\uD83D\uDCCD";

    private static final Map<String, String> ICON_POR_DEPORTE = Map.ofEntries(
            Map.entry("baloncesto", "\uD83C\uDFC0"),
            Map.entry("running", "\uD83C\uDFC3"),
            Map.entry("ciclismo", "\uD83D\uDEB4"));

    private final ActividadRepository actividadRepository;
    private final InformeMeteorologicoRepository informeMeteorologicoRepository;

    public ActividadService(ActividadRepository actividadRepository,
            InformeMeteorologicoRepository informeMeteorologicoRepository) {
        this.actividadRepository = actividadRepository;
        this.informeMeteorologicoRepository = informeMeteorologicoRepository;
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

    @Transactional(readOnly = true)
    public List<ActividadMapDto> obtenerParaMapa() {
        return actividadRepository.findAll().stream()
                .map(this::aMapDto)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private ActividadMapDto aMapDto(Actividad a) {
        Ubicacion u = a.getId_ubicacion();
        if (u == null || u.getLatitud() == null || u.getLongitud() == null) {
            return null;
        }
        String tipo = a.getTipoDeporte() != null ? a.getTipoDeporte() : "";
        String clave = tipo.trim().toLowerCase(Locale.ROOT);
        String icon = ICON_POR_DEPORTE.getOrDefault(clave, ICON_DEFAULT);
        String lugar = u.getNombre() != null && !u.getNombre().isBlank() ? u.getNombre() : u.getId();
        String title = tipo.isEmpty() ? lugar : tipo + " · " + lugar;
        ActividadMapDto dto = new ActividadMapDto();
        dto.setId(a.getId());
        dto.setLat(u.getLatitud().doubleValue());
        dto.setLng(u.getLongitud().doubleValue());
        dto.setIcon(icon);
        dto.setTitle(title);
        dto.setTipoDeporte(tipo);
        dto.setWeather(aWeatherDto(a.getId()));
        return dto;
    }

    private ActividadMapDto.WeatherDto aWeatherDto(String actividadId) {
        if (actividadId == null || actividadId.isBlank()) {
            return null;
        }
        Optional<InformeMeteorologico> optInforme = informeMeteorologicoRepository.findByActividadId(actividadId);
        if (optInforme.isEmpty()) {
            return null;
        }
        InformeMeteorologico informe = optInforme.get();
        ActividadMapDto.WeatherDto weather = new ActividadMapDto.WeatherDto();
        Integer lluvia = informe.getProbabilidadLluvia();
        weather.setLluvia(lluvia != null ? lluvia + "%" : "—");
        weather.setTemp(informe.getTemperatura() != null ? informe.getTemperatura() + "°C" : "—");
        weather.setAire(mapearCalidadAire(informe.getCalidadAire()));
        weather.setClima(mapearClima(lluvia));
        weather.setClimaIcon(iconoClima(lluvia));
        return weather;
    }

    private String mapearCalidadAire(Integer calidadAire) {
        if (calidadAire == null) {
            return "Sin datos";
        }
        if (calidadAire <= 50) {
            return "Buena";
        }
        if (calidadAire <= 100) {
            return "Moderada";
        }
        return "Mala";
    }

    private String mapearClima(Integer probabilidadLluvia) {
        if (probabilidadLluvia == null) {
            return "Sin datos";
        }
        if (probabilidadLluvia < 20) {
            return "Soleado";
        }
        if (probabilidadLluvia < 50) {
            return "Nublado";
        }
        return "Lluvioso";
    }

    private String iconoClima(Integer probabilidadLluvia) {
        if (probabilidadLluvia == null) {
            return "—";
        }
        if (probabilidadLluvia < 20) {
            return "☀";
        }
        if (probabilidadLluvia < 50) {
            return "⛅";
        }
        return "🌧";
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
