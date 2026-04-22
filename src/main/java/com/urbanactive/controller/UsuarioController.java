package com.urbanactive.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.urbanactive.model.Usuario;
import com.urbanactive.service.UsuarioService;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final PasswordEncoder passwordEncoder;

    public UsuarioController(UsuarioService usuarioService, PasswordEncoder passwordEncoder) {
        this.usuarioService = usuarioService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/registro")
    public ResponseEntity<?> registrar(@RequestBody Usuario usuario) {
        if (usuarioService.existePorEmail(usuario.getEmail())) {
            return ResponseEntity.badRequest().body("El email ya está registrado");
        }
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        usuario.setId(java.util.UUID.randomUUID().toString().substring(0, 10));
        usuario.setRol(usuario.getRol().toUpperCase());
        usuarioService.crear(usuario);
        return ResponseEntity.ok("Usuario registrado exitosamente");
    }

    @GetMapping("/{id}")
    public Usuario buscarPorId(@PathVariable("id") String id) {
        return usuarioService.obtenerPorId(id);
    }

    @PutMapping("/perfil")
    public ResponseEntity<?> actualizarPerfil(@RequestBody java.util.Map<String, String> datos, org.springframework.security.core.Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).body("No autenticado");
        }
        String email = auth.getName();
        String nombre = datos.get("nombre");
        String fotoUrl = datos.get("fotoUrl");
        String descripcion = datos.get("descripcion");

        usuarioService.actualizarPerfil(email, nombre, fotoUrl, descripcion);
        return ResponseEntity.ok("Perfil actualizado correctamente");
    }
}
