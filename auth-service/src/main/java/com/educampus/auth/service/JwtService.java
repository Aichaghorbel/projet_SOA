package com.educampus.auth.service;

import com.educampus.auth.config.AuthProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

/**
 * Génère les JWT (HS256). Contenu : sub = nom d'utilisateur, roles = liste de rôles, iat, exp.
 * La Gateway vérifie la signature avec le même secret.
 */
@Service
public class JwtService {

    private final SecretKey key;
    private final long expirationMinutes;

    public JwtService(AuthProperties properties) {
        byte[] secret = properties.jwt().secret().getBytes(StandardCharsets.UTF_8);
        if (secret.length < 32) {
            throw new IllegalStateException("JWT_SECRET doit contenir au moins 32 caractères (HS256)");
        }
        this.key = Keys.hmacShaKeyFor(secret);
        this.expirationMinutes = properties.jwt().expirationMinutes();
    }

    public String generate(String username, List<String> roles) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(username)
                .claim("roles", roles)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expirationMinutes * 60)))
                .signWith(key)
                .compact();
    }

    public long expiresInSeconds() {
        return expirationMinutes * 60;
    }
}