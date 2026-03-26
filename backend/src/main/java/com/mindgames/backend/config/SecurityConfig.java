package com.mindgames.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        // 1. Activar CORS con la configuración por defecto o la que definiremos
        .cors(Customizer.withDefaults()) 
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth
            .anyRequest().permitAll() // Por ahora permitimos todo para pruebas
        )
        .httpBasic(Customizer.withDefaults()); // Permite Basic Auth si lo usas
    
    return http.build();
}
}
