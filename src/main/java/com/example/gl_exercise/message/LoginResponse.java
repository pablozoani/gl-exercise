package com.example.gl_exercise.message;

import java.util.List;

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
    public record Phone(
        Long number,
        Integer citycode,
        String countrycode
    ) {}
}
