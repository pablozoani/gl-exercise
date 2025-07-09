package com.example.gl_exercise.controller;

import com.example.gl_exercise.message.LoginResponse;
import com.example.gl_exercise.message.SignUpRequest;
import com.example.gl_exercise.message.SignUpResponse;
import com.example.gl_exercise.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/sign-up")
    public ResponseEntity<?> signUp(@RequestBody @Valid SignUpRequest req) {
        SignUpResponse signUpResponse = this.userService.signUpUser(req);
        return ResponseEntity.ok(signUpResponse);
    }

    @GetMapping("/login")
    public ResponseEntity<?> login(@AuthenticationPrincipal String email) {
        LoginResponse loginResponse = this.userService.login(email);
        return ResponseEntity.ok(loginResponse);
    }
}
