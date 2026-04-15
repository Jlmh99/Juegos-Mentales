package com.mindgames.backend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
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
                
                // 4. NUEVO: Usuarios ahora requiere TOKEN (authenticated)
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
        config.setAllowedOrigins(java.util.List.of("http://localhost:4200"));
        config.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        // Permitir explícitamente Authorization
        config.setAllowedHeaders(java.util.List.of("Authorization", "Content-Type", "Accept"));
        config.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder encoder) {
        // Aquí creamos el usuario "user" con la contraseña "admin123" encriptada
        UserDetails user = User.builder()
                .username("user")
                .password(encoder.encode("admin321")) // 👈 IMPORTANTE: Se encripta aquí
                .roles("USER")
                .build();

        return new InMemoryUserDetailsManager(user);
    }

    
    //BEan de encriptacion de contraseñas
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
