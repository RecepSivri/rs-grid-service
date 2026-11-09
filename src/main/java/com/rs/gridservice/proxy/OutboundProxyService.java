package com.rs.gridservice.proxy;

import com.rs.gridservice.exception.ProxyException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Flow;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

/**
 * Kullanicinin tarayicisindan gelen istegi hedef REST API'ye iletir (tarayicinin CORS kisitini asmak icin).
 * Hedef kontrolu {@link TargetPolicy}'de. Burada ayrica:
 * - yonlendirmeler TAKIP EDILMEZ (3xx oldugu gibi doner; yonlendirme ile ic aga sicrama olmaz),
 * - istek ve yanit govdesi boyutu, baglanti ve toplam sure sinirlidir,
 * - taşima basliklari (Host, Content-Length, Connection ...) proxy tarafindan uretilir, kullanicinin gonderdigi atilir,
 * - header/govde/sorgu DEGERLERI loglanmaz (bkz. ProxyController).
 */
public class OutboundProxyService {

    private static final Set<String> METHODS = Set.of("GET", "POST", "PUT", "PATCH", "DELETE", "HEAD", "OPTIONS");
    private static final Set<String> TRANSPORT_HEADERS = Set.of("host", "content-length", "connection", "transfer-encoding",
            "te", "trailer", "upgrade", "keep-alive", "expect", "accept-encoding", "proxy-authorization", "proxy-connection");
    private static final Pattern CHARSET = Pattern.compile("charset=\"?([^;\\s\"]+)", Pattern.CASE_INSENSITIVE);

    private final ProxySettings settings;
    private final TargetPolicy policy;
    private final HttpClient client;

