package com.example.gl_exercise.service;

import com.example.gl_exercise.exception.UserAlreadyExistsException;
import com.example.gl_exercise.mapper.UserMapper;
import com.example.gl_exercise.message.SignUpRequest;
import com.example.gl_exercise.message.SignUpResponse;
import com.example.gl_exercise.repository.UserRepository;
import com.example.gl_exercise.util.JwtUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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

    @Test
    void signUpUser() {
        SignUpResponse signUpResponse = userService.signUpUser(signUpRequest);
        assertThat(signUpResponse.id()).isNotNull();
    }

    @Test
    void signUpUserDuplicateEmail() {
        userService.signUpUser(signUpRequest);
        Assertions.assertThrows(UserAlreadyExistsException.class, () -> userService.signUpUser(signUpRequest));
    }
}