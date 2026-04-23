package com.urbanactive.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;


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
            Map.entry("ciclismo", "\uD83D\uDEB4"),
            Map.entry("futbol", "\u26bd"),
            Map.entry("natacion", "\ud83c\udfca\u200d\u2642\ufe0f"),
            Map.entry("yoga", "\ud83e\uddd8\u200d\u2640\ufe0f"));
            

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

    public Actividad obtenerConDetalles(String id) {
        Actividad actividad = obtenerPorId(id);
        actividad.setPlazasOcupadas(reservaRepository.countActivasPorActividad(id));
        return actividad;
    }

    public List<Actividad> obtenerTodas() {
        return actividadRepository.findAll();
    }

    public List<Actividad> obtenerPorOrganizador(String email) {
        return actividadRepository.findByOrganizadorEmailIncludeNulls(email);
    }

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
        dto.setTipoEspacio(u.getTipoEspacio());
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
            weather.setAlerta(verificarCondicionesAdversas(informe.getTemperatura(), informe.getCalidadAire()));

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

                ActividadMapDto.WeatherDto weather = new ActividadMapDto.WeatherDto();
                weather.setTemp(live.getTemperatura() + "°C");
                weather.setLluvia(live.getProbabilidadLluvia() + "%");
                weather.setAire(mapearCalidadAire(live.getAqi()));
                weather.setClimaIcon(live.getEmojiClima());
                weather.setClima(live.getTextoClima());

                // Validador de Advertencia
                weather.setAlerta(verificarCondicionesAdversas(live.getTemperatura(), live.getAqi()));

                return weather;
            } catch (Exception e) {
                return null;
            }
        }
    }

    private String verificarCondicionesAdversas(java.math.BigDecimal temp, Integer aqi) {
        boolean advT = temp != null && temp.compareTo(new java.math.BigDecimal("5")) < 0;
        boolean advAqi = aqi != null && aqi >= 60;
        return (advT || advAqi) ? "⚠️ Condiciones adversas" : null;
    }

    /**
     * Unificado: Comprueba si una actividad tiene alertas (clima, aforo, estado)
     */
    public String obtenerMensajeAlerta(Actividad a) {
        if (a == null) return null;
        
        // 1. Clima
        ActividadMapDto.WeatherDto w = aWeatherDto(a);
        if (w != null && w.getAlerta() != null) {
            return w.getAlerta();
        }

        // 2. Aforo
        int ocupadas = reservaRepository.countActivasPorActividad(a.getId());
        if (a.getPlazasTotal() != null && ocupadas >= a.getPlazasTotal() && !"CERRADA".equalsIgnoreCase(a.getEstado()) && !"COMPLETA".equalsIgnoreCase(a.getEstado())) {
            return "⚠️ Aforo completo";
        }

        // 3. Estado (si está cancelada/cerrada pero no por aforo)
        if ("CANCELADA".equalsIgnoreCase(a.getEstado()) || "CERRADA".equalsIgnoreCase(a.getEstado()) || "COMPLETA".equalsIgnoreCase(a.getEstado())) {
            return "🚫 Actividad no disponible";
        }

        return null;
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

    @org.springframework.transaction.annotation.Transactional
    public void borrar(String id) {
        if (!actividadRepository.existsById(id)) {
            throw new IllegalArgumentException("Actividad no encontrada con id: " + id);
        }
        
        // 1. Borrar informe meteorológico (si existe)
        informeMeteorologicoRepository.deleteByActividadId(id);
        
        // 2. Borrar reservas asociadas (FK child)
        reservaRepository.deleteAllByActividadId(id);
        
        // 3. Borrar la actividad
        actividadRepository.deleteById(id);
    }

    /**
     * Marca una actividad como CANCELADA e inicia el contador de 7 días.
     */
    public void cancelar(String id) {
        Actividad a = obtenerPorId(id);
        if (a != null) {
            a.setEstado("CANCELADA");
            a.setFechaCancelacion(LocalDateTime.now());
            actividadRepository.save(a);
        }
    }

    /**
     * Limpieza al arrancar: Borra físicamente las actividades canceladas hace más de 7 días.
     * Se ejecuta una sola vez cuando la aplicación está lista (al "entrar").
     */
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.context.event.EventListener(org.springframework.boot.context.event.ApplicationReadyEvent.class)
    public void limpiarActividadesCanceladasAlArrancar() {
        LocalDateTime limite = LocalDateTime.now().minusDays(7);
        List<Actividad> todas = actividadRepository.findAll();
        boolean huboBorrados = false;
        for (Actividad a : todas) {
            if ("CANCELADA".equalsIgnoreCase(a.getEstado()) 
                && a.getFechaCancelacion() != null 
                && a.getFechaCancelacion().isBefore(limite)) {
                borrar(a.getId());
                huboBorrados = true;
            }
        }
        if (huboBorrados) {
            System.out.println(">>> [UrbanActive] Limpieza de actividades canceladas completada con éxito.");
        }
    }
}
