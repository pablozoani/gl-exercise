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

/**
 * Controlador para manejar registro e inicio de sesión de usuarios.
 * Se delega la lógica de aplicación al UserService.
 */
@Slf4j
@RestController
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Endpoint para registrar un nuevo usuario.
     *
     * @param req Datos de registro del usuario
     * @return Respuesta con los datos del usuario registrado y su token JWT
     */
    @PostMapping("/sign-up")
    public ResponseEntity<?> signUp(@RequestBody @Valid SignUpRequest req) {
        SignUpResponse signUpResponse = this.userService.signUpUser(req);
        return ResponseEntity.ok(signUpResponse);
    }

    /**
     * Endpoint para iniciar sesión.
     *
     * @param email Email del usuario obtenido del token (ya validado)
     * @return Respuesta con los datos del usuario y nuevo token JWT
     */
    @GetMapping("/login")
    public ResponseEntity<?> login(@AuthenticationPrincipal String email) {
        LoginResponse loginResponse = this.userService.login(email);
        return ResponseEntity.ok(loginResponse);
    }

}
