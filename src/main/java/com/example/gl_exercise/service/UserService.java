package com.example.gl_exercise.service;

import com.example.gl_exercise.exception.UserAlreadyExistsException;
import com.example.gl_exercise.exception.UserNotFoundException;
import com.example.gl_exercise.mapper.UserMapper;
import com.example.gl_exercise.message.LoginResponse;
import com.example.gl_exercise.message.SignUpRequest;
import com.example.gl_exercise.message.SignUpResponse;
import com.example.gl_exercise.model.User;
import com.example.gl_exercise.repository.UserRepository;
import com.example.gl_exercise.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Servicio que maneja la autenticación de usuarios, incluyendo login y registro.
 * Se encarga de: Validar credenciales, generar tokens JWT y registrar nuevos usuarios verificando duplicados.
 */
@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;

    @Autowired
    public UserService(UserRepository userRepository, UserMapper userMapper, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Realiza el proceso de login para un usuario dado su email. Busca al usuario por email, genera un token,
     * actualiza la fecha de último login, guarda los cambios y retorna la respuesta.
     *
     * @param email El correo electrónico del usuario que intenta hacer login
     * @return LoginResponse con los datos del usuario y el token JWT generado
     * @throws UserNotFoundException si no existe un usuario con el email proporcionado
     */
    public LoginResponse login(String email) {
        log.info("User login -> {}", email);

        Optional<User> userOptional = this.userRepository.findByEmail(email);

        if (userOptional.isEmpty())
            throw new UserNotFoundException("User with email " + email + " not found");

        User detached = userOptional.get(); // findByEmail devuelve una entidad detached.

        String token = this.jwtUtil.generateUserToken(detached);

        detached.setLastLogin(LocalDateTime.now());

        // UserRepository::save también carga los teléfonos (que va a usar el mapper)
        User savedAndDetached = this.userRepository.save(detached);

        LoginResponse output = this.userMapper.toLoginResponse(savedAndDetached, token);

        log.info("Logged in user -> {}", output);

        return output;
    }

    /**
     * Registra un nuevo usuario y genera su token JWT para la próxima llamada. Se hace flush enseguida
     * para que se arroje una excepción al estar el email duplicado.
     *
     * @param signUpRequest Datos de registro del usuario
     * @return SignUpResponse con los datos del usuario y su token
     * @throws UserAlreadyExistsException si el email ya está en uso
     */
    //@Transactional(isolation = Isolation.READ_COMMITTED) // No hace falta
    public SignUpResponse signUpUser(SignUpRequest signUpRequest) {
        log.info("Signing up user -> {}", signUpRequest);

        User transientUser = this.userMapper.toEntity(signUpRequest);

        User savedUser;

        try {
            // Flush asi sincroniza a la db y tira error en caso de usuario duplicado
            savedUser = this.userRepository.saveAndFlush(transientUser);
        } catch (DataIntegrityViolationException e) {
            Throwable root = e.getRootCause();

            if (root != null &&
                root.getMessage() != null &&
                root.getMessage().toLowerCase().contains("unique_user_email")
            ) {
                throw new UserAlreadyExistsException("User email " + transientUser.getEmail() + " already in use");
            }

            throw e;
        }

        String token = this.jwtUtil.generateUserToken(savedUser);

        SignUpResponse signUpResponse = this.userMapper.toSignUpResponse(savedUser, token);

        log.info("Signed up user -> {}", signUpResponse);

        return signUpResponse;
    }

}
