package com.urbanactive.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.urbanactive.model.Usuario;
import com.urbanactive.repository.UsuarioRepository;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Usuario crear(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    public Usuario obtenerPorId(String id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con id: " + id));
    }

    public List<Usuario> obtenerTodos() {
        return usuarioRepository.findAll();
    }

    public Usuario actualizar(String id, Usuario cambios) {
        Usuario existente = obtenerPorId(id);
        existente.setNombre(cambios.getNombre());
        existente.setEmail(cambios.getEmail());
        existente.setRol(cambios.getRol());
        return usuarioRepository.save(existente);
    }

    public void borrar(String id) {
        if (!usuarioRepository.existsById(id)) {
            throw new IllegalArgumentException("Usuario no encontrado con id: " + id);
        }
        usuarioRepository.deleteById(id);
    }
}
