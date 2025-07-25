package com.example.gl_exercise.service;

import com.example.gl_exercise.exception.UserNotFoundException;
import com.example.gl_exercise.mapper.UserMapper;
import com.example.gl_exercise.message.LoginResponse;
import com.example.gl_exercise.message.SignUpRequest;
import com.example.gl_exercise.message.SignUpResponse;
import com.example.gl_exercise.model.Phone;
import com.example.gl_exercise.model.User;
import com.example.gl_exercise.repository.UserRepository;
import com.example.gl_exercise.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

// Casos de prueba más livianos para el UserService.
// Estos usan mocks para probar condiciones y verificar los métodos.
// No se levanta el contexto de spring ni de hibernate.
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
    }

    // Se crean objetos de prueba y se verifican las llamadas a los mocks, también los condicionales.
    @Test
    void signUpUser() {
        // Un usuario "nuevo"
        final SignUpRequest signUpRequest = new SignUpRequest(
            "name", "email", "password", List.of(new SignUpRequest.Phone(123456789L, 1234, "countryCode")));
        // El usuario que debe userMapper::toEntity
        User transientUser = new User("email", "password", "name");
        transientUser.getPhones().add(new Phone(transientUser, 123456789L, 1234, "countryCode"));
        // El usuario que debe devolver Hibernate
        User persistedUser = new User("email", "password", "name");
        persistedUser.getPhones().add(new Phone(persistedUser, 123456789L, 1234, "countryCode"));
        // Campos autogenerados por Hibernate
        UUID userId = UUID.randomUUID();
        ReflectionTestUtils.setField(persistedUser, "id", userId);
        LocalDateTime createdAt = LocalDateTime.of(2023, 1, 1, 12, 0);
        ReflectionTestUtils.setField(persistedUser, "createdAt", createdAt);
        LocalDateTime lastLogin = LocalDateTime.of(2023, 1, 2, 12, 0);
        ReflectionTestUtils.setField(persistedUser, "lastLogin", lastLogin);
        ReflectionTestUtils.setField(persistedUser, "isActive", true);
        // Respuesta que debe devolver userMapper::toSignUpResponse
        final SignUpResponse signUpResponse = new SignUpResponse(
            userId.toString(),
            createdAt.format(UserMapper.DT_FORMATTER),
            lastLogin.format(UserMapper.DT_FORMATTER),
            "token",
            true
        );
        // Configuración de los mocks
        when(userMapper.toEntity(signUpRequest)).thenReturn(transientUser);
        when(userRepository.saveAndFlush(transientUser)).thenReturn(persistedUser);
        when(jwtUtil.generateUserToken(persistedUser)).thenReturn("token");
        when(userMapper.toSignUpResponse(persistedUser, "token")).thenReturn(signUpResponse);
        // Llamada al método
        userService.signUpUser(signUpRequest);
        // Verifico las llamadas con sus argumentos
        verify(userMapper, times(1)).toEntity(signUpRequest);
        verify(userRepository, times(1)).saveAndFlush(transientUser);
        verify(jwtUtil, times(1)).generateUserToken(persistedUser);
        verify(userMapper, times(1)).toSignUpResponse(persistedUser, "token");
    }

    @Test
    void signUpUserDuplicate() {
        // Solicitud de nuevo usuario
        final SignUpRequest signUpRequest = new SignUpRequest(
            "name", "email", "password", List.of(new SignUpRequest.Phone(123456789L, 1234, "countryCode")));
        // Usuario que debe devolver UserMapper::toEntity
        User usuarioTransitorio = new User("email", "password", "name");
        // Configuración de los mocks
        when(userMapper.toEntity(signUpRequest)).thenReturn(usuarioTransitorio);
        when(userRepository.saveAndFlush(usuarioTransitorio)).thenThrow(new UserNotFoundException("User Not Found"));
        // Tiene que arrojar la excepción
        assertThrows(UserNotFoundException.class, () -> userService.signUpUser(signUpRequest));
        // Verifica las llamadas a los mocks
        verify(userMapper, times(1)).toEntity(signUpRequest);
        verify(userRepository, times(1)).saveAndFlush(usuarioTransitorio);
    }

    // Se crean objetos de prueba, se configuran mocks y se verifican las llamadas.
    @Test
    void login() {
        // Email del usuario que quiere iniciar sesión, lo usa el repositorio para buscar
        String email = "example@email.com";
        // Usuario encontrado en la base de datos
        User persistido = new User("email", "password", "name");
        persistido.getPhones().add(new Phone(persistido, 123456789L, 1234, "countryCode"));
        // Campos generados por Hibernate...
        UUID userId = UUID.randomUUID();
        ReflectionTestUtils.setField(persistido, "id", userId);
        LocalDateTime createdAt = LocalDateTime.of(2023, 1, 1, 12, 0);
        ReflectionTestUtils.setField(persistido, "createdAt", createdAt);
        LocalDateTime lastLogin = LocalDateTime.of(2023, 1, 2, 12, 0);
        ReflectionTestUtils.setField(persistido, "lastLogin", lastLogin);
        ReflectionTestUtils.setField(persistido, "isActive", true);
        // Token que devuelve JwtUtil al pasarle el usuario persistido (encontrado)
        String token = "123456789";
        // Respuesta stub que devuelve UserMapper::toLoginResponse
        LoginResponse loginResponse = new LoginResponse(
            persistido.getId().toString(),
            "2025-07-25",
            "2025-07-26",
            token,
            true,
            "example",
            "example@email.com",
            "",
            Collections.emptyList()
        );
        // Configuración de mocks
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(persistido)); // Devuelve el detached
        when(jwtUtil.generateUserToken(persistido)).thenReturn(token);
        when(userRepository.save(persistido)).thenReturn(persistido); // Da igual que retorne el mismo para este caso de prueba.
        when(userMapper.toLoginResponse(persistido, token)).thenReturn(loginResponse);

        userService.login(email);

        verify(userRepository, times(1)).findByEmail(email);
        verify(jwtUtil, times(1)).generateUserToken(persistido);
        verify(userRepository, times(1)).save(persistido);
        verify(userMapper, times(1)).toLoginResponse(persistido, token);
    }

    // Cuando un usuario no se encuentra por email, el repository tiene que devolver un optional vacío.
    // Si el optional está vacío, hay que tirar una excepción.
    @Test
    void loginUserNotFound() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.login("example@email.com"));
        verify(userRepository, times(1)).findByEmail(anyString());
    }

}
