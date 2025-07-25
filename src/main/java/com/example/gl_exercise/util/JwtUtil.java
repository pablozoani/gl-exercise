package com.example.gl_exercise.util;

import com.example.gl_exercise.exception.TokenGenerationException;
import com.example.gl_exercise.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

/**
 * Utilidad para manejo de tokens JWT (generación y validación).
 */
@Component
public class JwtUtil {

    private final SecretKey secretKey;
    private final long tokenDurationSeconds;
    private final JwtParser parser;

    public JwtUtil(
        @Value("${jwt.secret}") String secret,
        @Value("${jwt.token.expiration.seconds}") long tokenDurationSeconds
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.tokenDurationSeconds = tokenDurationSeconds;
        this.parser = Jwts.parser().verifyWith(secretKey).build();
    }

    /**
     * Analiza y valida un token JWT, extrayendo sus claims.
     *
     * @param token Token JWT a validar
     * @return Claims contenidos en el token
     * @throws JwtException si el token es inválido o está expirado
     */
    public Claims parseToken(String token) {
        return this.parser.parseSignedClaims(token).getPayload();
    }

    /**
     * Genera un token JWT para el usuario con su email como subject.
     *
     * @param user Usuario para el que se genera el token (debe tener ID)
     * @return Token JWT
     * @throws TokenGenerationException si el usuario no está persistido
     */
    public String generateUserToken(User user) {
        if (user.getId() == null) {
            throw new TokenGenerationException("User not persisted");
        }

        Instant issuedAt = Instant.now();

        Instant expiration = issuedAt.plusSeconds(this.tokenDurationSeconds);

        return Jwts.builder()
            .subject(user.getEmail())
            .issuedAt(Date.from(issuedAt))
            .expiration(Date.from(expiration))
            .signWith(this.secretKey)
            .compact();
    }

}
