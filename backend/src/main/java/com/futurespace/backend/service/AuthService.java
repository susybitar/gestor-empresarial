package com.futurespace.backend.service;

import com.futurespace.backend.exception.BusinessException;
import com.futurespace.backend.model.dto.AuthResponseDTO;
import com.futurespace.backend.model.dto.LoginRequestDTO;
import com.futurespace.backend.model.dto.RegisterRequestDTO;
import com.futurespace.backend.model.dto.ResetPasswordRequestDTO;
import com.futurespace.backend.model.entities.User;
import com.futurespace.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

/**
 * Servicio de autenticación para administradores.
 * Se centra exclusivamente en el proceso de validación de acceso (Login),
 * siguiendo principios de seguridad corporativa y Clean Code.
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private static final String INVALID_CREDENTIALS_MESSAGE = "Operación no permitida: credenciales incorrectas";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Valida las credenciales de acceso de un administrador.
     * @param dto Datos de acceso (email y password).
     * @return AuthResponseDTO con los datos básicos del usuario y mensaje de éxito.
     */
    @Transactional(readOnly = true)
    public AuthResponseDTO login(LoginRequestDTO dto) {
        validateLoginInput(dto);

        String email = normalizeEmail(dto.getEmail());

        // Buscamos el usuario por email. Si no existe, lanzamos excepción genérica por seguridad.
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Acceso rechazado: email no encontrado {}", email);
                    return new BusinessException(INVALID_CREDENTIALS_MESSAGE);
                });

        // Verificamos si la contraseña coincide con el hash almacenado
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            log.warn("Acceso rechazado: contraseña incorrecta para email {}", email);
            throw new BusinessException(INVALID_CREDENTIALS_MESSAGE);
        }

        log.info("Usuario autenticado correctamente: id={} email={}", user.getIdUser(), user.getEmail());
        return createResponse(user, "Login correcto");
    }

    /**
     * Registra un nuevo usuario administrador en el sistema.
     * @param dto Datos del nuevo usuario.
     * @return AuthResponseDTO con los datos del usuario creado.
     */
    @Transactional
    public AuthResponseDTO register(RegisterRequestDTO dto) {
        String email = normalizeEmail(dto.getEmail());

        if (userRepository.findByEmail(email).isPresent()) {
            throw new BusinessException("El email ya está registrado");
        }

        User newUser = new User();
        newUser.setEmail(email);
        // CRÍTICO: Aquí es donde ocurre el hasheo automático
        newUser.setPassword(passwordEncoder.encode(dto.getPassword()));
        newUser.setRole("ADMIN");
        newUser.setCreatedAt(java.time.LocalDate.now());

        User savedUser = userRepository.save(newUser);
        log.info("Nuevo administrador registrado: id={} email={}", savedUser.getIdUser(), email);

        return createResponse(savedUser, "Registro completado con éxito");
    }

    /**
     * Resetea la contraseña de un usuario administrador.
     * @param dto Datos con email y nueva contraseña.
     * @return AuthResponseDTO con el estado de la operación.
     */
    @Transactional
    public AuthResponseDTO resetPassword(ResetPasswordRequestDTO dto) {
        String email = normalizeEmail(dto.getEmail());

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Usuario no encontrado"));

        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
            throw new BusinessException(INVALID_CREDENTIALS_MESSAGE);
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);

        log.info("Contraseña reseteada con éxito para: {}", email);
        return createResponse(user, "Contraseña actualizada correctamente");
    }

    // --- Helpers Privados ---

    private void validateLoginInput(LoginRequestDTO dto) {
        if (dto == null || isBlank(dto.getEmail()) || isBlank(dto.getPassword())) {
            throw new BusinessException(INVALID_CREDENTIALS_MESSAGE);
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    private AuthResponseDTO createResponse(User user, String message) {
        AuthResponseDTO dto = new AuthResponseDTO();
        dto.setIdUser(user.getIdUser());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setMessage(message);
        return dto;
    }
}