package com.rs.gridservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rs.gridservice.exception.ApiError;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * rs-grid-service'in kendi API'lerini (Keycloak'i wrap eden endpoint'leri) korur.
 *
 * Istekteki JWT, application.yml'deki
 * spring.security.oauth2.resourceserver.jwt.issuer-uri (Keycloak realm'i) ile dogrulanir.
 *
 * JWT'nin "realm_access.roles" claim'i {@link KeycloakRealmRoleConverter} ile Spring Security
 * authority'lerine ("ROLE_<rol>") cevrilir.
 *
 * Yetkilendirme kurallari:
 * - "/api/v1/groups/**", kullanici olusturma/guncelleme/silme (POST/PUT/DELETE "/api/v1/users/**")
 *   ve proje tipi/teknoloji'nin TUM metodlari (okuma dahil, "/api/v1/project-types/**",
 *   "/api/v1/technologies/**") sadece Keycloak'ta "admin" realm rolune sahip kullanicilar
 *   tarafindan cagrilabilir -- bunlar referans/lookup verisi, normal kullanicinin gormesine gerek yok.
 *   Istisna: teknoloji listeleme (GET "/api/v1/technologies", getAll) "desktop_user" rolune de acik
 *   -- teknoloji tek getirme/ekleme/guncelleme/silme yine sadece admin'e ozel.
 * - Proje, env ve page ("/api/v1/projects/**", "/api/v1/envs/**", "/api/v1/pages/**"): TUM metodlar
 *   (okuma dahil) sadece "admin" ya da "desktop_user" realm rolune sahip kullanicilar tarafindan
 *   cagrilabilir. Ayrica sahiplik servis katmaninda userId eslesmesiyle zorunlu kilinir (bkz.
 *   JpaProjectServiceImpl/JpaEnvServiceImpl/JpaPageServiceImpl), yani bu rollerden birine sahip bir
 *   kullanici bile sadece kendi kaydini duzenleyebilir/silebilir.
 * - Kullanici okuma (GET "/api/v1/users/**") gecerli bir JWT'ye sahip herkese acik.
 * - Yetkisiz erisimlerde 403 doner.
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
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        if (securityEnabled) {
            http.authorizeHttpRequests(auth -> auth
                            .requestMatchers("/actuator/health/**", "/actuator/info").permitAll()
                            .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                            .requestMatchers("/api/v1/auth/**").permitAll()
                            .requestMatchers("/api/v1/groups/**").hasRole("admin")
                            // Kullanici olusturma/guncelleme/silme sadece admin; okuma (GET) herkese acik.
                            .requestMatchers(HttpMethod.POST, "/api/v1/users").hasRole("admin")
                            .requestMatchers(HttpMethod.PUT, "/api/v1/users/**").hasRole("admin")
                            .requestMatchers(HttpMethod.DELETE, "/api/v1/users/**").hasRole("admin")
                            // Proje tipi/teknoloji: tamamen admin'e ozel (okuma dahil) -- referans/lookup
                            // verisi, normal kullanicinin gormesine gerek yok.
                            .requestMatchers(HttpMethod.GET, "/api/v1/project-types").hasAnyRole("admin", "desktop_user")
                            .requestMatchers("/api/v1/project-types", "/api/v1/project-types/**").hasRole("admin")
                            // Teknoloji listeleme (getAll) istisnasi: desktop_user de cagirabilir; digerleri (tek
                            // getirme, ekleme, guncelleme, silme) admin'e ozel kalir. Daha spesifik oldugu icin
                            // genel "/api/v1/technologies/**" kuralindan ONCE tanimlanmali.
                            .requestMatchers(HttpMethod.GET, "/api/v1/technologies").hasAnyRole("admin", "desktop_user")
                            .requestMatchers("/api/v1/technologies", "/api/v1/technologies/**").hasRole("admin")
                            // Proje/env/page: admin veya desktop_user rolune sahip kullanicilar TUM CRUD
                            // islemlerini yapabilir; sahiplik kontrolu (userId eslesmesi) ayrica servis
                            // katmaninda uygulanir (bkz. JpaProjectServiceImpl/JpaEnvServiceImpl/JpaPageServiceImpl).
                            .requestMatchers("/api/v1/projects", "/api/v1/projects/**", "/api/v1/envs", "/api/v1/envs/**",
                                    "/api/v1/pages", "/api/v1/pages/**")
                            .hasAnyRole("admin", "desktop_user")
                            .anyRequest().authenticated())
                    .oauth2ResourceServer(oauth2 -> oauth2
                            .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                            .accessDeniedHandler(accessDeniedHandler(objectMapper)));
        } else {
            http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        }

        return http.build();
    }

    /**
     * Local gelistirme icin permissif CORS ayari: tarayicidan (file:// ile acilan bir HTML sayfasi
     * ya da herhangi bir localhost portundan servis edilen bir frontend) dogrudan bu API'ye
     * istek atilabilmesini saglar. Prod'da mutlaka spesifik origin'lerle degistirilmeli.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
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
                    "Bu islemi yapmaya yetkiniz yok");
            objectMapper.writeValue(response.getWriter(), body);
        };
    }
}
