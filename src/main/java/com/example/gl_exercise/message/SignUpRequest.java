package com.example.gl_exercise.message;

import com.example.gl_exercise.validation.UserPassword;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Solicitud que se recibe en el endpoint /sign-up.
 *
 * @param username nombre de usuario, opcional.
 * @param email    correo electrónico del usuario, bien formateado.
 * @param password contraseña del usuario, se deben cumplir las reglas por @UserPassword.
 * @param phones   Lista de teléfonos, opcional.
 */
public record SignUpRequest(
    @Nullable String username,
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    String email,
    @UserPassword String password,
    @Nullable List<@NotNull(message = "A phone cannot be null") @Valid Phone> phones
) {

    /**
     * Teléfono del usuario.
     *
     * @param number número de teléfono.
     * @param cityCode código de la ciudad.
     * @param countryCode código del país.
     */
    public record Phone(
        @NotNull Long number,
        @NotNull Integer cityCode,
        @NotBlank String countryCode
    ) {
    }
}
