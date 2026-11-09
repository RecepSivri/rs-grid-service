package com.rs.gridservice.proxy;

import com.rs.gridservice.exception.ProxyException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OutboundProxyServiceTest {

    private HttpServer server;
    private String base;

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        server.setExecutor(Executors.newCachedThreadPool());
        server.createContext("/echo", this::echo);
        server.createContext("/gzip", ex -> respond(ex, 200, "text/plain; charset=utf-8", gzip("merhaba dünya"), "Content-Encoding", "gzip"));
        server.createContext("/deflate", ex -> respond(ex, 200, "text/plain", deflate("selam"), "Content-Encoding", "deflate"));
        server.createContext("/badgzip", ex -> respond(ex, 200, "text/plain", "bu gzip degil".getBytes(StandardCharsets.UTF_8), "Content-Encoding", "gzip"));
        server.createContext("/bomb", ex -> respond(ex, 200, "text/plain", gzip("0".repeat(5000)), "Content-Encoding", "gzip"));
        server.createContext("/big", ex -> respond(ex, 200, "text/plain", "x".repeat(600).getBytes(StandardCharsets.UTF_8)));
        server.createContext("/redirect", ex -> respond(ex, 302, "text/plain", new byte[0], "Location", "http://127.0.0.1:1/ic-ag"));
        server.createContext("/latin1", ex -> respond(ex, 200, "text/plain; charset=ISO-8859-1", new byte[]{'k', (byte) 0xE7, 'a'}));
        server.createContext("/cookies", ex -> {
            ex.getResponseHeaders().add("Set-Cookie", "a=1");
            ex.getResponseHeaders().add("Set-Cookie", "b=2");
            respond(ex, 204, null, new byte[0]);
        });
        server.createContext("/slow", ex -> {
            sleep(1500);
            respond(ex, 200, "text/plain", "gec".getBytes(StandardCharsets.UTF_8));
        });
        server.createContext("/drip", ex -> {
            ex.sendResponseHeaders(200, 100);
            ex.getResponseBody().write("0123456789".getBytes(StandardCharsets.UTF_8));
            ex.getResponseBody().flush();
            sleep(1500);
            ex.close();
        });
        server.start();
        base = "http://127.0.0.1:" + server.getAddress().getPort();
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    private static OutboundProxyService service(boolean allowPrivate, int maxRequest, int maxResponse, long readTimeoutMs) {
        return new OutboundProxyService(ProxySettings.of(allowPrivate, List.of(), 2000, readTimeoutMs, maxRequest, maxResponse));
    }

    private static OutboundProxyService service() {
        return service(true, 1000, 1000, 5000);
    }

    private void echo(HttpExchange ex) throws IOException {
        String requestBody = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        String text = "method=" + ex.getRequestMethod()
                + "\nquery=" + ex.getRequestURI().getRawQuery()
                + "\nbody=" + requestBody
                + "\nx-test=" + ex.getRequestHeaders().getFirst("X-Test")
                + "\nx-empty=[" + ex.getRequestHeaders().getFirst("X-Empty") + "]"
                + "\nhost=" + ex.getRequestHeaders().getFirst("Host")
                + "\naccept-encoding=" + ex.getRequestHeaders().getFirst("Accept-Encoding");
        respond(ex, 201, "text/plain; charset=utf-8", text.getBytes(StandardCharsets.UTF_8), "X-Reply", "yes");
    }

    private static void respond(HttpExchange ex, int status, String contentType, byte[] body, String... extraHeaders) throws IOException {
        if (contentType != null) {
            ex.getResponseHeaders().add("Content-Type", contentType);
        }
        for (int i = 0; i < extraHeaders.length; i += 2) {
            ex.getResponseHeaders().add(extraHeaders[i], extraHeaders[i + 1]);
        }
        if (body.length == 0) {
            ex.sendResponseHeaders(status, -1);
        } else {
            ex.sendResponseHeaders(status, body.length);
            try (OutputStream out = ex.getResponseBody()) {
                out.write(body);
            }
        }
        ex.close();
    }

    private static byte[] gzip(String text) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (GZIPOutputStream out = new GZIPOutputStream(bytes)) {
            out.write(text.getBytes(StandardCharsets.UTF_8));
        }
        return bytes.toByteArray();
    }

    private static byte[] deflate(String text) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DeflaterOutputStream out = new DeflaterOutputStream(bytes, new Deflater())) {
            out.write(text.getBytes(StandardCharsets.UTF_8));
        }
        return bytes.toByteArray();
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

    private static String header(ProxyResult result, String name) {
        return result.headers().stream().filter(h -> h.key().equalsIgnoreCase(name)).map(ProxyResult.Header::value).findFirst().orElse(null);
    }

    // ---- basari yolu ----

    @Test
    void forwardsMethodQueryBodyAndCustomHeadersAndReturnsStatusHeadersAndBody() {
        Map<String, String> headers = new HashMap<>();
        headers.put("X-Test", "merhaba");
        headers.put("X-Empty", null);

        ProxyResult result = service().execute("post", base + "/echo?a=1&b=%C3%A7", headers, "{\"ad\":\"Çağrı\"}");

        assertEquals(201, result.status());
        assertTrue(result.body().contains("method=POST"));
        assertTrue(result.body().contains("query=a=1&b=%C3%A7"));
        assertTrue(result.body().contains("body={\"ad\":\"Çağrı\"}"));
        assertTrue(result.body().contains("x-test=merhaba"));
        assertTrue(result.body().contains("x-empty=[]"));
        assertEquals("yes", header(result, "X-Reply"));
        assertTrue(result.elapsedMs() >= 0);
    }

    @Test
    void dropsTransportHeadersTheClientMustNotControl() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Host", "evil.example");
        headers.put("Content-Length", "1");
        headers.put("Connection", "upgrade");
        headers.put("Accept-Encoding", "br");

        ProxyResult result = service().execute("GET", base + "/echo", headers, null);

        assertEquals(201, result.status());
        assertTrue(result.body().contains("host=127.0.0.1:"));
        assertTrue(result.body().contains("accept-encoding=gzip, deflate"));
    }

    @Test
    void getAndHeadNeverSendABodyAndAbsentBodyOrHeadersAreFine() {
        ProxyResult get = service().execute("GET", base + "/echo", null, "yok sayilir");
        assertTrue(get.body().contains("body=\n"));

        ProxyResult post = service().execute("POST", base + "/echo", Map.of(), null);
        assertTrue(post.body().contains("method=POST"));

        ProxyResult head = service().execute("HEAD", base + "/echo", Map.of(), "yok sayilir");
        assertEquals(201, head.status());
        assertEquals("", head.body());
    }

    @Test
    void returnsEveryRepeatedResponseHeader() {
        ProxyResult result = service().execute("GET", base + "/cookies", null, null);

        assertEquals(204, result.status());
        assertEquals(2, result.headers().stream().filter(h -> h.key().equalsIgnoreCase("Set-Cookie")).count());
    }

    @Test
    void doesNotFollowRedirects() {
        ProxyResult result = service().execute("GET", base + "/redirect", null, null);

        assertEquals(302, result.status());
        assertEquals("http://127.0.0.1:1/ic-ag", header(result, "Location"));
    }

    @Test
    void decodesGzipAndDeflateBodies() {
        assertEquals("merhaba dünya", service().execute("GET", base + "/gzip", null, null).body());
        assertEquals("selam", service().execute("GET", base + "/deflate", null, null).body());
    }

    @Test
    void decodesBodyWithDeclaredCharsetAndFallsBackToUtf8() {
        assertEquals("kça", service().execute("GET", base + "/latin1", null, null).body());
        assertEquals(StandardCharsets.UTF_8, OutboundProxyService.charsetOf(""));
        assertEquals(StandardCharsets.UTF_8, OutboundProxyService.charsetOf("text/plain"));
        assertEquals(StandardCharsets.UTF_8, OutboundProxyService.charsetOf("text/plain; charset=nope-xyz"));
        assertEquals(Charset.forName("ISO-8859-9"), OutboundProxyService.charsetOf("text/plain; charset=\"ISO-8859-9\""));
    }

    // ---- reddedilen istekler ----

    @Test
    void rejectsUnsupportedMethods() {
        assertEquals(400, assertThrows(ProxyException.class, () -> service().execute(null, base + "/echo", null, null)).getStatus());
        assertEquals(400, assertThrows(ProxyException.class, () -> service().execute("TRACE", base + "/echo", null, null)).getStatus());
    }

    @Test
    void rejectsOversizedRequestBodyMeasuredInBytes() {
        OutboundProxyService small = service(true, 10, 1000, 5000);
        assertEquals(413, assertThrows(ProxyException.class, () -> small.execute("POST", base + "/echo", null, "çççççç")).getStatus());
    }

    @Test
    void rejectsBlankAndInvalidHeaders() {
        Map<String, String> blankName = new HashMap<>();
        blankName.put("  ", "x");
        assertEquals(400, assertThrows(ProxyException.class, () -> service().execute("GET", base + "/echo", blankName, null)).getStatus());

        Map<String, String> nullName = new HashMap<>();
        nullName.put(null, "x");
        assertEquals(400, assertThrows(ProxyException.class, () -> service().execute("GET", base + "/echo", nullName, null)).getStatus());

        ProxyException badName = assertThrows(ProxyException.class, () -> service().execute("GET", base + "/echo", Map.of("bad name", "x"), null));
        assertEquals(400, badName.getStatus());
        assertTrue(badName.getMessage().contains("bad name"));

        assertEquals(400, assertThrows(ProxyException.class, () -> service().execute("GET", base + "/echo", Map.of("X-Test", "a\r\nInjected: 1"), null)).getStatus());
    }

    @Test
    void rejectsBlockedTargetsBeforeAnyConnection() {
        OutboundProxyService strict = service(false, 1000, 1000, 5000);

        assertEquals(403, assertThrows(ProxyException.class, () -> strict.execute("GET", base + "/echo", null, null)).getStatus());
        assertEquals(403, assertThrows(ProxyException.class, () -> strict.execute("GET", "http://169.254.169.254/latest/meta-data/", null, null)).getStatus());
        assertEquals(400, assertThrows(ProxyException.class, () -> strict.execute("GET", "file:///etc/passwd", null, null)).getStatus());
    }

    // ---- boyut / sure / baglanti hatalari ----

    @Test
    void rejectsResponsesLargerThanTheLimitWithoutBufferingThem() {
        OutboundProxyService small = service(true, 1000, 500, 5000);

        assertEquals(413, assertThrows(ProxyException.class, () -> small.execute("GET", base + "/big", null, null)).getStatus());
    }

    @Test
    void rejectsDecompressionBombs() {
        OutboundProxyService small = service(true, 1000, 500, 5000);

        ProxyException ex = assertThrows(ProxyException.class, () -> small.execute("GET", base + "/bomb", null, null));
        assertEquals(413, ex.getStatus());
    }

    @Test
    void reportsUndecodableCompressedBodyAsBadGateway() {
        ProxyException ex = assertThrows(ProxyException.class, () -> service().execute("GET", base + "/badgzip", null, null));
        assertEquals(502, ex.getStatus());
        assertTrue(ex.getMessage().contains("gzip"));
    }

    @Test
    void timesOutWhenHeadersNeverArrive() {
        OutboundProxyService impatient = service(true, 1000, 1000, 300);

        assertEquals(504, assertThrows(ProxyException.class, () -> impatient.execute("GET", base + "/slow", null, null)).getStatus());
    }

    @Test
    void timesOutWhenTheBodyDripsTooSlowly() {
        OutboundProxyService impatient = service(true, 1000, 1000, 400);

        assertEquals(504, assertThrows(ProxyException.class, () -> impatient.execute("GET", base + "/drip", null, null)).getStatus());
    }

    @Test
    void reportsConnectionRefusedAsBadGateway() throws IOException {
        int closedPort;
        try (ServerSocket socket = new ServerSocket(0, 1, InetAddress.getLoopbackAddress())) {
            closedPort = socket.getLocalPort();
        }
        String url = "http://127.0.0.1:" + closedPort + "/";

        ProxyException ex = assertThrows(ProxyException.class, () -> service().execute("GET", url, null, null));
        assertEquals(502, ex.getStatus());
        assertEquals("Hedef sunucuya baglanilamadi", ex.getMessage());
    }

    @Test
    void reportsConnectionDroppedMidBodyAsBadGateway() throws Exception {
        try (ServerSocket socket = new ServerSocket(0, 1, InetAddress.getLoopbackAddress())) {
            Thread dropper = new Thread(() -> {
                try (Socket client = socket.accept()) {
                    client.getInputStream().read(new byte[2048]);
                    client.getOutputStream().write("HTTP/1.1 200 OK\r\nContent-Length: 100\r\n\r\nabc".getBytes(StandardCharsets.UTF_8));
                    client.getOutputStream().flush();
                } catch (IOException ignored) {
                    // istemci tarafi sonucu test ediyor
                }
            });
            dropper.start();
            String url = "http://127.0.0.1:" + socket.getLocalPort() + "/";

            ProxyException ex = assertThrows(ProxyException.class, () -> service().execute("GET", url, null, null));
            assertEquals(502, ex.getStatus());
            dropper.join();
        }
    }

    @Test
    void interruptedCallerGetsBadGatewayAndKeepsItsInterruptFlag() {
        Thread.currentThread().interrupt();
        try {
            ProxyException ex = assertThrows(ProxyException.class, () -> service().execute("GET", base + "/slow", null, null));
            assertEquals(502, ex.getStatus());
            assertTrue(Thread.currentThread().isInterrupted());
        } finally {
            assertTrue(Thread.interrupted());
        }
        assertFalse(Thread.currentThread().isInterrupted());
    }
}
