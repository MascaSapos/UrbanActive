package com.urbanactive.dto;

import java.math.BigDecimal;

public class WeatherDto {
    private BigDecimal temperatura;
    private Integer probabilidadLluvia;
    private String emojiClima;
    private String textoClima;
    private Integer aqi;

    public WeatherDto() {}

    public WeatherDto(BigDecimal temperatura, Integer probabilidadLluvia, String emojiClima, String textoClima, Integer aqi) {
        this.temperatura = temperatura;
        this.probabilidadLluvia = probabilidadLluvia;
        this.emojiClima = emojiClima;
        this.textoClima = textoClima;
        this.aqi = aqi;
    }

    public String getTextoClima() {
        return textoClima;
    }

    public void setTextoClima(String textoClima) {
        this.textoClima = textoClima;
    }

    public BigDecimal getTemperatura() {
        return temperatura;
    }

    public void setTemperatura(BigDecimal temperatura) {
        this.temperatura = temperatura;
    }

    public Integer getProbabilidadLluvia() {
        return probabilidadLluvia;
    }

    public void setProbabilidadLluvia(Integer probabilidadLluvia) {
        this.probabilidadLluvia = probabilidadLluvia;
    }

    public String getEmojiClima() {
        return emojiClima;
    }

    public void setEmojiClima(String emojiClima) {
        this.emojiClima = emojiClima;
    }

    public Integer getAqi() {
        return aqi;
    }

    public void setAqi(Integer aqi) {
        this.aqi = aqi;
    }
}
