package com.example.gl_exercise.util;

import com.example.gl_exercise.model.User;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil("FKmg6d77tgqfen7n1g763nvsqe3kJadou18aDa8d9asO", 10_000L);
    }

    @Test
    void testGenerateUserToken() {
        final User persisted = new User("email", "password", "name");

        // Hibernate fields
        UUID userId = UUID.randomUUID();
        ReflectionTestUtils.setField(persisted, "id", userId);
        LocalDateTime createdAt = LocalDateTime.of(2023, 1, 1, 12, 0);
        ReflectionTestUtils.setField(persisted, "createdAt", createdAt);
        LocalDateTime lastLogin = LocalDateTime.of(2023, 1, 2, 12, 0);
        ReflectionTestUtils.setField(persisted, "lastLogin", lastLogin);
        ReflectionTestUtils.setField(persisted, "isActive", true);

        // Genera el token para el usuario dado
        final String token = jwtUtil.generateUserToken(persisted);

        assertThat(token).isNotNull();

        // Analiza el token
        Claims claims = jwtUtil.parseToken(token);

        // Corrobora que el token contenga el email en el subject
        assertThat(claims.getSubject()).isEqualTo("email");
    }
}
