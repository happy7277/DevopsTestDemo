package com.example.demo;

import java.io.ByteArrayInputStream;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class BaselineTest {
    private static final String KEY = "example-only-test-secret-0123456789012";

    @Test
    void tokenChecksSignatureAndExactExpiration() {
        var start = Instant.parse("2025-01-01T00:00:00Z");
        var clock = Clock.fixed(start, ZoneOffset.UTC);
        var tokens = new JwtTokens(KEY, clock);
        String authorization = "Bearer " + tokens.issue("admin@example.com");
        assertEquals("admin@example.com", tokens.subject(authorization).orElseThrow());
        assertTrue(new JwtTokens(KEY + "other", clock).subject(authorization).isEmpty());
        var expiry = Clock.fixed(start.plus(JwtTokens.LIFETIME), ZoneOffset.UTC);
        assertTrue(new JwtTokens(KEY, expiry).subject(authorization).isEmpty());
        assertTrue(tokens.subject("Bearer invalid").isEmpty());
    }

    @Test
    void exportPreservesTextAndHeader() throws Exception {
        var user = new User();
        user.id = 42L;
        user.name = "=1+1";
        user.email = "sample@example.com";
        user.status = User.Status.ACTIVE;
        try (var workbook = new XSSFWorkbook(new ByteArrayInputStream(UserExport.write(List.of(user))))) {
            var sheet = workbook.getSheet("Users");
            assertEquals("Email", sheet.getRow(0).getCell(2).getStringCellValue());
            assertEquals("=1+1", sheet.getRow(1).getCell(1).getStringCellValue());
        }
    }

    @Test
    void invalidPageFailsBeforeDatabaseAccess() {
        var service = new UserService(mock(UserMapper.class), mock(Audit.class));
        assertEquals(400, assertThrows(ResponseStatusException.class, () -> service.list(0, 10, "") )
            .getStatusCode().value());
    }
}
