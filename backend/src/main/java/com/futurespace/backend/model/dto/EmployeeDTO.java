package com.futurespace.backend.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

/**
 * Objeto de transferencia de datos (DTO) para la entidad de empleados.
 * Encapsula la información del capital humano con validaciones de integridad de negocio.
 */
@Data
public class EmployeeDTO {

    /** Identificador único secuencial. */
    private Integer idEmployee;

    /** Número de Identificación Fiscal (8 dígitos + letra). */
    @NotBlank(message = "El NIF es un campo obligatorio")
    @Pattern(regexp = "^[0-9]{8}[A-Za-z]$", message = "El NIF debe tener 8 números y una letra final (ej: 12345678Z)")
    private String nif;

    /** Nombre de pila del empleado. */
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 30, message = "El nombre debe tener entre 2 y 30 caracteres")
    private String firstName;

    /** Primer apellido. */
    @NotBlank(message = "El primer apellido es obligatorio")
    @Size(min = 2, max = 40, message = "El apellido debe tener entre 2 y 40 caracteres")
    private String lastName;

    /** Segundo apellido. */
    @NotBlank(message = "El segundo apellido es obligatorio")
    @Size(min = 2, max = 40, message = "El apellido debe tener entre 2 y 40 caracteres")
    private String secondLastName;

    /** Fecha de nacimiento del empleado. */
    @NotNull(message = "La fecha de nacimiento es obligatoria")
    @Past(message = "La fecha de nacimiento debe ser una fecha pasada")
    private LocalDate birthDate;

    /** Teléfono de contacto principal. */
    @NotBlank(message = "El teléfono principal es obligatorio")
    @Pattern(regexp = "^[0-9]{9,12}$", message = "El teléfono debe tener entre 9 y 12 dígitos numéricos")
    private String phone1;

    /** Teléfono de contacto secundario o de emergencia. */
    @NotBlank(message = "El teléfono secundario es obligatorio")
    @Pattern(regexp = "^[0-9]{9,12}$", message = "El teléfono debe tener entre 9 y 12 dígitos numéricos")
    private String phone2;

    /** Dirección de correo electrónico corporativo. */
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Debe introducir una dirección de correo válida")
    @Size(max = 40, message = "El email no puede superar los 40 caracteres")
    private String email;

    /** Fecha de alta oficial en la compañía. */
    @NotNull(message = "La fecha de alta es obligatoria")
    @PastOrPresent(message = "La fecha de alta no puede ser una fecha futura")
    private LocalDate hireDate;

    /** Fecha de rescisión de contrato (Solo lectura). */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDate terminationDate;

    /** Estado civil: [S]oltero, [C]asado. */
    @NotBlank(message = "El estado civil es obligatorio")
    @Pattern(regexp = "^[SC]$")
    private String maritalStatus;

    /** Indicador de titulación superior: [S]í, [N]o. */
    @NotBlank(message = "La formación académica es obligatoria")
    @Pattern(regexp = "^[SN]$")
    private String hasUniversityEducation;
}
