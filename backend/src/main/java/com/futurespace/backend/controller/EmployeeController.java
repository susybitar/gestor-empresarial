package com.futurespace.backend.controller;

import com.futurespace.backend.model.dto.EmployeeDTO;
import com.futurespace.backend.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
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
 * Controlador para la gestión de empleados.
 * Gestiona operaciones CRUD y la lógica de activación/desactivación.
 */
@RestController
@RequestMapping("/employees")
@Tag(name = "Gestión de Empleados")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    /**
     * Devuelve la lista de empleados de forma paginada.
     * Con el parámetro includeInactive podemos elegir si queremos ver también a los que ya no están.
     * @param pageable Configuración de la página (tamaño, orden, etc).
     * @param includeInactive Si queremos incluir empleados que ya no están activos.
     * @return Página con los datos de los empleados.
     */
    @GetMapping
    @Operation(summary = "Listado paginado de empleados")
    public Page<EmployeeDTO> getEmployees(
            @PageableDefault(size = 20, sort = "lastName", direction = Sort.Direction.ASC) Pageable pageable,
            @RequestParam(name = "includeInactive", required = false, defaultValue = "false")
            boolean includeInactive) {
        return includeInactive
                ? employeeService.getAllEmployees(pageable)
                : employeeService.getActiveEmployees(pageable);
    }

    /** Recupera los datos de un empleado específico por su identificador. */
    @GetMapping("/{id}")
    @Operation(summary = "Consulta de empleado por ID")
    public EmployeeDTO getEmployee(@PathVariable Integer id) {
        return employeeService.getEmployeeById(id);
    }

    /** Registra un nuevo empleado en el sistema. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Alta de nuevo empleado")
    public EmployeeDTO createEmployee(@Valid @RequestBody EmployeeDTO dto) {
        return employeeService.saveEmployee(dto);
    }

    /** Actualiza la información de un empleado existente. */
    @PutMapping("/{id}")
    @Operation(summary = "Actualización de datos de empleado")
    public EmployeeDTO updateEmployee(@PathVariable Integer id,
                                      @Valid @RequestBody EmployeeDTO dto) {
        return employeeService.updateEmployee(id, dto);
    }

    /**
     * Da de baja a un empleado (baja lógica).
     * El empleado no se borra de la base de datos, solo se le pone una fecha de fin de contrato.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Baja lógica de empleado")
    public void deleteEmployee(@PathVariable Integer id) {
        employeeService.deactivateEmployee(id);
    }

    /** Realiza la eliminación física del registro de un empleado. */
    @DeleteMapping("/{id}/physical")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Baja física de empleado (Eliminación permanente)")
    public void deleteEmployeePhysical(@PathVariable Integer id) {
        employeeService.deleteEmployee(id);
    }
}
