package com.example.gl_exercise.controller;

import com.example.gl_exercise.mapper.UserMapper;
import com.example.gl_exercise.message.ApiErrorResponse;
import com.example.gl_exercise.message.LoginResponse;
import com.example.gl_exercise.message.SignUpRequest;
import com.example.gl_exercise.message.SignUpResponse;
import com.example.gl_exercise.model.User;
import com.example.gl_exercise.repository.UserRepository;
import com.example.gl_exercise.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

// Pruebas de punta a punta con un WebClient.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class AuthControllerTest {

    @LocalServerPort
    int port;

    @Autowired
    UserRepository userRepository;

    @Autowired
    JwtUtil jwtUtil;

    @Value("${jwt.secret}")
    String jwtSecret;

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

    // Usuario correcto. Debe retornar 201 Created con todos los campos válidos y con su formato correcto.
    @Test
    void signUp() { // Happy path
        SignUpResponse response = restClient.post()
            .uri("/sign-up")
            .body(request)
            .retrieve()
            .body(SignUpResponse.class);

        assertThat(response.token()).isNotBlank();
        Claims claims = jwtUtil.parseToken(response.token());
        assertThat(claims.getSubject()).isEqualTo(request.email());
        assertThat(response.id()).isNotBlank();
        assertThat(UUID.fromString(response.id())).isNotNull();
        assertThat(response.isActive()).isTrue();
        assertThat(response.created()).isNotBlank();
        assertThat(UserMapper.DT_FORMATTER.parse(response.created())).isNotNull();
        assertThat(response.lastLogin()).isNotBlank();
        assertThat(UserMapper.DT_FORMATTER.parse(response.lastLogin())).isNotNull();
    }

    // Un usuario no puede ingresar un email que ya se encuentra registrado.
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

    // Un usuario no puede registrarse con una contraseña invalida.
    // Las pruebas de contraseñas se encuentran en la pruebas del password validator.
    @Test
    void signUpInvalidPassword() {
        RestClientResponseException exc = assertThrows(RestClientResponseException.class, () -> {
            restClient.post()
                .uri("/sign-up")
                .body(new SignUpRequest("", "example@email.com", "invalid", Collections.emptyList()))
                .retrieve()
                .toBodilessEntity();
        });

        assertThat(exc.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        ApiErrorResponse errorResponse = exc.getResponseBodyAs(ApiErrorResponse.class);

        for (var err : errorResponse.error()) {
            assertThat(err.detail().toLowerCase()).contains("password");
        }
    }

    // Un usuario no puede registrarse con un email cuyo formato no es valido.
    @Test
    void signUpInvalidEmail() {
        RestClientResponseException exc = assertThrows(RestClientResponseException.class, () -> {
            restClient.post()
                .uri("/sign-up")
                .body(new SignUpRequest("", "@email?", "Isvalid12", Collections.emptyList()))
                .retrieve()
                .toBodilessEntity();
        });

        assertThat(exc.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        ApiErrorResponse errorResponse = exc.getResponseBodyAs(ApiErrorResponse.class);

        for (var err : errorResponse.error()) {
            assertThat(err.detail().toLowerCase()).contains("email");
        }
    }

    // Si hay telefonos, estos no pueden ser nulos.
    @Test
    void signUpNullPhone() {
        RestClientResponseException exc = assertThrows(RestClientResponseException.class, () -> {
            var phones = new ArrayList<SignUpRequest.Phone>();
            phones.add(null);
            var body = new SignUpRequest(request.username(), request.email(), request.password(), phones);
            restClient.post()
                .uri("/sign-up")
                .body(body)
                .retrieve()
                .toBodilessEntity();
        });

        assertThat(exc.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        ApiErrorResponse errorResponse = exc.getResponseBodyAs(ApiErrorResponse.class);

        for (var err : errorResponse.error()) {
            assertThat(err.detail().toLowerCase()).contains("null", "phone");
        }
    }

    // Para el login se debe utilizar un token producido por el sign up.
    // Se verifican los campos de salida de un login satisfactorio.
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
        assertThat(loginResponse.email()).isEqualTo(request.email());
        assertThat(loginResponse.phones().size()).isEqualTo(1);
        assertThat(loginResponse.created()).isEqualTo(signUpResponse.created());
        assertThat(UserMapper.DT_FORMATTER.parse(loginResponse.lastLogin())).isNotNull();
        assertThat(LocalDateTime.parse(loginResponse.lastLogin(), UserMapper.DT_FORMATTER))
            .isAfterOrEqualTo(LocalDateTime.parse(signUpResponse.lastLogin(), UserMapper.DT_FORMATTER));
        assertThat(loginResponse.name()).isEqualTo(request.username());
        Date previousIssuedAt = jwtUtil.parseToken(signUpResponse.token()).getIssuedAt();
        Claims newClaims = jwtUtil.parseToken(loginResponse.token());
        Date newIssuedAt = newClaims.getIssuedAt();
        assertThat(newIssuedAt).isAfterOrEqualTo(previousIssuedAt);
    }

    // Un login si el encabezado Authorization no es autorizado.
    @Test
    void loginWithoutHeaderForbidden() {
        RestClientResponseException responseException = assertThrows(RestClientResponseException.class, () -> {
            restClient.get()
                .uri("/login")
                .retrieve()
                .toBodilessEntity();
        });

        assertThat(responseException.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    // En caso de que un token haya expirado, también hay que devolver forbidden.
    @Test
    void
    loginWithExpiredToken() throws InterruptedException {
        JwtUtil jwtUtil2 = new JwtUtil(jwtSecret, 1);
        User u = new User("example@email.com", "password", "username");
        ReflectionTestUtils.setField(u, "id", UUID.randomUUID()); // JwtUtil Checks for managed users.
        String token = jwtUtil2.generateUserToken(u);

        Thread.sleep(1001L); // Token expires in one second

        RestClientResponseException responseException = assertThrows(RestClientResponseException.class, () -> {
            restClient.get()
                .uri("/login")
                .header("Authorization", "Bearer ".concat(token))
                .retrieve()
                .toBodilessEntity();
        });

        assertThat(responseException.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        ApiErrorResponse errorResponse = responseException.getResponseBodyAs(ApiErrorResponse.class);

        for (var err : errorResponse.error()) {
            assertThat(err.detail().toLowerCase()).contains("jwt", "expired");
        }
    }

    // Caso en el que el token contenga un usuario eliminado.
    // Devuelve 404 not found.
    @Test
    void loginWithNonExistingUser() {
        User u = new User("example@email.com", "password", "username");
        ReflectionTestUtils.setField(u, "id", UUID.randomUUID()); // JwtUtil Checks for managed users.
        String token = jwtUtil.generateUserToken(u);

        RestClientResponseException responseException = assertThrows(RestClientResponseException.class, () -> {
            restClient.get()
                .uri("/login")
                .header("Authorization", "Bearer ".concat(token))
                .retrieve()
                .toBodilessEntity();
        });

        assertThat(responseException.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        ApiErrorResponse errorResponse = responseException.getResponseBodyAs(ApiErrorResponse.class);

        for (var err : errorResponse.error()) {
            assertThat(err.detail().toLowerCase()).contains("user", "not", "found");
        }
    }

}