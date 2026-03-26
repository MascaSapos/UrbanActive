package com.urbanactive.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "Actividad")
public class Actividad {

    @Id
    @Column(name = "Id", length = 10, nullable = false, unique = true)
    private String Id;

    @Column(name = "tipoDeporte", length = 50, nullable = false)
    private String tipoDeporte;

    @Column(name = "fechaHora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(name = "plazasTotal", nullable = false)
    private Integer plazasTotal;

    @ManyToOne
    @JoinColumn(name = "id_ubicacion", nullable = false)
    private Ubicacion id_ubicacion;

    public Actividad() {
    }

    public Actividad(String Id, String tipoDeporte, LocalDateTime fechaHora, Integer plazasTotal, Ubicacion id_ubicacion) {
        this.Id = Id;
        this.tipoDeporte = tipoDeporte;
        this.fechaHora = fechaHora;
        this.plazasTotal = plazasTotal;
        this.id_ubicacion = id_ubicacion;
    }

    public String getId() {
        return Id;
    }

    public void setId(String Id) {
        this.Id = Id;
    }

    public String getTipoDeporte() {
        return tipoDeporte;
    }

    public void setTipoDeporte(String tipoDeporte) {
        this.tipoDeporte = tipoDeporte;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public Integer getPlazasTotal() {
        return plazasTotal;
    }

    public void setPlazasTotal(Integer plazasTotal) {
        this.plazasTotal = plazasTotal;
    }

    public Ubicacion getId_ubicacion() {
        return id_ubicacion;
    }

    public void setId_ubicacion(Ubicacion id_ubicacion) {
        this.id_ubicacion = id_ubicacion;
    }
}
