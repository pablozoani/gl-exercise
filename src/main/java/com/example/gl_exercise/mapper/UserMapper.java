package com.example.gl_exercise.mapper;

import com.example.gl_exercise.message.LoginResponse;
import com.example.gl_exercise.message.SignUpRequest;
import com.example.gl_exercise.message.SignUpResponse;
import com.example.gl_exercise.model.Phone;
import com.example.gl_exercise.model.User;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class UserMapper {

    public static final DateTimeFormatter DT_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm:ss a");

    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserMapper(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public User toEntity(@NotNull SignUpRequest signUpRequest) {
        String encoded = this.passwordEncoder.encode(signUpRequest.password());

        User user = new User(signUpRequest.email(), encoded, signUpRequest.username());

        if (signUpRequest.phones() != null) {
            signUpRequest.phones()
                .stream()
                .map(p -> new Phone(user, p.number(), p.cityCode(), p.countryCode()))
                // Won't trigger a database query because "user" is not managed.
                .forEach(phone -> user.getPhones().add(phone));
        }

        return user;
    }

    public SignUpResponse toSignUpResponse(@NotNull User user, String token) {
        return new SignUpResponse(
            user.getId().toString(),
            user.getCreatedAt().format(DT_FORMATTER),
            user.getLastLogin().format(DT_FORMATTER),
            token,
            user.getIsActive()
        );
    }

    public LoginResponse toLoginResponse(User user, String token) {
        List<LoginResponse.Phone> phones = user.getPhones()
            .stream()
            .map(p -> new LoginResponse.Phone(p.getNumber(), p.getCityCode(), p.getCountryCode()))
            .toList();

        return new LoginResponse(
            user.getId().toString(),
            user.getCreatedAt().format(DT_FORMATTER),
            user.getLastLogin().format(DT_FORMATTER),
            token,
            user.getIsActive(),
            user.getName(),
            user.getEmail(),
            user.getPassword(),
            phones
        );
    }
}
