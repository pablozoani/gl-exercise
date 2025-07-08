package com.example.gl_exercise.validation;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserPasswordValidatorTest {

    UserPasswordValidator validator = new UserPasswordValidator();

    // Short test case for demo purposes.
    @Test
    void isValid() {
        var invalidInputs = new String[]{
            null, "short", "toolongpassword", "noupper12", "No2numbers", "sp3ci4lChar!"
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