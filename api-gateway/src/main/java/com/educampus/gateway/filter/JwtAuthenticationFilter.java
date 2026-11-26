package com.educampus.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.List;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/** Filtre global : vérifie le JWT (signature, expiration) et les rôles avant de router. */
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final String CERTIFICATION_PATH = "/api/v1/certification-requests";

    private final SecretKey key;

    public JwtAuthenticationFilter(@Value("${jwt.secret}") String secret) {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("JWT_SECRET doit contenir au moins 32 caractères (HS256)");
        }
        this.key = Keys.hmacShaKeyFor(bytes);
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        HttpMethod method = request.getMethod();

        // Les en-têtes d'identité du client ne sont jamais crus : retirés dans tous les cas
        ServerHttpRequest cleaned = request.mutate()
                .headers(h -> {
                    h.remove("X-User");
                    h.remove("X-Roles");
                })
                .build();

        if (isPublic(path) || HttpMethod.OPTIONS.equals(method)) {
            return chain.filter(exchange.mutate().request(cleaned).build());
        }

        String authorization = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return reject(exchange, HttpStatus.UNAUTHORIZED, "Token manquant : en-tête Authorization: Bearer <token> requis");
        }

        Claims claims;
        try {
            claims = Jwts.parser().verifyWith(key).build()
                    .parseSignedClaims(authorization.substring(7).trim())
                    .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            return reject(exchange, HttpStatus.UNAUTHORIZED, "Token invalide ou expiré");
        }

        List<?> rolesClaim = claims.get("roles", List.class);
        List<String> roles = rolesClaim == null ? List.of()
                : rolesClaim.stream().map(String::valueOf).toList();

        if (!isAllowed(method, path, roles)) {
            return reject(exchange, HttpStatus.FORBIDDEN, "Accès refusé : rôle insuffisant pour cette opération");
        }

        ServerHttpRequest authenticated = cleaned.mutate()
                .header("X-User", claims.getSubject())
                .header("X-Roles", String.join(",", roles))
                .build();
        return chain.filter(exchange.mutate().request(authenticated).build());
    }

    private boolean isPublic(String path) {
        return path.equals("/auth") || path.startsWith("/auth/")
                || path.equals("/swagger-ui.html") || path.startsWith("/swagger-ui/")
                || path.startsWith("/v3/api-docs");
    }

    private boolean isAllowed(HttpMethod method, String path, List<String> roles) {
        if (HttpMethod.GET.equals(method) || HttpMethod.HEAD.equals(method)) {
            return true; // lecture : tout utilisateur connecté
        }
        if (roles.contains("ADMIN") || roles.contains("TEACHER")) {
            return true;
        }
        return HttpMethod.POST.equals(method) && path.equals(CERTIFICATION_PATH) && roles.contains("STUDENT");
    }

    private Mono<Void> reject(ServerWebExchange exchange, HttpStatus status, String message) {
        String json = "{\"timestamp\":\"" + OffsetDateTime.now() + "\","
                + "\"status\":" + status.value() + ","
                + "\"error\":\"" + status.getReasonPhrase() + "\","
                + "\"message\":\"" + message + "\"}";
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(json.getBytes(StandardCharsets.UTF_8));
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -1; // avant le routage
    }
}