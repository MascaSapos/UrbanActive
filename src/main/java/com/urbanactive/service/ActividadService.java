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
import com.urbanactive.repository.ReservaRepository;

@Service
public class ActividadService {

    private static final String ICON_DEFAULT = "\uD83D\uDCCD";

    private static final Map<String, String> ICON_POR_DEPORTE = Map.ofEntries(
            Map.entry("baloncesto", "\uD83C\uDFC0"),
            Map.entry("running", "\uD83C\uDFC3"),
            Map.entry("ciclismo", "\uD83D\uDEB4"));

    private final ActividadRepository actividadRepository;
    private final InformeMeteorologicoRepository informeMeteorologicoRepository;
    private final OpenMeteoClientService openMeteoClientService;
    private final ReservaRepository reservaRepository;

    public ActividadService(ActividadRepository actividadRepository,
            InformeMeteorologicoRepository informeMeteorologicoRepository,
            OpenMeteoClientService openMeteoClientService,
            ReservaRepository reservaRepository) {
        this.actividadRepository = actividadRepository;
        this.informeMeteorologicoRepository = informeMeteorologicoRepository;
        this.openMeteoClientService = openMeteoClientService;
        this.reservaRepository = reservaRepository;
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
        dto.setWeather(aWeatherDto(a)); // Pasa todo el objeto Actividad en vez del Id
        dto.setPlazasTotal(a.getPlazasTotal());
        dto.setPlazasOcupadas(reservaRepository.countActivasPorActividad(a.getId()));
        dto.setEstado(a.getEstado());
        return dto;
    }

    public ActividadMapDto.WeatherDto aWeatherDto(Actividad a) {
        if (a == null || a.getId() == null) {
            return null;
        }

        Optional<InformeMeteorologico> optInforme = informeMeteorologicoRepository.findByActividadId(a.getId());

        if (optInforme.isPresent()) {
            // Caso BBDD
            InformeMeteorologico informe = optInforme.get();
            ActividadMapDto.WeatherDto weather = new ActividadMapDto.WeatherDto();
            Integer lluvia = informe.getProbabilidadLluvia();
            weather.setLluvia(lluvia != null ? lluvia + "%" : "—");
            weather.setTemp(informe.getTemperatura() != null ? informe.getTemperatura() + "°C" : "—");
            weather.setAire(mapearCalidadAire(informe.getCalidadAire()));
            weather.setClima(mapearClima(lluvia));
            weather.setClimaIcon(iconoClima(lluvia));

            // Validador de Advertencia (Caso BBDD)
            boolean advT = informe.getTemperatura() != null
                    && informe.getTemperatura().compareTo(new java.math.BigDecimal("5")) < 0;
            boolean advAqi = informe.getCalidadAire() != null && informe.getCalidadAire() >= 60;
            if (advT || advAqi) {
                weather.setAlerta("⚠️ Condiciones adversas");
            }

            return weather;
        } else {
            // Caso API Tiempo Real (Fallback dinámico y con caché!)
            Ubicacion u = a.getId_ubicacion();
            if (u == null || u.getLatitud() == null || u.getLongitud() == null || a.getFechaHora() == null) {
                return null;
            }
            try {
                // Buffer to respect Open-Meteo's limit (1000 requests/hour, burst limits
                // heavily restricted under multiple queries)
                Thread.sleep(700);

                com.urbanactive.dto.WeatherDto live = openMeteoClientService.getWeather(
                        u.getLatitud().doubleValue(), u.getLongitud().doubleValue(), a.getFechaHora());

                System.out.println("DEBUG WEATHER - Actividad: " + a.getId() + " - Temp: " + live.getTemperatura());

                ActividadMapDto.WeatherDto weather = new ActividadMapDto.WeatherDto();
                weather.setTemp(live.getTemperatura() + "°C");
                weather.setLluvia(live.getProbabilidadLluvia() + "%");
                weather.setAire(mapearCalidadAire(live.getAqi()));
                weather.setClimaIcon(live.getEmojiClima());
                weather.setClima(live.getTextoClima());

                // Validador de Advertencia
                boolean advT = live.getTemperatura() != null
                        && live.getTemperatura().compareTo(new java.math.BigDecimal("5")) < 0;
                boolean advAqi = live.getAqi() != null && live.getAqi() >= 60;
                if (advT || advAqi) {
                    weather.setAlerta("⚠️ Condiciones adversas");
                }

                return weather;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            } catch (Exception e) {
                return null;
            }
        }
    }

    private String mapearCalidadAire(Integer calidadAire) {
        if (calidadAire == null || calidadAire == 0) {
            return "Sin datos";
        }
        if (calidadAire <= 40) {
            return "Buena";
        }
        if (calidadAire <= 60) {
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
