package com.example.gl_exercise.controller;

import com.example.gl_exercise.message.ApiErrorResponse;
import com.example.gl_exercise.message.LoginResponse;
import com.example.gl_exercise.message.SignUpRequest;
import com.example.gl_exercise.message.SignUpResponse;
import com.example.gl_exercise.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS) // Needed when using rest client, not services.
class AuthControllerTest {

    @LocalServerPort
    int port;

    @Autowired
    UserRepository userRepository;

    RestClient restClient;

    final SignUpRequest request = new SignUpRequest(
        "example",
        "example@email",
        "passwordA12",
        List.of(new SignUpRequest.Phone(123456789L, 1234, "54"))
    );

    @BeforeEach
    void setup() {
        restClient = RestClient.builder()
            .baseUrl("http://localhost:" + port)
            .build();

        userRepository.deleteAll();
    }

    // Short test case for demo purposes.
    @Test
    void signUp() { // Happy path
        SignUpResponse response = restClient.post()
            .uri("/sign-up")
            .body(request)
            .retrieve()
            .body(SignUpResponse.class);

        assertThat(response.token()).isNotBlank();
        assertThat(response.created()).isNotBlank();
    }

    @Test
    void signUpDuplicateUser() {
        restClient.post()
            .uri("/sign-up")
            .body(request)
            .retrieve()
            .toBodilessEntity();

        var exc = assertThrows(RestClientResponseException.class, () -> {
            restClient.post()
                .uri("/sign-up")
                .body(request)
                .retrieve()
                .toBodilessEntity();
        });

        ApiErrorResponse errorResponse = exc.getResponseBodyAs(ApiErrorResponse.class);

        assertThat(exc.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(errorResponse.error().size()).isEqualTo(1);
        assertThat(errorResponse.error().get(0).code()).isEqualTo(HttpStatus.CONFLICT.value());
    }

    @Test
    void login() {
        ResponseEntity<SignUpResponse> response = restClient.post()
            .uri("/sign-up")
            .body(request)
            .retrieve()
            .toEntity(SignUpResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        SignUpResponse signUpResponse = response.getBody();

        ResponseEntity<LoginResponse> responseEntity = restClient.get()
            .uri("/login")
            .header("Authorization", "Bearer " + signUpResponse.token())
            .retrieve()
            .toEntity(LoginResponse.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        LoginResponse loginResponse = responseEntity.getBody();

        assertThat(loginResponse.id()).isEqualTo(signUpResponse.id());
        assertThat(loginResponse.phones().size()).isEqualTo(1);
    }

    @Test
    void loginForbidden() {
        RestClientResponseException responseException = assertThrows(RestClientResponseException.class, () -> {
            restClient.get()
                .uri("/login")
                .retrieve()
                .toBodilessEntity();
        });

        assertThat(responseException.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}