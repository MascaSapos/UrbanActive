package com.urbanactive.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "Usuario")
public class Usuario {

    @Id
    @Column(name = "Id", length = 10, nullable = false, unique = true)
    private String Id;

    @Column(name = "nombre", length = 255, nullable = false)
    private String nombre;

    @Column(name = "email", length = 255, nullable = false, unique = true)
    private String email;

    @Column(name = "rol", length = 255, nullable = false)
    private String rol;

    public Usuario() {
    }

    public Usuario(String Id, String nombre, String email, String rol) {
        this.Id = Id;
        this.nombre = nombre;
        this.email = email;
        this.rol = rol;
    }

    public String getId() {
        return Id;
    }

    public void setId(String Id) {
        this.Id = Id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }
}
