package com.rs.gridservice.exception;

/**
 * Giden istek proxy'sinin (bkz. proxy.OutboundProxyService) reddettigi/basaramadigi durumlar.
 * {@code status} dogrudan HTTP durum koduna cevrilir (bkz. GlobalExceptionHandler): 400 gecersiz istek,
 * 403 engellenen adres, 413 govde cok buyuk, 502 hedefe ulasilamadi, 504 zaman asimi.
 */
public class ProxyException extends RuntimeException {

    private final int status;

    public ProxyException(int status, String message) {
        super(message);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
