package com.example.gl_exercise.mapper;

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

@ExtendWith(MockitoExtension.class)
class UserMapperTest {

    @Mock
    private PasswordEncoder mockPasswordEncoder;

    private UserMapper userMapper;

    @BeforeEach
    void setUp() {
        userMapper = new UserMapper(mockPasswordEncoder);
    }

    // Short test case for demo purposes.
    @Test
    void testToEntity() {
        when(mockPasswordEncoder.encode(anyString())).thenReturn("@#$p455w0rd");

        final var signUpRequest = new SignUpRequest(
            "example",
            "example@email",
            "rawPassword",
            List.of(new SignUpRequest.Phone(123456789L, 1_000, "54"))
        );

        final User result = userMapper.toEntity(signUpRequest);

        assertThat(result.getName()).isEqualTo("example");
        assertThat(result.getEmail()).isEqualTo("example@email");
        assertThat(result.getPassword()).isEqualTo("@#$p455w0rd");
        assertThat(result.getPhones().size()).isEqualTo(1);
        result.getPhones().forEach(p -> {
            assertThat(p.getUser().getEmail()).isEqualTo("example@email");
            assertThat(p.getNumber()).isEqualTo(123456789L);
            assertThat(p.getCityCode()).isEqualTo(1_000);
            assertThat(p.getCountryCode()).isEqualTo("54");
        });

        verify(mockPasswordEncoder, times(1)).encode("rawPassword");
    }

    // Short test case for demo purposes.
    @Test
    void testToSignUpResponse() {
        final User user = new User("example@email", "password", "example");
        user.getPhones().add(new Phone(user, 123456789L, 1_000, "54"));

        // Hibernate fields
        UUID userId = UUID.randomUUID();
        ReflectionTestUtils.setField(user, "id", userId);

        LocalDateTime createdAt = LocalDateTime.of(2023, 1, 1, 12, 0);
        ReflectionTestUtils.setField(user, "createdAt", createdAt);

        LocalDateTime lastLogin = LocalDateTime.of(2023, 1, 2, 12, 0);
        ReflectionTestUtils.setField(user, "lastLogin", lastLogin);

        ReflectionTestUtils.setField(user, "isActive", true);

        final SignUpResponse result = userMapper.toSignUpResponse(user, "token");

        assertThat(result.id()).isEqualTo(userId.toString());
        assertThat(result.created()).isEqualTo(createdAt.format(UserMapper.DT_FORMATTER));
        assertThat(result.lastLogin()).isEqualTo(lastLogin.format(UserMapper.DT_FORMATTER));
        assertThat(result.isActive()).isTrue();
    }
}