    public OutboundProxyService(ProxySettings settings) {
        this.settings = settings;
        this.policy = new TargetPolicy(settings);
        this.client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NEVER)
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(settings.connectTimeout())
                .build();
    }

    public ProxyResult execute(String method, String url, Map<String, String> headers, String body) {
        String httpMethod = method == null ? "" : method.trim().toUpperCase(Locale.ROOT);
        if (!METHODS.contains(httpMethod)) {
            throw new ProxyException(400, "Desteklenmeyen HTTP metodu");
        }
        byte[] bodyBytes = body == null ? new byte[0] : body.getBytes(StandardCharsets.UTF_8);
        if (bodyBytes.length > settings.maxRequestBytes()) {
            throw new ProxyException(413, "Istek govdesi cok buyuk (en fazla " + settings.maxRequestBytes() + " bayt)");
        }
        URI uri = policy.check(url);

        boolean sendsBody = bodyBytes.length > 0 && !httpMethod.equals("GET") && !httpMethod.equals("HEAD");
        HttpRequest.Builder builder = HttpRequest.newBuilder(uri)
                .timeout(settings.readTimeout())
                .header("Accept-Encoding", "gzip, deflate")
                .method(httpMethod, sendsBody ? HttpRequest.BodyPublishers.ofByteArray(bodyBytes) : HttpRequest.BodyPublishers.noBody());
        if (headers != null) {
            applyHeaders(builder, headers);
        }
        HttpRequest request = builder.build();

        long started = System.nanoTime();
        HttpResponse<byte[]> response = send(request);
        long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started);

        byte[] raw = response.body();
        String contentEncoding = response.headers().firstValue("Content-Encoding").orElse("").trim().toLowerCase(Locale.ROOT);
        byte[] decoded = decode(raw, contentEncoding);
        Charset charset = charsetOf(response.headers().firstValue("Content-Type").orElse(""));

        List<ProxyResult.Header> responseHeaders = new ArrayList<>();
        response.headers().map().forEach((key, values) -> values.forEach(v -> responseHeaders.add(new ProxyResult.Header(key, v))));
        return new ProxyResult(response.statusCode(), responseHeaders, new String(decoded, charset), elapsedMs);
    }

    private void applyHeaders(HttpRequest.Builder builder, Map<String, String> headers) {
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            String name = entry.getKey() == null ? "" : entry.getKey().trim();
            if (name.isEmpty()) {
                throw new ProxyException(400, "Header adi bos olamaz");
            }
            if (TRANSPORT_HEADERS.contains(name.toLowerCase(Locale.ROOT))) {
                continue;
            }
            try {
                builder.header(name, entry.getValue() == null ? "" : entry.getValue());
            } catch (IllegalArgumentException e) {
                throw new ProxyException(400, "Gecersiz header: " + name);
            }
        }
    }

    private HttpResponse<byte[]> send(HttpRequest request) {
        // Yanit en fazla maxResponseBytes tamponlanir; asilirsa abonelik iptal edilir (bellek sismez).
        CompletableFuture<HttpResponse<byte[]>> future = client.sendAsync(request,
                info -> new BoundedBodySubscriber(settings.maxResponseBytes()));
        try {
            // HttpRequest.timeout yalnizca yanit BASLIKLARI icin gecerli; govdeyi yavas damlatan sunucuya karsi toplam sure siniri.
            return future.get(settings.readTimeout().toMillis(), TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            throw timeout();
        } catch (ExecutionException e) {
            if (e.getCause() instanceof ProxyException proxyException) {
                throw proxyException;
            }
            if (e.getCause() instanceof HttpTimeoutException) {
                throw timeout();
            }
            throw new ProxyException(502, "Hedef sunucuya baglanilamadi");
        } catch (InterruptedException e) {
            future.cancel(true);
            Thread.currentThread().interrupt();
            throw new ProxyException(502, "Istek kesildi");
        }
    }

    private byte[] decode(byte[] raw, String contentEncoding) {
        try {
            if (contentEncoding.equals("gzip")) {
                return readLimited(new GZIPInputStream(new ByteArrayInputStream(raw)));
            }
            if (contentEncoding.equals("deflate")) {
                return readLimited(new InflaterInputStream(new ByteArrayInputStream(raw)));
            }
            return raw;
        } catch (IOException e) {
            throw new ProxyException(502, "Hedef yaniti cozulemedi (" + contentEncoding + ")");
        }
    }

    private byte[] readLimited(InputStream in) throws IOException {
        try (in) {
            byte[] data = in.readNBytes(settings.maxResponseBytes() + 1);
            if (data.length > settings.maxResponseBytes()) {
                throw tooLarge(settings.maxResponseBytes());
            }
            return data;
        }
    }

    static Charset charsetOf(String contentType) {
        Matcher m = CHARSET.matcher(contentType);
        if (!m.find()) {
            return StandardCharsets.UTF_8;
        }
        try {
            return Charset.forName(m.group(1));
        } catch (IllegalArgumentException e) {
            return StandardCharsets.UTF_8;
        }
    }

    static ProxyException tooLarge(int maxBytes) {
        return new ProxyException(413, "Hedef yaniti cok buyuk (en fazla " + maxBytes + " bayt)");
    }

    private static ProxyException timeout() {
        return new ProxyException(504, "Hedef sunucu zaman asimina ugradi");
    }

    /** Gelen govdeyi en fazla {@code max} bayt biriktirir; asilirsa aboneligi iptal edip 413 ile biter. */
    private static final class BoundedBodySubscriber implements HttpResponse.BodySubscriber<byte[]> {
        private final int max;
        private final ByteArrayOutputStream out = new ByteArrayOutputStream();
        private final CompletableFuture<byte[]> result = new CompletableFuture<>();
        private Flow.Subscription subscription;

        BoundedBodySubscriber(int max) {
            this.max = max;
        }

        @Override
        public CompletableFuture<byte[]> getBody() {
            return result;
        }

        @Override
        public void onSubscribe(Flow.Subscription subscription) {
            this.subscription = subscription;
            subscription.request(Long.MAX_VALUE);
        }

        @Override
        public void onNext(List<ByteBuffer> items) {
            for (ByteBuffer buffer : items) {
                int n = buffer.remaining();
                if (out.size() + n > max) {
                    subscription.cancel();
                    result.completeExceptionally(tooLarge(max));
                    return;
                }
                byte[] chunk = new byte[n];
                buffer.get(chunk);
                out.write(chunk, 0, n);
            }
        }

        @Override
        public void onError(Throwable throwable) {
            result.completeExceptionally(throwable);
        }

        @Override
        public void onComplete() {
            result.complete(out.toByteArray());
        }
    }
}
