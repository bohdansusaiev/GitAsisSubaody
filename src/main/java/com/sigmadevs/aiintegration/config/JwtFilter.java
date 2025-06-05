package com.sigmadevs.aiintegration.config;


import com.sigmadevs.aiintegration.entity.User;
import com.sigmadevs.aiintegration.exception.NotFoundException;
import com.sigmadevs.aiintegration.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.Principal;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String jwtToken = null;

        Cookie cookie = Arrays.stream(Optional.ofNullable(request.getCookies()).orElse(new Cookie[]{}))
                .filter(cookieElement -> "accessToken".toLowerCase(Locale.ROOT).equals(cookieElement.getName().toLowerCase(Locale.ROOT)))
                .findFirst().orElse(null);
        jwtToken = cookie==null?null:cookie.getValue();

        if(jwtToken==null){
            String bearerToken = request.getHeader("Authorization");
            if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
                jwtToken = bearerToken.substring(7);
            }
        }

        if (jwtToken == null) {
            log.debug("no accessToken");
            filterChain.doFilter(request, response);
            return;
        }
        if (!jwtToken.isBlank() && jwtUtil.validateToken(jwtToken)) {
            String username = jwtUtil.getUsername(jwtToken);
            User user;
            try {
                user = userService.getUserByUsername(username);
            } catch (NotFoundException e) {
                log.debug("Token contains a non-existent user");
                filterChain.doFilter(request, response);
                return;
            }
            if (user != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(new Principal() {
                    @Override
                    public String getName() {
                        return user.getUsername();
                    }
                },
                        null,
                        user.getAuthorities()
                );
                SecurityContextHolder.getContext().setAuthentication(token);
            }

        } else {
            log.debug("Token value is blank");
            filterChain.doFilter(request, response);
            return;
        }
        filterChain.doFilter(request, response);
        return;
    }

}