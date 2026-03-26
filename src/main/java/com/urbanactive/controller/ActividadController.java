package com.urbanactive.controller;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.urbanactive.model.Actividad;
import com.urbanactive.service.ActividadService;

@Controller
public class ActividadController {

    private final ActividadService actividadService;

    public ActividadController(ActividadService actividadService) {
        this.actividadService = actividadService;
    }

    @GetMapping("/leaflet")
    public String verActividadesLeaflet(Model model) {
        model.addAttribute("actividades", actividadService.obtenerTodas());
        return "leaflet";
    }

    @GetMapping("/api/actividades/map")
    @ResponseBody
    public List<Map<String, Object>> obtenerActividadesMapa() {
        return actividadService.obtenerTodas().stream()
                .map(this::toMapItem)
                .toList();
    }

    private Map<String, Object> toMapItem(Actividad actividad) {
        String tipo = actividad.getTipoDeporte() != null ? actividad.getTipoDeporte() : "Actividad";
        String nombreSitio = actividad.getId_ubicacion() != null && actividad.getId_ubicacion().getNombre() != null
                ? actividad.getId_ubicacion().getNombre()
                : "Ubicacion sin nombre";
        BigDecimal latitud = actividad.getId_ubicacion() != null ? actividad.getId_ubicacion().getLatitud() : null;
        BigDecimal longitud = actividad.getId_ubicacion() != null ? actividad.getId_ubicacion().getLongitud() : null;

        Map<String, Object> weather = new LinkedHashMap<>();
        weather.put("clima", "No disponible");
        weather.put("climaIcon", "🌤️");
        weather.put("temp", "N/D");
        weather.put("lluvia", "N/D");
        weather.put("aire", "N/D");

        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", actividad.getId() != null ? actividad.getId() : "");
        item.put("lat", latitud != null ? latitud.doubleValue() : 0.0);
        item.put("lng", longitud != null ? longitud.doubleValue() : 0.0);
        item.put("title", tipo);
        item.put("desc", "Ubicacion: " + nombreSitio);
        item.put("icon", getIconoPorTipo(tipo));
        item.put("badge", tipo);
        item.put("weather", weather);
        return item;
    }

    private String getIconoPorTipo(String tipo) {
        String t = tipo.toLowerCase();
        if (t.contains("running")) {
            return "🏃";
        }
        if (t.contains("cicl")) {
            return "🚴";
        }
        if (t.contains("baloncesto") || t.contains("basket")) {
            return "🏀";
        }
        return "📍";
    }
}

