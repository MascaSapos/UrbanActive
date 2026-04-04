package com.urbanactive.dto.openmeteo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenMeteoResponse {
    
    @JsonProperty("hourly")
    private Hourly hourly;

    public Hourly getHourly() { return hourly; }
    public void setHourly(Hourly hourly) { this.hourly = hourly; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Hourly {
        private List<String> time;
        @JsonProperty("temperature_2m")
        private List<Double> temperature2m;
        @JsonProperty("precipitation_probability")
        private List<Integer> precipitationProbability;
        @JsonProperty("weather_code")
        private List<Integer> weatherCode;
        
        public List<String> getTime() { return time; }
        public void setTime(List<String> time) { this.time = time; }
        
        public List<Double> getTemperature2m() { return temperature2m; }
        public void setTemperature2m(List<Double> temperature2m) { this.temperature2m = temperature2m; }
        
        public List<Integer> getPrecipitationProbability() { return precipitationProbability; }
        public void setPrecipitationProbability(List<Integer> precipitationProbability) { this.precipitationProbability = precipitationProbability; }
        
        public List<Integer> getWeatherCode() { return weatherCode; }
        public void setWeatherCode(List<Integer> weatherCode) { this.weatherCode = weatherCode; }
    }
}
