package com.mindgames.backend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {
     @Autowired
    private JwtFilter jwtFilter;


        @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 1. EL ORDEN IMPORTA: CORS debe ir antes que cualquier otra cosa
            .cors(Customizer.withDefaults()) 
            .csrf(csrf -> csrf.disable())

            .authorizeHttpRequests(auth -> auth
                // 2. Mantenemos permitir OPTIONS (Preflight) para evitar errores de CORS en Angular
                .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                
                // 3. Rutas de Auth siempre libres
                .requestMatchers("/api/auth/**").permitAll()

                // 3.1 Formulario de Ayuda y Soporte del footer: público (con o sin sesión)
                .requestMatchers("/api/soporte/**").permitAll()

                // 4. NUEVO: Cada usuario autenticado puede ver/editar SU PROPIO perfil
                //    (debe ir ANTES que la regla de ADMIN de abajo, más específica primero)
                //    Se incluye la ruta exacta "/api/users/me" porque "/**" no siempre
                //    cubre la ruta base sin segmento adicional.
                .requestMatchers("/api/users/me", "/api/users/me/**").authenticated()

                // 5. El resto de /api/users requiere rol ADMIN (gestión de todos los usuarios)
                .requestMatchers("/api/users/**").hasRole("ADMIN") // Solo usuarios con rol ADMIN podrán entrar aquí
                
                // 5. El resto de la app protegida
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }

    // 3. ASEGÚRATE DE TENER ESTE BEAN EN LA MISMA CLASE
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(java.util.List.of("http://localhost:4200",
            "https://mind-games-frontend.onrender.com"
        ));
        config.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        // Permitir explícitamente Authorization
        config.setAllowedHeaders(java.util.List.of("Authorization", "Content-Type", "Accept"));
        config.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /**
     * La autenticación real es con JWT (JwtFilter) contra la tabla de usuarios.
     * Este bean existe solo para que Spring Boot NO cree su usuario por defecto
     * (que imprimiría una contraseña generada en el log). Está vacío a propósito:
     * no hay usuarios en memoria ni contraseñas escritas en el código.
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return new InMemoryUserDetailsManager();
    }

    
    //BEan de encriptacion de contraseñas
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
