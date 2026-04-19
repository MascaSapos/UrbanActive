package com.urbanactive.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Autorizaciones de URL
                .authorizeHttpRequests(authz -> authz
                        // Permite archivos estáticos sin contraseña
                        .requestMatchers("/css/**", "/js/**", "/img/**", "/login", "/styles.css", "/script.js",
                                "/assets/**", "/api/usuarios/registro", "/api/actividades/testmap")
                        .permitAll()
                        .requestMatchers("/actividades/nueva").hasRole("ORGANIZADOR")
                        .requestMatchers("/perfil-organizador").hasRole("ORGANIZADOR")
                        .requestMatchers("/mis-reservas", "/actividades/*/reservar").hasAnyRole("DEPORTISTA", "USER")
                        // El resto debe estar autenticado
                        .anyRequest().authenticated())
                // Configuración del login propio
                .formLogin(form -> form
                        .loginPage("/login")
                        .usernameParameter("email")
                        .failureUrl("/login?error=true")
                        .defaultSuccessUrl("/", true)
                        .permitAll())
                // Configuración de salir
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
