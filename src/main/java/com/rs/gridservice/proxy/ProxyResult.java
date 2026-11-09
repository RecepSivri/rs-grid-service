package com.rs.gridservice.proxy;

import java.util.List;

/** Hedef sunucunun yaniti. Gövde metin olarak (charset'e gore) cozulmus, gzip/deflate acilmis halde. */
public record ProxyResult(int status, List<Header> headers, String body, long elapsedMs) {

    public record Header(String key, String value) {
    }
}
