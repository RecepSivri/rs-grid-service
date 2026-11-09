package com.rs.gridservice.proxy;

import com.rs.gridservice.exception.ProxyException;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TargetPolicyTest {

    private static TargetPolicy policy(boolean allowPrivate, String... allowedHosts) {
        return new TargetPolicy(ProxySettings.of(allowPrivate, List.of(allowedHosts), 1000, 1000, 1000, 1000));
    }

    private static InetAddress ip(String literal) throws Exception {
        return InetAddress.getByName(literal);
    }

    // ---- parse ----

    @Test
    void parseRejectsMissingBlankOrTooLongUrl() {
        assertEquals(400, assertThrows(ProxyException.class, () -> TargetPolicy.parse(null)).getStatus());
        assertEquals(400, assertThrows(ProxyException.class, () -> TargetPolicy.parse("   ")).getStatus());
        String tooLong = "http://example.com/" + "a".repeat(TargetPolicy.MAX_URL_LENGTH);
        assertEquals(400, assertThrows(ProxyException.class, () -> TargetPolicy.parse(tooLong)).getStatus());
    }

    @Test
    void parseRejectsSyntaxErrors() {
        ProxyException ex = assertThrows(ProxyException.class, () -> TargetPolicy.parse("http://exa mple.com/x"));
        assertEquals(400, ex.getStatus());
        assertEquals("Gecersiz URL", ex.getMessage());
    }

    @Test
    void parseRejectsMissingOrNonHttpScheme() {
        assertEquals(400, assertThrows(ProxyException.class, () -> TargetPolicy.parse("example.com/x")).getStatus());
        ProxyException ftp = assertThrows(ProxyException.class, () -> TargetPolicy.parse("ftp://example.com/x"));
        assertEquals("Yalnizca http ve https adresleri desteklenir", ftp.getMessage());
        assertEquals(400, assertThrows(ProxyException.class, () -> TargetPolicy.parse("file:///etc/passwd")).getStatus());
    }

    @Test
    void parseRejectsMissingHostAndUserInfo() {
        assertEquals(400, assertThrows(ProxyException.class, () -> TargetPolicy.parse("http:///path")).getStatus());
        assertEquals(400, assertThrows(ProxyException.class, () -> TargetPolicy.parse("http://exa_mple.com/")).getStatus());
        assertEquals(400, assertThrows(ProxyException.class, () -> TargetPolicy.parse("http://user:pass@example.com/")).getStatus());
    }

    @Test
    void parseAcceptsHttpAndHttpsInAnyCase() {
        assertEquals("example.com", TargetPolicy.parse("HTTP://example.com/x?y=1").getHost());
        assertEquals("https", TargetPolicy.parse("  https://example.com:8443/a  ").getScheme());
    }

    // ---- isBlocked ----

    @Test
    void alwaysBlockedAddressesStayBlockedEvenWhenPrivateNetworksAreAllowed() throws Exception {
        List<String> alwaysBlocked = List.of(
                "0.0.0.0", "0.1.2.3", "224.0.0.1", "239.255.255.255", "240.0.0.1", "255.255.255.255",
                "169.254.169.254", "169.254.0.1",
                "fe80::1", "ff02::1", "fd00:ec2::254",
                "::2", "::ffff:0:1", "64:ff9b::808:808", "2002:c0a8:101::1");
        for (String literal : alwaysBlocked) {
            assertTrue(TargetPolicy.isBlocked(ip(literal), false), literal + " (privateOk=false)");
            assertTrue(TargetPolicy.isBlocked(ip(literal), true), literal + " (privateOk=true)");
        }
    }

    @Test
    void privateAddressesAreBlockedUnlessPrivateNetworksAreAllowed() throws Exception {
        List<String> privateRanges = List.of(
                "127.0.0.1", "127.255.255.254", "10.1.2.3", "172.16.0.1", "172.31.255.255", "192.168.1.1",
                "100.64.0.1", "100.127.255.255", "198.18.0.1", "198.19.255.255", "192.0.0.1",
                "::1", "fc00::1", "fd12:3456::1");
        for (String literal : privateRanges) {
            assertTrue(TargetPolicy.isBlocked(ip(literal), false), literal + " (privateOk=false)");
            assertFalse(TargetPolicy.isBlocked(ip(literal), true), literal + " (privateOk=true)");
        }
    }

    @Test
    void publicAddressesAreNeverBlocked() throws Exception {
        List<String> publicAddresses = List.of(
                "8.8.8.8", "93.184.216.34", "172.15.255.255", "172.32.0.1", "192.167.1.1", "193.168.0.1",
                "100.63.255.255", "100.128.0.1", "198.17.255.255", "198.20.0.1", "192.0.1.1", "192.1.1.1",
                "2606:4700::1111", "2001:db8::1", "64:ff9a::1", "64:ff9b:1::1", "64:1::1", "1::1", "2003::1");
        for (String literal : publicAddresses) {
            assertFalse(TargetPolicy.isBlocked(ip(literal), false), literal + " (privateOk=false)");
            assertFalse(TargetPolicy.isBlocked(ip(literal), true), literal + " (privateOk=true)");
        }
    }

    @Test
    void ipv4MappedIpv6IsJudgedByItsIpv4Part() throws Exception {
        assertTrue(TargetPolicy.isBlocked(ip("::ffff:127.0.0.1"), false));
        assertTrue(TargetPolicy.isBlocked(ip("::ffff:169.254.169.254"), true));
        assertFalse(TargetPolicy.isBlocked(ip("::ffff:8.8.8.8"), false));
    }

    // ---- check ----

    @Test
    void checkRejectsLoopbackAndPrivateLiteralsByDefault() {
        TargetPolicy policy = policy(false);
        for (String url : List.of("http://127.0.0.1:8080/x", "http://10.0.0.5/", "http://192.168.1.10:3000/api",
                "http://[::1]:8080/", "http://[fd00::1]/")) {
            assertEquals(403, assertThrows(ProxyException.class, () -> policy.check(url)).getStatus(), url);
        }
    }

    @Test
    void checkRejectsAlternativeLoopbackNotations() {
        TargetPolicy policy = policy(false);
        // "127.1" ve "0x7f.0.0.1" gecerli bir URI host'u degil -> parse asamasinda 400
        for (String url : List.of("http://127.1/", "http://0x7f.0.0.1/")) {
            assertEquals(400, assertThrows(ProxyException.class, () -> policy.check(url)).getStatus(), url);
        }
        // onluk tek sayi (2130706433 == 127.0.0.1) cozumlenir ve adres duzeyinde yakalanir
        assertEquals(403, assertThrows(ProxyException.class, () -> policy.check("http://2130706433/")).getStatus());
    }

    @Test
    void checkAlwaysRejectsCloudMetadataEvenWithPrivateNetworksAllowed() {
        TargetPolicy policy = policy(true);
        ProxyException ex = assertThrows(ProxyException.class, () -> policy.check("http://169.254.169.254/latest/meta-data/"));
        assertEquals(403, ex.getStatus());
        assertTrue(ex.getMessage().contains("169.254.169.254"));
        assertEquals(403, assertThrows(ProxyException.class, () -> policy.check("http://[fd00:ec2::254]/")).getStatus());
    }

    @Test
    void checkAllowsPublicLiteralsAndPrivateOnesWhenPermitted() {
        assertEquals(URI.create("http://8.8.8.8/dns"), policy(false).check("http://8.8.8.8/dns"));
        assertEquals("127.0.0.1", policy(true).check("http://127.0.0.1:9000/x").getHost());
        assertEquals("[::1]", policy(true).check("http://[::1]:9000/x").getHost());
    }

    @Test
    void checkHonoursAllowedHostListForPrivateTargetsOnly() {
        TargetPolicy policy = policy(false, "LocalHost");
        assertEquals("localhost", policy.check("http://localhost:9000/x").getHost());
        assertEquals(403, assertThrows(ProxyException.class, () -> policy.check("http://127.0.0.1:9000/x")).getStatus());
        // izin listesi metadata adresini acmaz
        assertEquals(403, assertThrows(ProxyException.class, () -> policy(false, "169.254.169.254").check("http://169.254.169.254/")).getStatus());
    }

    @Test
    void checkReportsUnresolvableHostAsBadGateway() {
        ProxyException ex = assertThrows(ProxyException.class, () -> policy(false).check("http://rs-grid-nonexistent-host.invalid/x"));
        assertEquals(502, ex.getStatus());
        assertTrue(ex.getMessage().contains("rs-grid-nonexistent-host.invalid"));
    }
}
