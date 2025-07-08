package com.example.gl_exercise.service;

import com.example.gl_exercise.exception.UserAlreadyExistsException;
import com.example.gl_exercise.exception.UserNotFoundException;
import com.example.gl_exercise.mapper.UserMapper;
import com.example.gl_exercise.message.LoginResponse;
import com.example.gl_exercise.message.SignUpRequest;
import com.example.gl_exercise.message.SignUpResponse;
import com.example.gl_exercise.model.User;
import com.example.gl_exercise.repository.UserRepository;
import com.example.gl_exercise.util.JwtUtil;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final EntityManager entityManager;

    @Autowired
    public UserService(UserRepository userRepository, UserMapper userMapper, JwtUtil jwtUtil, EntityManager entityManager) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.jwtUtil = jwtUtil;
        this.entityManager = entityManager;
    }

    public boolean existsByEmail(String email) {
        return this.userRepository.existsByEmail(email);
    }

    public LoginResponse login(String email) {
        Optional<User> userOptional = this.userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            throw new UserNotFoundException("User with email " + email + " not found");
        }

        User user = userOptional.get();

        String token = this.jwtUtil.generateUserToken(user);

        user.setLastLogin(LocalDateTime.now());

        try {
            Thread.sleep(2000L);
        } catch (InterruptedException e) {

        }

        this.userRepository.save(user);

        return this.userMapper.toLoginResponse(user, token);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public SignUpResponse signUpUser(SignUpRequest signUpRequest) {
        log.info("Signing up user -> {}", signUpRequest);

        User unmanaged = this.userMapper.toEntity(signUpRequest);

        User managed;

        try {
            managed = this.userRepository.saveAndFlush(unmanaged);
        } catch (DataIntegrityViolationException e) {
            Throwable root = e.getRootCause();

            if (root != null &&
                root.getMessage() != null &&
                root.getMessage().toLowerCase().contains("unique_user_email")
            ) {
                throw new UserAlreadyExistsException("User email " + unmanaged.getEmail() + " already in use");
            }

            throw e;
        }

        String token = this.jwtUtil.generateUserToken(managed);

        SignUpResponse signUpResponse = this.userMapper.toSignUpResponse(managed, token);

        log.info("Signed up user -> {}", signUpResponse);

        return signUpResponse;
    }
}
