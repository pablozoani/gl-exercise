package com.example.gl_exercise.message;

import java.util.List;

public record ApiErrorResponse(List<Error> error) {
    public record Error(String timestamp, Integer code, String detail) {}
}
