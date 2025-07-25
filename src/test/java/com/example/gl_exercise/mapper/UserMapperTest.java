package com.example.gl_exercise.mapper;

import com.example.gl_exercise.message.LoginResponse;
import com.example.gl_exercise.message.SignUpRequest;
import com.example.gl_exercise.message.SignUpResponse;
import com.example.gl_exercise.model.Phone;
import com.example.gl_exercise.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

// Clase para probar el objeto que convierte entre usuarios y DTOs.
@ExtendWith(MockitoExtension.class)
class UserMapperTest {

    @Mock
    private PasswordEncoder mockPasswordEncoder;

    private UserMapper userMapper;

    @BeforeEach
    void setUp() {
        userMapper = new UserMapper(mockPasswordEncoder);
    }

    // Solicitud del endpoint /sign-up
    @Test
    void toEntity() {
        when(mockPasswordEncoder.encode(anyString())).thenReturn("@#$p455w0rd");

        final var signUpRequest = new SignUpRequest(
            "example",
            "example@email",
            "rawPassword",
            List.of(new SignUpRequest.Phone(123456789L, 1_000, "54"))
        );

        final User usuarioTransitorio = userMapper.toEntity(signUpRequest);

        assertThat(usuarioTransitorio.getName()).isEqualTo("example");
        assertThat(usuarioTransitorio.getEmail()).isEqualTo("example@email");
        assertThat(usuarioTransitorio.getPassword()).isEqualTo("@#$p455w0rd");
        assertThat(usuarioTransitorio.getPhones().size()).isEqualTo(1);
        usuarioTransitorio.getPhones().forEach(p -> {
            assertThat(p.getUser().getEmail()).isEqualTo("example@email");
            assertThat(p.getNumber()).isEqualTo(123456789L);
            assertThat(p.getCityCode()).isEqualTo(1_000);
            assertThat(p.getCountryCode()).isEqualTo("54");
        });

        verify(mockPasswordEncoder, times(1)).encode("rawPassword");
    }

    // Respuesta del endpoint /sign-up
    @Test
    void toSignUpResponse() {
        final User usuarioPersistido = new User("example@email", "password", "example");
        usuarioPersistido.getPhones().add(new Phone(usuarioPersistido, 123456789L, 1_000, "54"));

        // Hibernate fields
        UUID userId = UUID.randomUUID();
        ReflectionTestUtils.setField(usuarioPersistido, "id", userId);
        LocalDateTime createdAt = LocalDateTime.of(2023, 1, 1, 12, 0);
        ReflectionTestUtils.setField(usuarioPersistido, "createdAt", createdAt);
        LocalDateTime lastLogin = LocalDateTime.of(2023, 1, 2, 12, 0);
        ReflectionTestUtils.setField(usuarioPersistido, "lastLogin", lastLogin);
        ReflectionTestUtils.setField(usuarioPersistido, "isActive", true);

        final SignUpResponse result = userMapper.toSignUpResponse(usuarioPersistido, "token");

        assertThat(result.id()).isEqualTo(userId.toString());
        assertThat(result.created()).isEqualTo(createdAt.format(UserMapper.DT_FORMATTER));
        assertThat(result.lastLogin()).isEqualTo(lastLogin.format(UserMapper.DT_FORMATTER));
        assertThat(result.isActive()).isTrue();
        assertThat(result.token()).isEqualTo("token");
    }

    // Respuesta el endpoint /login
    @Test
    void toLoginResponse() {
        final User usuarioPersistido = new User("example@email", "password", "example");
        usuarioPersistido.getPhones().add(new Phone(usuarioPersistido, 123456789L, 1_000, "54"));
        // Hibernate fields
        UUID userId = UUID.randomUUID();
        ReflectionTestUtils.setField(usuarioPersistido, "id", userId);
        LocalDateTime createdAt = LocalDateTime.of(2023, 1, 1, 12, 0);
        ReflectionTestUtils.setField(usuarioPersistido, "createdAt", createdAt);
        LocalDateTime lastLogin = LocalDateTime.of(2023, 1, 2, 12, 0);
        ReflectionTestUtils.setField(usuarioPersistido, "lastLogin", lastLogin);
        ReflectionTestUtils.setField(usuarioPersistido, "isActive", true);

        var token = "1234567890";

        LoginResponse res = userMapper.toLoginResponse(usuarioPersistido, token);

        assertThat(res.created()).isEqualTo(usuarioPersistido.getCreatedAt().format(UserMapper.DT_FORMATTER));
        assertThat(res.lastLogin()).isEqualTo(usuarioPersistido.getLastLogin().format(UserMapper.DT_FORMATTER));
        assertThat(res.id()).isEqualTo(userId.toString());
        assertThat(res.email()).isEqualTo(usuarioPersistido.getEmail());
        assertThat(res.isActive()).isTrue();
        assertThat(res.name()).isEqualTo(usuarioPersistido.getName());
        assertThat(res.password()).isEmpty();
        assertThat(res.phones().size()).isEqualTo(1);
        assertThat(res.token()).isEqualTo(token);
    }

}
