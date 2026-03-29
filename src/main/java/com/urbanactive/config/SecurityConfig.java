package com.urbanactive.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

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
                                "/assets/**")
                        .permitAll()
                        // El resto debe estar autenticado
                        .anyRequest().authenticated())
                // Configuración del login propio
                .formLogin(form -> form
                        .loginPage("/login")
                        .usernameParameter("email")
                        .defaultSuccessUrl("/", true)
                        .permitAll())
                // Configuración de salir
                .logout(logout -> logout.permitAll());

        return http.build();
    }

    // Usuario de prueba
    @Bean
    public org.springframework.security.core.userdetails.UserDetailsService userDetailsService() {
        org.springframework.security.core.userdetails.UserDetails user = org.springframework.security.core.userdetails.User
                .withDefaultPasswordEncoder()
                .username("hola@test.com")
                .password("123456")
                .roles("USER")
                .build();

        return new org.springframework.security.provisioning.InMemoryUserDetailsManager(user);
    }
}
