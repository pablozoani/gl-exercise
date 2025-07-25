package com.example.gl_exercise.message;

/**
 * Respuesta exitosa del endpoint /sign-up.
 *
 * @param id ID del usuario recientemente creado.
 * @param created fecha de creación del usuario.
 * @param lastLogin fecha de último inicio de sesión.
 * @param token token generado para el próximo inicio de sesión.
 * @param isActive si el usuario está activo o no.
 */
public record SignUpResponse(
    String id,
    String created,
    String lastLogin,
    String token,
    Boolean isActive
) {
}
