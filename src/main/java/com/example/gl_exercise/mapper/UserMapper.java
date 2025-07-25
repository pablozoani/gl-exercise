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

/**
 * Mapper responsable de la conversión entre objetos de dominio User y DTOs relacionados.
 */
@Component
public class UserMapper {

    // Formato especificado en el trabajo de Global Logic
    public static final DateTimeFormatter DT_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm:ss a");

    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserMapper(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Convierte un SignUpRequest en una entidad User, incluyendo codificación de la contraseña y mapeo
     * de teléfonos asociados (si existen).
     *
     * @param signUpRequest DTO con datos de registro del usuario (no null)
     * @return Entidad User (no persistida) con los datos mapeados
     */
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

    /**
     * Convierte una entidad User en un DTO de respuesta para el endpoint de registro.
     *
     * @param user  Entidad del usuario a convertir (no null), debe tener ID
     * @param token Token JWT generado para el usuario
     * @return DTO con los datos básicos del usuario y su token
     */
    public SignUpResponse toSignUpResponse(@NotNull User user, String token) {
        return new SignUpResponse(
            user.getId().toString(),
            user.getCreatedAt().format(DT_FORMATTER),
            user.getLastLogin().format(DT_FORMATTER),
            token,
            user.getIsActive()
        );
    }

    /**
     * Convierte una entidad User en un DTO de respuesta para login.
     * Incluye información detallada del perfil y teléfonos asociados.
     *
     * @param user  Entidad del usuario a convertir
     * @param token Token JWT generado para el usuario
     * @return DTO con los datos completos del usuario y su token
     */
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
            "",
            phones
        );
    }
}
