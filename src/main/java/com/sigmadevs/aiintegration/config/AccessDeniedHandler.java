package com.sigmadevs.aiintegration.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class AccessDeniedHandler implements org.springframework.security.web.access.AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        log.warn("Access denied: {}", accessDeniedException.getMessage()+ " to "+request.getRequestURI());
        if (accessDeniedException instanceof CsrfException) {
            response.getWriter().write("Invalid or missing CSRF token");
        }else {
            response.getWriter().write("Access denied: "+accessDeniedException.getMessage());
        }
        response.setStatus(403);
    }
}
