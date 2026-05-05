package com.futurespace.backend.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO para la solicitud de reseteo de contraseña.
 */
@Data
public class ResetPasswordRequestDTO {

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Formato de email inválido")
    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@futurespace\\.com$", message = "Solo se permiten correos corporativos @futurespace.com")
    private String email;

    @NotBlank(message = "La contraseña actual es obligatoria")
    private String currentPassword;

    @NotBlank(message = "La nueva contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    @Pattern(
        regexp = "^(?=.*[A-Z])(?=.*[!@#\\$%^&*(),.?\":{}|<>]).*$",
        message = "La contraseña debe contener al menos una mayúscula y un carácter especial"
    )
    private String newPassword;

}
