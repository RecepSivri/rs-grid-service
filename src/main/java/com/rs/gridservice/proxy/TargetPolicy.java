package com.rs.gridservice.proxy;

import com.rs.gridservice.exception.ProxyException;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;

/**
 * Proxy'nin ulasabilecegi hedefleri sinirlar (SSRF korumasi).
 *
 * Kurallar:
 * - Yalnizca http/https, host zorunlu, "kullanici:sifre@" yok.
 * - Host DNS ile cozulur ve cozulen BUTUN adresler kontrol edilir (biri bile engelliyse ret) -- sayisal
 *   gosterimler (2130706433, 0x7f.1, 127.1) ve IPv4-mapped IPv6 de adres duzeyinde yakalanir.
 * - HER ZAMAN engelli: 0.0.0.0/8, 224.0.0.0/4 (multicast), 240.0.0.0/4 (+yayin), link-local (169.254.0.0/16 =
 *   bulut metadata, fe80::/10), fd00:ec2::254, IPv4 gomen IPv6 bicimleri (::/96, 64:ff9b::/96, 2002::/16).
 * - Ozel aglar (loopback, 10/8, 172.16/12, 192.168/16, 100.64/10, 198.18/15, 192.0.0/24, fc00::/7): varsayilan
 *   engelli; {@link ProxySettings#allowPrivateNetworks()} veya host izin listesinde ise serbest.
 *
 * Bilinen sinir: kontrol ile baglanti arasinda DNS yeniden cozumlenir (DNS rebinding). JVM'in varsayilan
 * DNS onbellegi (30 sn) bu araligi pratikte kapatir ama garanti degildir; uretimde ek savunma olarak konteynerin
 * cikis agi ozel bloklara kapatilmalidir.
 */
public final class TargetPolicy {

    static final int MAX_URL_LENGTH = 2048;
    /** fd00:ec2::254 -- AWS IPv6 instance metadata. */
    private static final byte[] AWS_METADATA_V6 = {
            (byte) 0xfd, 0x00, 0x0e, (byte) 0xc2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0x02, 0x54};

    private final boolean allowPrivateNetworks;
    private final Set<String> allowedHosts;

    public TargetPolicy(ProxySettings settings) {
        this.allowPrivateNetworks = settings.allowPrivateNetworks();
        this.allowedHosts = settings.allowedHosts();
    }

    /** @return dogrulanmis URI; reddedilirse {@link ProxyException} (400/403/502). */
    public URI check(String rawUrl) {
        URI uri = parse(rawUrl);
        String host = uri.getHost().toLowerCase(Locale.ROOT);
        if (host.startsWith("[")) {
            host = host.substring(1, host.length() - 1);
        }
        boolean privateOk = allowPrivateNetworks || allowedHosts.contains(host);
        InetAddress[] addresses;
        try {
            addresses = InetAddress.getAllByName(host);
        } catch (UnknownHostException e) {
            throw new ProxyException(502, "Adres cozumlenemedi: " + host);
        }
        for (InetAddress address : addresses) {
            if (isBlocked(address, privateOk)) {
                throw new ProxyException(403,
                        "Bu adrese proxy uzerinden erisilemez (yerel/ozel ag veya metadata adresi): " + host);
            }
        }
        return uri;
    }

    static URI parse(String rawUrl) {
        if (rawUrl == null || rawUrl.isBlank() || rawUrl.length() > MAX_URL_LENGTH) {
            throw new ProxyException(400, "Gecersiz URL");
        }
        URI uri;
        try {
            uri = new URI(rawUrl.trim());
        } catch (URISyntaxException e) {
            throw new ProxyException(400, "Gecersiz URL");
        }
        String scheme = uri.getScheme();
        if (scheme == null || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
            throw new ProxyException(400, "Yalnizca http ve https adresleri desteklenir");
        }
        if (uri.getHost() == null || uri.getUserInfo() != null) {
            throw new ProxyException(400, "URL gecerli bir host icermeli ve kullanici bilgisi (kullanici:sifre@) icermemeli");
        }
        return uri;
    }

    static boolean isBlocked(InetAddress address, boolean privateOk) {
        if (address.isAnyLocalAddress() || address.isMulticastAddress() || address.isLinkLocalAddress()) {
            return true;
        }
        byte[] b = address.getAddress();
        if (b.length == 4) {
            return isBlockedV4(b, privateOk);
        }
        return isBlockedV6(address, b, privateOk);
    }

    private static boolean isBlockedV4(byte[] b, boolean privateOk) {
        int o1 = b[0] & 0xff;
        int o2 = b[1] & 0xff;
        int o3 = b[2] & 0xff;
        if (o1 == 0 || o1 >= 240) {
            return true;
        }
        if (privateOk) {
            return false;
        }
        return o1 == 127
                || o1 == 10
                || (o1 == 172 && o2 >= 16 && o2 <= 31)
                || (o1 == 192 && o2 == 168)
                || (o1 == 100 && o2 >= 64 && o2 <= 127)
                || (o1 == 198 && (o2 == 18 || o2 == 19))
                || (o1 == 192 && o2 == 0 && o3 == 0);
    }

    private static boolean isBlockedV6(InetAddress address, byte[] b, boolean privateOk) {
        if (Arrays.equals(b, AWS_METADATA_V6)) {
            return true;
        }
        if (address.isLoopbackAddress()) {
            return !privateOk;
        }
        boolean embedsV4 = allZero(b, 0, 12)
                || (b[0] == 0x00 && b[1] == 0x64 && (b[2] & 0xff) == 0xff && (b[3] & 0xff) == 0x9b && allZero(b, 4, 12))
                || (b[0] == 0x20 && b[1] == 0x02);
        if (embedsV4) {
            return true;
        }
        boolean uniqueLocal = (b[0] & 0xfe) == 0xfc;
        return uniqueLocal && !privateOk;
    }

    private static boolean allZero(byte[] b, int from, int toExclusive) {
        for (int i = from; i < toExclusive; i++) {
            if (b[i] != 0) {
                return false;
            }
        }
        return true;
    }
}
