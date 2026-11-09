package com.rs.gridservice.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProxyExceptionTest {

    @Test
    void carriesStatusAndMessage() {
        ProxyException ex = new ProxyException(403, "engellendi");

        assertEquals(403, ex.getStatus());
        assertEquals("engellendi", ex.getMessage());
    }
}
