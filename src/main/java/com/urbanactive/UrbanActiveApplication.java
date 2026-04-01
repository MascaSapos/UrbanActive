package com.urbanactive;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class UrbanActiveApplication {

    public static void main(String[] args) {
        SpringApplication.run(UrbanActiveApplication.class, args);
    }

    @Bean
    public org.springframework.boot.CommandLineRunner initData(com.urbanactive.repository.UsuarioRepository usuarioRepo,
            org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
        return args -> {
            if (usuarioRepo.findByEmail("hola@test.com").isEmpty()) {
                com.urbanactive.model.Usuario deportista = new com.urbanactive.model.Usuario();
                deportista.setId("USR001");
                deportista.setNombre("Deportista Test");
                deportista.setEmail("hola@test.com");
                deportista.setRol("DEPORTISTA");
                deportista.setPassword(passwordEncoder.encode("123456"));
                usuarioRepo.save(deportista);
            }
            if (usuarioRepo.findByEmail("admin@test.com").isEmpty()) {
                com.urbanactive.model.Usuario org = new com.urbanactive.model.Usuario();
                org.setId("ORG001");
                org.setNombre("Organizador Test");
                org.setEmail("admin@test.com");
                org.setRol("ORGANIZADOR");
                org.setPassword(passwordEncoder.encode("123456"));
                usuarioRepo.save(org);
            }
        };
    }
}
