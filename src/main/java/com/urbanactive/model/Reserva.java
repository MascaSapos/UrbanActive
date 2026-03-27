package com.urbanactive.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "Reserva")
public class Reserva {

    @Id
    @Column(name = "Id", length = 10, nullable = false, unique = true)
    private String Id;

    @Column(name = "fechaReserva", nullable = false)
    private LocalDateTime fechaReserva;

    @Column(name = "estado", length = 20, nullable = false)
    private String estado;

    @Column(name = "id_usuario", length = 4, nullable = false)
    private String id_usuario;

    @Column(name = "id_actividad", length = 4, nullable = false)
    private String id_actividad;

    public Reserva() {
    }

    public Reserva(String Id, LocalDateTime fechaReserva, String estado, String id_usuario, String id_actividad) {
        this.Id = Id;
        this.fechaReserva = fechaReserva;
        this.estado = estado;
        this.id_usuario = id_usuario;
        this.id_actividad = id_actividad;
    }

    public String getId() {
        return Id;
    }

    public void setId(String Id) {
        this.Id = Id;
    }

    public LocalDateTime getFechaReserva() {
        return fechaReserva;
    }

    public void setFechaReserva(LocalDateTime fechaReserva) {
        this.fechaReserva = fechaReserva;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getId_usuario() {
        return id_usuario;
    }

    public void setId_usuario(String id_usuario) {
        this.id_usuario = id_usuario;
    }

    public String getId_actividad() {
        return id_actividad;
    }

    public void setId_actividad(String id_actividad) {
        this.id_actividad = id_actividad;
    }
}
