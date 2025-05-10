package com.candiflow.api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtre JWT simplifié qui ne bloque aucune requête.
 * Cette version est utilisée lorsque l'authentification est désactivée.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // Version simplifiée qui ne fait aucune vérification d'authentification
        // et laisse passer toutes les requêtes
        log.debug("Authentification désactivée, passage de la requête: {}", request.getRequestURI());
        filterChain.doFilter(request, response);
    }
}
