package com.example.gl_exercise.message;

import java.util.List;

public record SignUpResponse(
    String id,
    String created,
    String lastLogin,
    String token,
    Boolean isActive
) {
}
