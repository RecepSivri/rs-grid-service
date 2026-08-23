package com.rs.gridservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rs.gridservice.exception.ApiError;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

/**
 * rs-grid-service'in kendi API'lerini (Keycloak'i wrap eden endpoint'leri) korur.
 *
 * Istekteki JWT, application.yml'deki
 * spring.security.oauth2.resourceserver.jwt.issuer-uri (Keycloak realm'i) ile dogrulanir.
 *
 * JWT'nin "realm_access.roles" claim'i {@link KeycloakRealmRoleConverter} ile Spring Security
 * authority'lerine ("ROLE_<rol>") cevrilir; grup API'leri ("/api/v1/groups/**") sadece Keycloak'ta
 * "admin" realm rolune sahip kullanicilar tarafindan cagrilabilir, digerlerine 403 doner.
 *
 * "rs-grid.security.enabled=false" yapilarak (sadece local gelistirme icin) kapatilabilir.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${rs-grid.security.enabled:true}")
    private boolean securityEnabled;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, ObjectMapper objectMapper) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        if (securityEnabled) {
            http.authorizeHttpRequests(auth -> auth
                            .requestMatchers("/actuator/health/**", "/actuator/info").permitAll()
                            .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                            .requestMatchers("/api/v1/auth/**").permitAll()
                            .requestMatchers("/api/v1/groups/**").hasRole("admin")
                            .anyRequest().authenticated())
                    .oauth2ResourceServer(oauth2 -> oauth2
                            .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                            .accessDeniedHandler(accessDeniedHandler(objectMapper)));
        } else {
            http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        }

        return http.build();
    }

    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new KeycloakRealmRoleConverter());
        return converter;
    }

    /** Yetkisiz (403) durumlarda da diger API hatalariyla ayni JSON formatinda (ApiError) yanit doner. */
    private AccessDeniedHandler accessDeniedHandler(ObjectMapper objectMapper) {
        return (request, response, ex) -> {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            ApiError body = new ApiError(HttpServletResponse.SC_FORBIDDEN, "Forbidden",
                    "Bu islemi yapmaya yetkiniz yok (admin rolu gerekli)");
            objectMapper.writeValue(response.getWriter(), body);
        };
    }
}
