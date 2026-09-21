package com.rs.gridservice.config;

import com.rs.gridservice.dto.GroupResponse;
import com.rs.gridservice.dto.ProjectResponse;
import com.rs.gridservice.service.AuthService;
import com.rs.gridservice.service.EnvService;
import com.rs.gridservice.service.GroupService;
import com.rs.gridservice.service.ProjectService;
import com.rs.gridservice.service.ProjectTypeService;
import com.rs.gridservice.service.TechnologyService;
import com.rs.gridservice.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * SecurityConfig'in "rs-grid.security.enabled=true" (varsayilan) haldeki gercek davranisini
 * gercek bir HTTP sunucusuna karsi (RANDOM_PORT) dogrular: 401/403/200 durumlari ve
 * AccessDeniedHandler'in ürettigi JSON govde.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SecurityConfigEnabledIntegrationTest {

    @DynamicPropertySource
    static void h2Properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> "jdbc:h2:mem:security-enabled-test;DB_CLOSE_DELAY=-1");
        registry.add("spring.datasource.driver-class-name", () -> "org.h2.Driver");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private JwtDecoder jwtDecoder;
    @MockBean
    private AuthService authService;
    @MockBean
    private UserService userService;
    @MockBean
    private GroupService groupService;
    @MockBean
    private ProjectService projectService;
    @MockBean
    private TechnologyService technologyService;
    @MockBean
    private ProjectTypeService projectTypeService;
    @MockBean
    private EnvService envService;

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    private Jwt jwtWithRealmRoles(String token, List<String> roles) {
        return Jwt.withTokenValue(token)
                .header("alg", "none")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60))
                .claim("sub", "recepsivri")
                .claim("realm_access", Map.of("roles", roles))
                .build();
    }

    @Test
    void loginEndpointIsPubliclyAccessibleWithoutToken() {
        String body = "{\"username\":\"recepsivri\",\"password\":\"Rs78349401?\"}";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> response = restTemplate.postForEntity(url("/api/v1/auth/login"),
                new HttpEntity<>(body, headers), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void protectedEndpointWithoutTokenReturnsUnauthorized() {
        ResponseEntity<String> response = restTemplate.getForEntity(url("/api/v1/users"), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void protectedEndpointWithValidTokenAndNoSpecialRoleIsAuthorized() {
        when(userService.getAllUsers(0, 50, null)).thenReturn(List.of());
        when(jwtDecoder.decode("plain-user-token")).thenReturn(jwtWithRealmRoles("plain-user-token", List.of("offline_access")));

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("plain-user-token");

        ResponseEntity<String> response = restTemplate.exchange(url("/api/v1/users"), HttpMethod.GET,
                new HttpEntity<>(headers), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void groupsEndpointWithoutAdminRoleReturnsForbiddenWithApiErrorBody() {
        when(jwtDecoder.decode("non-admin-token")).thenReturn(jwtWithRealmRoles("non-admin-token", List.of("offline_access")));

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("non-admin-token");

        ResponseEntity<String> response = restTemplate.exchange(url("/api/v1/groups"), HttpMethod.GET,
                new HttpEntity<>(headers), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).contains("\"status\":403").contains("admin rolu gerekli");
    }

    @Test
    void groupsEndpointWithAdminRoleIsAuthorized() {
        when(groupService.getAllGroups(0, 50, null)).thenReturn(List.of(GroupResponse.builder().id("g-1").build()));
        when(jwtDecoder.decode("admin-token")).thenReturn(jwtWithRealmRoles("admin-token", List.of("admin")));

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("admin-token");

        ResponseEntity<String> response = restTemplate.exchange(url("/api/v1/groups"), HttpMethod.GET,
                new HttpEntity<>(headers), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void projectsReadWithoutAdminRoleIsAuthorized() {
        when(projectService.getAllProjects(0, 50, null)).thenReturn(List.of());
        when(jwtDecoder.decode("plain-user-token")).thenReturn(jwtWithRealmRoles("plain-user-token", List.of("offline_access")));

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("plain-user-token");

        ResponseEntity<String> response = restTemplate.exchange(url("/api/v1/projects"), HttpMethod.GET,
                new HttpEntity<>(headers), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void projectsWriteWithoutAdminRoleReturnsForbidden() {
        when(jwtDecoder.decode("non-admin-token")).thenReturn(jwtWithRealmRoles("non-admin-token", List.of("offline_access")));

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("non-admin-token");
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> response = restTemplate.exchange(url("/api/v1/projects"), HttpMethod.POST,
                new HttpEntity<>("{}", headers), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).contains("\"status\":403").contains("admin rolu gerekli");
    }

    @Test
    void projectsWriteWithAdminRoleIsAuthorized() {
        ProjectResponse created = ProjectResponse.builder().id("project-1").name("Grid Dashboard")
                .userId("user-1").technology("React").type("web").build();
        when(projectService.addProject(any())).thenReturn(created);
        when(jwtDecoder.decode("admin-token")).thenReturn(jwtWithRealmRoles("admin-token", List.of("admin")));

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("admin-token");
        headers.setContentType(MediaType.APPLICATION_JSON);
        String body = "{\"name\":\"Grid Dashboard\",\"userId\":\"user-1\",\"technology\":\"React\",\"type\":\"web\"}";

        ResponseEntity<String> response = restTemplate.exchange(url("/api/v1/projects"), HttpMethod.POST,
                new HttpEntity<>(body, headers), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void technologiesWriteWithoutAdminRoleReturnsForbidden() {
        when(jwtDecoder.decode("non-admin-token")).thenReturn(jwtWithRealmRoles("non-admin-token", List.of("offline_access")));

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("non-admin-token");
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> response = restTemplate.exchange(url("/api/v1/technologies"), HttpMethod.POST,
                new HttpEntity<>("{}", headers), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).contains("\"status\":403").contains("admin rolu gerekli");
    }

    @Test
    void projectTypesWriteWithoutAdminRoleReturnsForbidden() {
        when(jwtDecoder.decode("non-admin-token")).thenReturn(jwtWithRealmRoles("non-admin-token", List.of("offline_access")));

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("non-admin-token");
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> response = restTemplate.exchange(url("/api/v1/project-types"), HttpMethod.POST,
                new HttpEntity<>("{}", headers), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).contains("\"status\":403").contains("admin rolu gerekli");
    }

    @Test
    void envsReadWithoutAdminRoleIsAuthorized() {
        when(envService.getAllEnvs(0, 50, null)).thenReturn(List.of());
        when(jwtDecoder.decode("plain-user-token")).thenReturn(jwtWithRealmRoles("plain-user-token", List.of("offline_access")));

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("plain-user-token");

        ResponseEntity<String> response = restTemplate.exchange(url("/api/v1/envs"), HttpMethod.GET,
                new HttpEntity<>(headers), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void envsWriteWithoutAdminRoleReturnsForbidden() {
        when(jwtDecoder.decode("non-admin-token")).thenReturn(jwtWithRealmRoles("non-admin-token", List.of("offline_access")));

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("non-admin-token");
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> response = restTemplate.exchange(url("/api/v1/envs"), HttpMethod.POST,
                new HttpEntity<>("{}", headers), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).contains("\"status\":403").contains("admin rolu gerekli");
    }

    @Test
    void envsWriteWithAdminRoleIsAuthorized() {
        com.rs.gridservice.dto.EnvResponse created = com.rs.gridservice.dto.EnvResponse.builder()
                .id("env-1").name("Local Dev").url("http://localhost:5500").userId("user-1").build();
        when(envService.addEnv(any())).thenReturn(created);
        when(jwtDecoder.decode("admin-token")).thenReturn(jwtWithRealmRoles("admin-token", List.of("admin")));

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("admin-token");
        headers.setContentType(MediaType.APPLICATION_JSON);
        String body = "{\"name\":\"Local Dev\",\"url\":\"http://localhost:5500\",\"userId\":\"user-1\"}";

        ResponseEntity<String> response = restTemplate.exchange(url("/api/v1/envs"), HttpMethod.POST,
                new HttpEntity<>(body, headers), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }
}
