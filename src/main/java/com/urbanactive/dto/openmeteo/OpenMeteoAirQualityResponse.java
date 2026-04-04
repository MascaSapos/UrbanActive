package com.urbanactive.dto.openmeteo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenMeteoAirQualityResponse {
    
    @JsonProperty("hourly")
    private Hourly hourly;

    public Hourly getHourly() { return hourly; }
    public void setHourly(Hourly hourly) { this.hourly = hourly; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Hourly {
        private List<String> time;
        @JsonProperty("european_aqi")
        private List<Integer> europeanAqi;

        public List<String> getTime() { return time; }
        public void setTime(List<String> time) { this.time = time; }
        
        public List<Integer> getEuropeanAqi() { return europeanAqi; }
        public void setEuropeanAqi(List<Integer> europeanAqi) { this.europeanAqi = europeanAqi; }
    }
}
