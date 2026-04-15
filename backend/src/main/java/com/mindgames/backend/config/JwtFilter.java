package com.mindgames.backend.config;
import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
                                    throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            if (jwtUtil.validarToken(token)) {
                String email = jwtUtil.extraerEmail(token);

                // 🔥 1. Extraemos el rol usando el nuevo método del util
                String rol = jwtUtil.extraerRol(token);

                // 🔐 2. Creamos la autoridad (Spring requiere el prefijo ROLE_)
                SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + rol);

                // Creamos un objeto de autenticación que Spring entienda
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        email, null, List.of(authority)); // 👈 Ya no es una lista vacía

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 🔑 ¡ESTO ES LO MÁS IMPORTANTE!
                // Le decimos a Spring: "Este usuario es legal, dale acceso"
                SecurityContextHolder.getContext().setAuthentication(authToken);
                    
                    // aquí puedes cargar usuario si quieres después
                    System.out.println("Usuario autenticado: " + email + " con rol: " + rol);
                }
            }

        filterChain.doFilter(request, response);
    }
}
