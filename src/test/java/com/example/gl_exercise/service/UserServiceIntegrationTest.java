package com.example.gl_exercise.service;

import com.example.gl_exercise.exception.UserAlreadyExistsException;
import com.example.gl_exercise.exception.UserNotFoundException;
import com.example.gl_exercise.mapper.UserMapper;
import com.example.gl_exercise.message.LoginResponse;
import com.example.gl_exercise.message.SignUpRequest;
import com.example.gl_exercise.message.SignUpResponse;
import com.example.gl_exercise.repository.UserRepository;
import com.example.gl_exercise.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

// Prueba de integración real de UserService usando base de datos embebida.
// La anotación @DirtiesContext asegura un contexto limpio antes de ejecutar los tests.
// Recordemos que las otras pruebas reutilizan la misma instancia de base de datos en memoria.
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class UserServiceIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    final SignUpRequest signUpRequest = new SignUpRequest(
        "example",
        "example@email",
        "passwordA12",
        List.of(new SignUpRequest.Phone(123456789L, 1234, "54"))
    );

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    // Verifica que se puede registrar un nuevo usuario correctamente
    @Test
    void signUpUser() {
        SignUpResponse signUpResponse = userService.signUpUser(signUpRequest);
        // Generados por hibernate
        assertThat(signUpResponse.id()).isNotNull();
        assertThat(signUpResponse.created()).isNotNull();
        assertThat(signUpResponse.lastLogin()).isNotNull();
        String subject = jwtUtil.parseToken(signUpResponse.token()).getSubject();
        assertThat(subject).isEqualTo(signUpRequest.email());
    }

    // Verifica que un usuario no pueda registrarse si el email ya existe
    @Test
    void signUpUserDuplicateEmail() {
        // Se registra este primero
        userService.signUpUser(signUpRequest);
        // Otro con el mismo email trata de hacer sign up
        assertThrows(UserAlreadyExistsException.class, () -> userService.signUpUser(signUpRequest));
    }

    // Verifica que un usuario pueda iniciar sesión correctamente
    @Test
    void login() throws InterruptedException {
        SignUpResponse signUpResponse = userService.signUpUser(signUpRequest);
        // Para que los dos tokens sean distintos tiene que haber una diferencia temporal de 1 segundo como mínimo
        Thread.sleep(1000L);
        LoginResponse loginResponse = userService.login(signUpRequest.email());
        assertThat(loginResponse.id()).isEqualTo(signUpResponse.id()); // Mismo ID
        assertThat(loginResponse.token()).isNotEqualTo(signUpResponse.token()); // Distinto token
        // Distinto último inicio de sesión
        assertThat(loginResponse.lastLogin()).isNotEqualTo(signUpResponse.lastLogin());
        assertThat(loginResponse.password()).isBlank(); // No devolvamos contraseña al frontend
    }

    // Verifica que tira excepción si se intenta iniciar sesión con un email no registrado
    @Test
    void loginUserNotFound() {
        assertThrows(UserNotFoundException.class, () -> userService.login("example@email"));
    }

}