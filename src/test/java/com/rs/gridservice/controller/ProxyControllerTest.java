package com.rs.gridservice.controller;

import com.rs.gridservice.dto.ProxyRequest;
import com.rs.gridservice.dto.ProxyResponse;
import com.rs.gridservice.exception.ProxyException;
import com.rs.gridservice.proxy.OutboundProxyService;
import com.rs.gridservice.proxy.ProxySettings;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProxyControllerTest {

    private HttpServer server;
    private String base;
    private ProxyController controller;

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        server.createContext("/ok", exchange -> reply(exchange, 200, "{\"ad\":\"Ali\"}"));
        server.createContext("/teapot", exchange -> reply(exchange, 418, "demlik"));
        server.createContext("/odd", exchange -> reply(exchange, 299, "tuhaf"));
        server.start();
        base = "http://127.0.0.1:" + server.getAddress().getPort();
        OutboundProxyService service = new OutboundProxyService(ProxySettings.of(true, List.of(), 2000, 5000, 1000, 1000));
        controller = new ProxyController(service);
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    private static void reply(com.sun.net.httpserver.HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
        exchange.close();
    }

    @Test
    void forwardReturnsTargetStatusReasonHeadersAndBody() {
        ResponseEntity<ProxyResponse> response = controller.forward(new ProxyRequest("GET", base + "/ok", Map.of(), null));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        ProxyResponse body = response.getBody();
        assertEquals(200, body.status());
        assertEquals("OK", body.statusText());
        assertEquals("{\"ad\":\"Ali\"}", body.body());
        assertTrue(body.headers().stream().anyMatch(h -> h.key().equalsIgnoreCase("Content-Type") && h.value().equals("application/json")));
        assertTrue(body.elapsedMs() >= 0);
    }

    @Test
    void forwardPassesThroughTargetClientErrorsAsNormalResponses() {
        ProxyResponse body = controller.forward(new ProxyRequest("GET", base + "/teapot", null, null)).getBody();

        assertEquals(418, body.status());
        assertEquals("demlik", body.body());
    }

    @Test
    void forwardUsesEmptyStatusTextForUnknownStatusCodes() {
        ProxyResponse body = controller.forward(new ProxyRequest("GET", base + "/odd", null, null)).getBody();

        assertEquals(299, body.status());
        assertEquals("", body.statusText());
    }

    @Test
    void forwardPropagatesProxyRejections() {
        ProxyException ex = assertThrows(ProxyException.class,
                () -> controller.forward(new ProxyRequest("GET", "http://169.254.169.254/latest/meta-data/", null, null)));

        assertEquals(403, ex.getStatus());
    }
}
