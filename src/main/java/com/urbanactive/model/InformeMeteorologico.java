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

    @Column(name = "fechaDatos", nullable = false)
    private LocalDateTime fechaDatos;

    @Column(name = "id_ubicacion", length = 25, nullable = false)
    private String id_ubicacion;

    public InformeMeteorologico() {
    }

    public InformeMeteorologico(String Id, BigDecimal temperatura, Integer calidadAire, LocalDateTime fechaDatos,
            String id_ubicacion) {
        this.Id = Id;
        this.temperatura = temperatura;
        this.calidadAire = calidadAire;
        this.fechaDatos = fechaDatos;
        this.id_ubicacion = id_ubicacion;
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

    public LocalDateTime getFechaDatos() {
        return fechaDatos;
    }

    public void setFechaDatos(LocalDateTime fechaDatos) {
        this.fechaDatos = fechaDatos;
    }

    public String getId_ubicacion() {
        return id_ubicacion;
    }

    public void setId_ubicacion(String id_ubicacion) {
        this.id_ubicacion = id_ubicacion;
    }
}
