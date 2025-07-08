package com.example.gl_exercise.message;

import com.example.gl_exercise.validation.UserPassword;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record SignUpRequest(
    @Nullable String username,
    @NotNull @Email String email,
    @UserPassword String password,
    @Nullable List<@NotNull @Valid Phone> phones
) {
    public record Phone(
        @NotNull Long number,
        @NotNull Integer cityCode,
        @NotBlank String countryCode
    ) {
    }
}
