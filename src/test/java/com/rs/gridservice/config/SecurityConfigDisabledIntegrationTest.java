package com.rs.gridservice.config;

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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * SecurityConfig'in "rs-grid.security.enabled=false" haldeki davranisini (else branch:
 * anyRequest().permitAll()) dogrular - token olmadan da korumali endpoint'lere erisilebilmeli.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "rs-grid.security.enabled=false")
class SecurityConfigDisabledIntegrationTest {

    @DynamicPropertySource
    static void h2Properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> "jdbc:h2:mem:security-disabled-test;DB_CLOSE_DELAY=-1");
        registry.add("spring.datasource.driver-class-name", () -> "org.h2.Driver");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

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

    @Test
    void protectedEndpointWithoutTokenIsAllowedWhenSecurityDisabled() {
        when(userService.getAllUsers(0, 50, null)).thenReturn(List.of());

        ResponseEntity<String> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/api/v1/users", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
