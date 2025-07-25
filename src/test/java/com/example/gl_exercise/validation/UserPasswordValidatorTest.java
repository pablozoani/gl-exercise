package com.example.gl_exercise.validation;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserPasswordValidatorTest {

    UserPasswordValidator validator = new UserPasswordValidator();

    // Cada uno de los casos en los arreglos.
    @Test
    void isValid() {
        var invalidInputs = new String[]{
            null, // null
            "short", // muy corta
            "toolongpassword", // muy larga
            "noupper12", // sin mayúsculas
            "MaNyUpp12", // muchas mayúsculas
            "Abcdefgh", // sin números
            "Abcdefg1", // pocos números
            "Abcdef123", // demasiados números
            "Abcdef12@", // caracteres especiales
            "Abc def12" // espacios en blanco
        };

        for (String s : invalidInputs) {
            assertThat(validator.isValid(s, null)).isFalse();
        }

        var validInputs = new String[]{
            "password12A", "89Zpassword", "Apassword89"
        };

        for (String s : validInputs) {
            assertThat(validator.isValid(s, null)).isTrue();
        }
    }

}
