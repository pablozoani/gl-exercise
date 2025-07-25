package com.example.gl_exercise.message;

import java.util.List;

/**
 * Objeto de respuesta para los errores 4xx y 5xx.
 * @param error lista de errores tal como se describe en el trabajo de Global Logic.
 */
public record ApiErrorResponse(List<Error> error) {
    /**
     * Objeto de error con la información para el cliente.
     *
     * @param timestamp fecha del error.
     * @param code código del error. El examen no especifica como asignar este valor, por ahora lo dejo con el código
     *             del http status.
     * @param detail información sobre el error para el cliente.
     */
    public record Error(String timestamp, Integer code, String detail) {}
}
