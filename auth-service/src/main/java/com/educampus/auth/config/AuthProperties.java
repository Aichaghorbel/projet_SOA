package com.educampus.auth.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "auth")
public record AuthProperties(Jwt jwt, List<User> users) {

    public record Jwt(String secret, long expirationMinutes) {
    }

    public record User(String username, String password, List<String> roles) {
    }
}