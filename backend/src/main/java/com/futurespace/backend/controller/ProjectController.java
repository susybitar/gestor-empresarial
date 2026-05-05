package com.futurespace.backend.controller;

import com.futurespace.backend.model.dto.ProjectDTO;
import com.futurespace.backend.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador para la gestión de proyectos corporativos.
 * Gestiona el ciclo de vida de los proyectos y su visibilidad en el sistema.
 */
@RestController
@RequestMapping("/projects")
@Tag(name = "Gestión de Proyectos")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    /**
     * Recupera el listado de proyectos con soporte para paginación.
     */
    @GetMapping
    @Operation(summary = "Listado paginado de proyectos")
    public Page<ProjectDTO> getProjects(
            @PageableDefault(size = 20, sort = "startDate", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(name = "includeInactive", required = false, defaultValue = "false") boolean includeInactive) {
        return includeInactive ? projectService.getAllProjects(pageable) : projectService.getActiveProjects(pageable);
    }

    /** Recupera los datos de un proyecto específico por su identificador. */
    @GetMapping("/{id}")
    @Operation(summary = "Consulta de proyecto por ID")
    public ProjectDTO getProject(@PathVariable Integer id) {
        return projectService.getProjectById(id);
    }

    /** Registra un nuevo proyecto en la cartera de la empresa. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Alta de nuevo proyecto")
    public ProjectDTO createProject(@Valid @RequestBody ProjectDTO dto) {
        return projectService.saveProject(dto);
    }

    /** Actualiza la información técnica o descriptiva de un proyecto existente. */
    @PutMapping("/{id}")
    @Operation(summary = "Actualización de datos de proyecto")
    public ProjectDTO updateProject(@PathVariable Integer id,
                                    @Valid @RequestBody ProjectDTO dto) {
        return projectService.updateProject(id, dto);
    }

    /** Realiza la baja lógica de un proyecto en el sistema. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Baja lógica de proyecto")
    public void deleteProject(@PathVariable Integer id) {
        projectService.deactivateProject(id);
    }

    /** Realiza la eliminación física del registro de un proyecto. */
    @DeleteMapping("/{id}/physical")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Baja física de proyecto (Eliminación permanente)")
    public void deleteProjectPhysical(@PathVariable Integer id) {
        projectService.deleteProject(id);
    }
}
