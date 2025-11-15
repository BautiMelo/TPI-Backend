package com.backend.tpi.ms_solicitudes.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/publico/**").permitAll()
                .requestMatchers("/api/v1/public/**").permitAll()
                .requestMatchers("/protegido-administradores/**").hasRole("ADMIN")
                .requestMatchers("/protegido-usuarios/**").hasAnyRole("USUARIO", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/protegido-mixto/**").hasAnyRole("USUARIO", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/protegido-mixto/**").hasRole("ADMIN")
                .anyRequest().authenticated()
        ).oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt ->
                jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));
        return http.build();
    }

    @Bean
    Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter() {
        return new Converter<Jwt, AbstractAuthenticationToken>() {
            @Override
            public AbstractAuthenticationToken convert(Jwt jwt) {
                Map<String, Object> realmAccess = jwt.getClaim("realm_access");
                List<String> roles = Collections.emptyList();
                if (realmAccess != null && realmAccess.get("roles") instanceof List) {
                    roles = (List<String>) realmAccess.get("roles");
                }
                List<GrantedAuthority> authorities = roles.stream()
                        .map(r -> String.format("ROLE_%s", r.toUpperCase()))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());
                return new JwtAuthenticationToken(jwt, authorities);
            }
        };
    }
}
