package com.futurespace.backend.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/**
 * Objeto de transferencia de datos (DTO) para la entidad de proyectos.
 * Representa la unidad operativa de negocio y sus metadatos temporales y geográficos.
 */
@Data
public class ProjectDTO {

    /** Identificador único secuencial del proyecto. */
    private Integer idProject;

    /** Descripción detallada o nombre del proyecto corporativo. */
    @NotBlank(message = "El nombre del proyecto es obligatorio")
    @Size(max = 125, message = "El nombre del proyecto no puede superar los 125 caracteres")
    private String description;

    /** Fecha de inicio de las operaciones del proyecto. */
    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDate startDate;

    /** Fecha prevista de finalización. */
    @NotNull(message = "La fecha de finalización es obligatoria")
    private LocalDate endDate;

    /** Fecha de baja técnica o cancelación (Solo lectura). */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDate terminationDate;

    /** Ubicación física o centro de coste asociado. */
    @NotBlank(message = "La ubicación del proyecto es obligatoria")
    @Size(max = 30, message = "La ubicación no puede superar los 30 caracteres")
    private String location;

    /** Notas adicionales o requisitos específicos del proyecto. */
    @NotBlank(message = "Las observaciones son obligatorias")
    @Size(max = 300, message = "Las observaciones no pueden superar los 300 caracteres")
    private String observations;
}
