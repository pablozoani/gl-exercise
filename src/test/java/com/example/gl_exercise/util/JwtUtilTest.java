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

    private JwtUtil jwtUtilUnderTest;

    @BeforeEach
    void setUp() {
        jwtUtilUnderTest = new JwtUtil("FKmg6d77tgqfen7n1g763nvsqe3kJadou18aDa8d9asO", 10_000L);
    }

    // Short test case for demo purposes
    @Test
    void testGenerateUserToken() {
        final User user = new User("email", "password", "name");

        // Hibernate fields
        UUID userId = UUID.randomUUID();
        ReflectionTestUtils.setField(user, "id", userId);

        LocalDateTime createdAt = LocalDateTime.of(2023, 1, 1, 12, 0);
        ReflectionTestUtils.setField(user, "createdAt", createdAt);

        LocalDateTime lastLogin = LocalDateTime.of(2023, 1, 2, 12, 0);
        ReflectionTestUtils.setField(user, "lastLogin", lastLogin);

        ReflectionTestUtils.setField(user, "isActive", true);

        final String result = jwtUtilUnderTest.generateUserToken(user);

        assertThat(result).isNotNull();

        System.out.println(result);

        Claims claims = this.jwtUtilUnderTest.parseToken(result);

        assertThat(claims.getSubject()).isEqualTo("email");
    }
}
