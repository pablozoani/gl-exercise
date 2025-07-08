package com.example.gl_exercise.validation;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UserPasswordValidator.class)
public @interface UserPassword {

    String message() default "Password must contain exactly 1 uppercase letter, 2 numbers, other " +
        "characters must be lowercase, and length between 8-12 characters";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}

class UserPasswordValidator implements ConstraintValidator<UserPassword, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.length() < 8 || value.length() > 12) {
            return false;
        }

        boolean oneUppercase = false;
        int numberCount = 0;

        for (char c : value.toCharArray()) {
            if (c >= 'A' && c <= 'Z') {
                if (oneUppercase) {
                    return false;
                }

                oneUppercase = true;
            } else if (c >= '0' && c <= '9') {
                if (numberCount == 2) {
                    return false;
                }

                numberCount++;
            } else if (c < 'a' || c > 'z') {
                return false;
            }
        }

        return oneUppercase && numberCount == 2;
    }

}
