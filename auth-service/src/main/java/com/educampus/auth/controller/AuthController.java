package com.educampus.auth.controller;

import com.educampus.auth.dto.LoginRequest;
import com.educampus.auth.dto.TokenResponse;
import com.educampus.auth.exception.InvalidCredentialsException;
import com.educampus.auth.service.JwtService;
import com.educampus.auth.service.UserStore;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserStore userStore;
    private final JwtService jwtService;

    public AuthController(UserStore userStore, JwtService jwtService) {
        this.userStore = userStore;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        UserStore.StoredUser user = userStore.authenticate(request.username(), request.password())
                .orElseThrow(InvalidCredentialsException::new);
        return new TokenResponse(jwtService.generate(user.username(), user.roles()),
                "Bearer", jwtService.expiresInSeconds(), user.roles());
    }
}