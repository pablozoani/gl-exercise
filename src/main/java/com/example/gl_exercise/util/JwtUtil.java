package com.example.gl_exercise.util;

import com.example.gl_exercise.exception.TokenGenerationException;
import com.example.gl_exercise.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

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

    public Claims parseToken(String token) {
        return this.parser.parseSignedClaims(token).getPayload();
    }

    public String generateUserToken(User user) {
        if (user.getId() == null) {
            throw new TokenGenerationException("User not managed");
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
