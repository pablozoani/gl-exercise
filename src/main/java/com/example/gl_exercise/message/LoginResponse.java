package com.example.gl_exercise.message;

import java.util.List;

/**
 * Respuesta para del endpoint /login.
 *
 * @param id        ID del usuario en la base de datos.
 * @param created   fecha de registro del usuario.
 * @param lastLogin fecha de último inicio de sesión, actualizada antes de generar esta respuesta.
 * @param token     token con el que el cliente puede hacer otra llamada a una ruta protegida.
 * @param isActive  si el usuario está activo o no.
 * @param name      nombre del usuario.
 * @param email     correo electrónico del usuario.
 * @param password  contraseña del usuario. El mapper le asigna una contraseña vacía.
 * @param phones    lista de teléfonos del usuario en la base de datos.
 */
public record LoginResponse(
    String id,
    String created,
    String lastLogin,
    String token,
    Boolean isActive,
    String name,
    String email,
    String password,
    List<Phone> phones
) {

    /**
     * Teléfono del usuario.
     *
     * @param number      número de teléfono.
     * @param citycode    código de la ciudad.
     * @param countrycode código del país.
     */
    public record Phone(
        Long number,
        Integer citycode,
        String countrycode
    ) {}
}
