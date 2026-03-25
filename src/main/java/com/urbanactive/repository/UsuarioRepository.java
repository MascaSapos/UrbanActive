package com.urbanactive.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.urbanactive.model.Usuario;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, String> {
}
