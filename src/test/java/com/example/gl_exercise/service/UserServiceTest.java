package com.example.gl_exercise.service;

import com.example.gl_exercise.mapper.UserMapper;
import com.example.gl_exercise.message.SignUpRequest;
import com.example.gl_exercise.message.SignUpResponse;
import com.example.gl_exercise.model.Phone;
import com.example.gl_exercise.model.User;
import com.example.gl_exercise.repository.UserRepository;
import com.example.gl_exercise.util.JwtUtil;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository mockUserRepository;

    @Mock
    private UserMapper mockUserMapper;

    @Mock
    private JwtUtil mockJwtUtil;

    @Mock
    private EntityManager entityManager;

    private UserService userServiceUnderTest;

    @BeforeEach
    void setUp() {
        userServiceUnderTest = new UserService(mockUserRepository, mockUserMapper, mockJwtUtil, entityManager);
    }

    // Short test case for demo purposes.
    @Test
    void testSignUpUser() {
        User unmanaged = new User("email", "password", "name");

        unmanaged.getPhones().add(new Phone(unmanaged, 123456789L, 1234, "countryCode"));

        when(mockUserMapper.toEntity(any(SignUpRequest.class))).thenReturn(unmanaged);

        final SignUpRequest signUpRequest = new SignUpRequest(
            "name",
            "email",
            "password",
            List.of(new SignUpRequest.Phone(123456789L, 1234, "countryCode"))
        );

        User managed = new User("email", "password", "name");

        // Hibernate fields
        UUID userId = UUID.randomUUID();
        ReflectionTestUtils.setField(managed, "id", userId);

        LocalDateTime createdAt = LocalDateTime.of(2023, 1, 1, 12, 0);
        ReflectionTestUtils.setField(managed, "createdAt", createdAt);

        LocalDateTime lastLogin = LocalDateTime.of(2023, 1, 2, 12, 0);
        ReflectionTestUtils.setField(managed, "lastLogin", lastLogin);

        ReflectionTestUtils.setField(managed, "isActive", true);

        managed.getPhones().add(new Phone(managed, 123456789L, 1234, "countryCode"));

        when(mockUserRepository.saveAndFlush(unmanaged)).thenReturn(managed);

        when(mockJwtUtil.generateUserToken(managed)).thenReturn("token");

        final SignUpResponse signUpResponse = new SignUpResponse(
            userId.toString(),
            createdAt.format(UserMapper.DT_FORMATTER),
            lastLogin.format(UserMapper.DT_FORMATTER),
            "token",
            true
        );

        when(mockUserMapper.toSignUpResponse(any(User.class), anyString()))
            .thenReturn(signUpResponse);

        final SignUpResponse result = userServiceUnderTest.signUpUser(signUpRequest);

        assertThat(result).isEqualTo(signUpResponse);
        verify(mockUserMapper, times(1)).toEntity(any());
        verify(mockUserRepository, times(1)).saveAndFlush(any());
        verify(mockJwtUtil, times(1)).generateUserToken(any());
        verify(mockUserMapper, times(1)).toSignUpResponse(any(), anyString());
    }
}
