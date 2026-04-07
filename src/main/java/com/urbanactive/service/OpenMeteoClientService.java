package com.urbanactive.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.urbanactive.dto.WeatherDto;
import com.urbanactive.dto.openmeteo.OpenMeteoResponse;
import com.urbanactive.dto.openmeteo.OpenMeteoAirQualityResponse;

@Service
public class OpenMeteoClientService {

    private final RestClient restClient;
    
    @Value("${openmeteo.api.weather.url}")
    private String weatherApiUrl;

    @Value("${openmeteo.api.airquality.url}")
    private String airQualityApiUrl;

    public OpenMeteoClientService() {
        org.springframework.http.client.SimpleClientHttpRequestFactory factory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(2500);
        factory.setReadTimeout(3500);
        this.restClient = RestClient.builder().requestFactory(factory).build();
    }

    @Cacheable("weather")
    public WeatherDto getWeather(double lat, double lng, LocalDateTime fechaHora) {
        // Redondear lat y lng a 2 decimales para la clave de caché
        double roundedLat = Math.round(lat * 100.0) / 100.0;
        double roundedLng = Math.round(lng * 100.0) / 100.0;

        int weatherForecastDays = 14; 
        int weatherPastDays = 31;     
        
        OpenMeteoResponse weatherResponse = null;
        try {
            String weatherUrl = String.format(Locale.US, "%s/v1/forecast?latitude=%.4f&longitude=%.4f&hourly=temperature_2m,precipitation_probability,weather_code&timezone=Europe/Madrid&forecast_days=%d&past_days=%d",
                    weatherApiUrl, roundedLat, roundedLng, weatherForecastDays, weatherPastDays);
            
            weatherResponse = restClient.get()
                    .uri(weatherUrl)
                    .retrieve()
                    .body(OpenMeteoResponse.class);
        } catch (Exception e) {
            // Log o ignorar, devolveremos "Sin datos" después
        }

        int aqForecastDays = 5; // Air Quality gratuito sólo soporta hasta 5 días de predicción
        int aqPastDays = 31;
        
        OpenMeteoAirQualityResponse aqResponse = null;
        try {
            String airQualityUrl = String.format(Locale.US, "%s/v1/air-quality?latitude=%.4f&longitude=%.4f&hourly=european_aqi&timezone=Europe/Madrid&forecast_days=%d&past_days=%d",
                    airQualityApiUrl, roundedLat, roundedLng, aqForecastDays, aqPastDays);

            aqResponse = restClient.get()
                    .uri(airQualityUrl)
                    .retrieve()
                    .body(OpenMeteoAirQualityResponse.class);
        } catch (Exception e) {
            // Ignorar
        }

        String targetTimeStr = fechaHora.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:00"));
        
        int indexWeather = -1;
        if (weatherResponse != null && weatherResponse.getHourly() != null && weatherResponse.getHourly().getTime() != null) {
            List<String> times = weatherResponse.getHourly().getTime();
            for (int i = 0; i < times.size(); i++) {
                if (times.get(i).equals(targetTimeStr) || times.get(i).startsWith(targetTimeStr.substring(0, 13))) {
                    indexWeather = i;
                    break;
                }
            }
        }

        if (indexWeather == -1 || weatherResponse == null || weatherResponse.getHourly() == null) {
            return new WeatherDto(BigDecimal.ZERO, 0, "❓", "Desconocido", 0);
        }

        Double temp = weatherResponse.getHourly().getTemperature2m().get(indexWeather);
        Integer precip = weatherResponse.getHourly().getPrecipitationProbability().get(indexWeather);
        Integer weatherCode = weatherResponse.getHourly().getWeatherCode().get(indexWeather);
        
        // Buscar el índice específico para Air Quality (los tiempos pueden no coincidir en longitud)
        Integer aqi = 0;
        if (aqResponse != null && aqResponse.getHourly() != null && aqResponse.getHourly().getTime() != null) {
            List<String> timesAq = aqResponse.getHourly().getTime();
            int indexAq = -1;
            for (int i = 0; i < timesAq.size(); i++) {
                if (timesAq.get(i).startsWith(targetTimeStr.substring(0, 13))) {
                    indexAq = i;
                    break;
                }
            }
            if (indexAq != -1) {
                aqi = aqResponse.getHourly().getEuropeanAqi().get(indexAq);
                if (aqi == null) aqi = 0;
            }
        }

        String emoji = mapCodeToEmoji(weatherCode);
        String texto = mapCodeToText(weatherCode);

        return new WeatherDto(
                BigDecimal.valueOf(temp != null ? temp : 0.0),
                precip != null ? precip : 0,
                emoji,
                texto,
                aqi
        );
    }

    private String mapCodeToEmoji(Integer weatherCode) {
        if (weatherCode == null) return "🌡️";
        switch (weatherCode) {
            case 0: return "☀️"; // Clear sky
            case 1: case 2: return "⛅"; // Partly cloudy
            case 3: return "☁️"; // Overcast
            case 45: case 48: return "🌫️"; // Fog
            case 51: case 53: case 55: case 56: case 57: return "🌧️"; // Drizzle
            case 61: case 63: case 65: case 66: case 67: return "🌧️"; // Rain
            case 71: case 73: case 75: case 77: case 85: case 86: return "❄️"; // Snow
            case 80: case 81: case 82: return "🌦️"; // Showers
            case 95: case 96: case 99: return "⛈️"; // Thunderstorm
            default: return "🌡️";
        }
    }

    private String mapCodeToText(Integer weatherCode) {
        if (weatherCode == null) return "Desconocido";
        switch (weatherCode) {
            case 0: return "Soleado";
            case 1: case 2: return "Nubes y claros";
            case 3: return "Nublado";
            case 45: case 48: return "Niebla";
            case 51: case 53: case 55: case 56: case 57: return "Llovizna";
            case 61: case 63: case 65: case 66: case 67: return "Lluvia";
            case 71: case 73: case 75: case 77: case 85: case 86: return "Nieve";
            case 80: case 81: case 82: return "Chubascos";
            case 95: case 96: case 99: return "Tormenta";
            default: return "Desconocido";
        }
    }
}
