package com.rs.gridservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * rs-grid-service'in kendi API'lerini (Keycloak'i wrap eden endpoint'leri) korur.
 *
 * Istekteki JWT, application.yml'deki
 * spring.security.oauth2.resourceserver.jwt.issuer-uri (Keycloak realm'i) ile dogrulanir.
 *
 * "rs-grid.security.enabled=false" yapilarak (sadece local gelistirme icin) kapatilabilir.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${rs-grid.security.enabled:true}")
    private boolean securityEnabled;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        if (securityEnabled) {
            http.authorizeHttpRequests(auth -> auth
                            .requestMatchers("/actuator/health/**", "/actuator/info").permitAll()
                            .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                            .requestMatchers("/api/v1/auth/**").permitAll()
                            .anyRequest().authenticated())
                    .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {}));
        } else {
            http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        }

        return http.build();
    }
}
