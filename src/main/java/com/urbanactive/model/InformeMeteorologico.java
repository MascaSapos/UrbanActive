package com.urbanactive.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "InformeMeteorologico")
public class InformeMeteorologico {

    @Id
    @Column(name = "Id", length = 10, nullable = false, unique = true)
    private String Id;

    @Column(name = "temperatura", nullable = false)
    private BigDecimal temperatura;

    @Column(name = "calidadAire", nullable = false)
    private Integer calidadAire;

    @Column(name = "id_actividad", length = 10, nullable = false, unique = true)
    private String id_actividad;

    @Column(name = "probabilidadLluvia", nullable = false)
    private Integer probabilidadLluvia;

    @Column(name = "ultimaActualizacion", nullable = false)
    private LocalDateTime ultimaActualizacion;

    public InformeMeteorologico() {
    }

    public InformeMeteorologico(String Id, BigDecimal temperatura, Integer calidadAire, String id_actividad,
            Integer probabilidadLluvia, LocalDateTime ultimaActualizacion) {
        this.Id = Id;
        this.temperatura = temperatura;
        this.calidadAire = calidadAire;
        this.id_actividad = id_actividad;
        this.probabilidadLluvia = probabilidadLluvia;
        this.ultimaActualizacion = ultimaActualizacion;
    }

    public String getId() {
        return Id;
    }

    public void setId(String Id) {
        this.Id = Id;
    }

    public BigDecimal getTemperatura() {
        return temperatura;
    }

    public void setTemperatura(BigDecimal temperatura) {
        this.temperatura = temperatura;
    }

    public Integer getCalidadAire() {
        return calidadAire;
    }

    public void setCalidadAire(Integer calidadAire) {
        this.calidadAire = calidadAire;
    }

    public String getId_actividad() {
        return id_actividad;
    }

    public void setId_actividad(String id_actividad) {
        this.id_actividad = id_actividad;
    }

    public Integer getProbabilidadLluvia() {
        return probabilidadLluvia;
    }

    public void setProbabilidadLluvia(Integer probabilidadLluvia) {
        this.probabilidadLluvia = probabilidadLluvia;
    }

    public LocalDateTime getUltimaActualizacion() {
        return ultimaActualizacion;
    }

    public void setUltimaActualizacion(LocalDateTime ultimaActualizacion) {
        this.ultimaActualizacion = ultimaActualizacion;
    }
}
