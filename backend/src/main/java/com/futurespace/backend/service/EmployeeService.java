package com.futurespace.backend.service;

import com.futurespace.backend.exception.BusinessException;
import com.futurespace.backend.model.dto.EmployeeDTO;
import com.futurespace.backend.model.entities.Employee;
import com.futurespace.backend.repository.EmployeeProjectRepository;
import com.futurespace.backend.repository.EmployeeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;

/**
 * Servicio para la gestión de la entidad de empleados.
 * Centraliza la lógica de negocio, validaciones de integridad y persistencia.
 */
@Service
public class EmployeeService {

    private static final Logger log = LoggerFactory.getLogger(EmployeeService.class);

    private final EmployeeRepository employeeRepository;
    private final EmployeeProjectRepository assignmentRepository;

    public EmployeeService(EmployeeRepository employeeRepository,
                           EmployeeProjectRepository assignmentRepository) {
        this.employeeRepository = employeeRepository;
        this.assignmentRepository = assignmentRepository;
    }

    /**
     * Recupera empleados activos con soporte para paginación.
     * @param pageable Parámetros de paginación.
     * @return Página de EmployeeDTO activos.
     */
    @Transactional(readOnly = true)
    public Page<EmployeeDTO> getActiveEmployees(Pageable pageable) {
        return employeeRepository.findByTerminationDateIsNull(pageable).map(this::toDTO);
    }

    /**
     * Recupera el histórico total de empleados (activos e inactivos).
     * @param pageable Parámetros de paginación.
     * @return Página de EmployeeDTO total.
     */
    @Transactional(readOnly = true)
    public Page<EmployeeDTO> getAllEmployees(Pageable pageable) {
        return employeeRepository.findAll(Objects.requireNonNull(pageable)).map(this::toDTO);
    }

    /**
     * Recupera un empleado por su identificador único.
     * @param id ID del empleado.
     * @return EmployeeDTO correspondiente.
     * @throws BusinessException Si el empleado no existe o no está activo.
     */
    @Transactional(readOnly = true)
    public EmployeeDTO getEmployeeById(Integer id) {
        return toDTO(fetchActiveEmployee(id));
    }

    /**
     * Procesa el alta de un nuevo empleado tras validar restricciones de NIF.
     * @param dto Datos del empleado.
     * @return DTO del empleado persistido.
     * @throws BusinessException Si el NIF ya existe o hay discrepancias en el ID.
     */
    @Transactional
    public EmployeeDTO saveEmployee(EmployeeDTO dto) {
        if (dto.getIdEmployee() != null) {
            throw new BusinessException("Restricción: El identificador debe ser autogenerado.");
        }
        if (employeeRepository.findByNif(dto.getNif()).isPresent()) {
            log.warn("Alta fallida: NIF duplicado {}", dto.getNif());
            throw new BusinessException("El NIF indicado ya está registrado en el sistema.");
        }

        Employee entity = toEntity(dto);
        validateBirthDate(entity);

        Employee saved = employeeRepository.save(entity);
        log.info("Nuevo empleado registrado: ID={}, NIF={}", saved.getIdEmployee(), saved.getNif());
        return toDTO(saved);
    }

    /**
     * Actualiza los datos de un empleado activo validando campos inmutables.
     * @param id ID del empleado a actualizar.
     * @param dto Nuevos datos.
     * @return DTO actualizado.
     */
    @Transactional
    public EmployeeDTO updateEmployee(Integer id, EmployeeDTO dto) {
        validateUpdateInput(id, dto);
        Employee current = fetchActiveEmployee(id);
        validateImmutableFields(id, dto, current);

        applyUpdatableFields(dto, current);
        validateBirthDate(current);

        Employee saved = employeeRepository.save(current);
        log.info("Empleado actualizado: ID={}", saved.getIdEmployee());
        return toDTO(saved);
    }

    /**
     * Da de baja a un empleado de forma lógica.
     * No podemos darlo de baja si todavía tiene proyectos asignados.
     * @param id ID del empleado.
     * @throws BusinessException Si el empleado todavía tiene proyectos activos.
     */
    @Transactional
    public void deactivateEmployee(Integer id) {
        Employee employee = fetchActiveEmployee(id);

        Set<String> assignedProjects = assignmentRepository.findProjectDescriptionsByEmployeeId(id);
        if (!assignedProjects.isEmpty()) {
            log.warn("Baja denegada: Empleado ID={} con proyectos activos.", id);
            String fullName = buildFullName(employee);
            String projects = String.join(", ", assignedProjects);
            throw new BusinessException(
                    "No se puede dar de baja al empleado " + fullName +
                    " porque está asignado a el/los proyecto/s " + projects);
        }

        employee.setTerminationDate(LocalDate.now());
        employeeRepository.save(employee);
        log.info("Baja lógica procesada: ID={}", id);
    }

    /**
     * Realiza la eliminación física del empleado en la base de datos.
     * @param id ID del empleado a eliminar.
     */
    @Transactional
    public void deleteEmployee(Integer id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Recurso no encontrado."));
        
        if (!assignmentRepository.findProjectDescriptionsByEmployeeId(id).isEmpty()) {
            throw new BusinessException("Restricción: No se puede eliminar físicamente un empleado con histórico de proyectos.");
        }
        
        employeeRepository.delete(employee);
        log.info("Eliminación física procesada: ID={}", id);
    }

    // -----------------------------------------------------------------
    // Métodos auxiliares de soporte
    // -----------------------------------------------------------------

    private Employee fetchActiveEmployee(Integer id) {
        if (id == null || id <= 0) {
            throw new BusinessException("Error: Identificador de recurso no válido.");
        }
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Recurso no encontrado."));
        if (employee.getTerminationDate() != null) {
            throw new BusinessException("Recurso inactivo.");
        }
        return employee;
    }

    private void validateUpdateInput(Integer id, EmployeeDTO dto) {
        if (id == null || id <= 0 || dto == null) {
            throw new BusinessException("Error: Entrada de datos no válida para actualización.");
        }
    }

    /**
     * Comprueba que no se estén intentando cambiar campos que son fijos.
     * Por normativa, el NIF y la fecha de alta no se pueden modificar una vez creados.
     */
    private void validateImmutableFields(Integer id, EmployeeDTO dto, Employee current) {
        if (dto.getIdEmployee() != null && !Objects.equals(id, dto.getIdEmployee())) {
            throw new BusinessException("Error: Discrepancia en identificadores de recurso.");
        }
        boolean nifChanged = !current.getNif().equalsIgnoreCase(dto.getNif());
        boolean hireDateChanged = !Objects.equals(current.getHireDate(), dto.getHireDate());
        if (nifChanged || hireDateChanged) {
            throw new BusinessException("Restricción: NIF y Fecha de Alta son inmutables.");
        }
    }

    private void applyUpdatableFields(EmployeeDTO dto, Employee entity) {
        entity.setFirstName(dto.getFirstName());
        entity.setLastName(dto.getLastName());
        entity.setSecondLastName(dto.getSecondLastName());
        entity.setBirthDate(dto.getBirthDate());
        entity.setPhone1(dto.getPhone1());
        entity.setPhone2(dto.getPhone2());
        entity.setEmail(dto.getEmail());
        entity.setMaritalStatus(dto.getMaritalStatus());
        entity.setHasUniversityEducation(dto.getHasUniversityEducation());
    }

    private void validateBirthDate(Employee employee) {
        if (employee.getBirthDate() != null && !employee.getBirthDate().isBefore(LocalDate.now())) {
            throw new BusinessException("Validación: La fecha de nacimiento debe ser anterior a la actual.");
        }
    }

    private String buildFullName(Employee e) {
        StringBuilder sb = new StringBuilder(e.getFirstName()).append(" ").append(e.getLastName());
        if (e.getSecondLastName() != null && !e.getSecondLastName().isBlank()) {
            sb.append(" ").append(e.getSecondLastName());
        }
        return sb.toString();
    }

    private EmployeeDTO toDTO(Employee e) {
        EmployeeDTO dto = new EmployeeDTO();
        dto.setIdEmployee(e.getIdEmployee());
        dto.setNif(e.getNif());
        dto.setFirstName(e.getFirstName());
        dto.setLastName(e.getLastName());
        dto.setSecondLastName(e.getSecondLastName());
        dto.setBirthDate(e.getBirthDate());
        dto.setPhone1(e.getPhone1());
        dto.setPhone2(e.getPhone2());
        dto.setEmail(e.getEmail());
        dto.setHireDate(e.getHireDate());
        dto.setTerminationDate(e.getTerminationDate());
        dto.setMaritalStatus(e.getMaritalStatus());
        dto.setHasUniversityEducation(e.getHasUniversityEducation());
        return dto;
    }

    private Employee toEntity(EmployeeDTO dto) {
        Employee e = new Employee();
        e.setNif(dto.getNif());
        e.setFirstName(dto.getFirstName());
        e.setLastName(dto.getLastName());
        e.setSecondLastName(dto.getSecondLastName());
        e.setBirthDate(dto.getBirthDate());
        e.setPhone1(dto.getPhone1());
        e.setPhone2(dto.getPhone2());
        e.setEmail(dto.getEmail());
        e.setHireDate(dto.getHireDate());
        e.setMaritalStatus(dto.getMaritalStatus());
        e.setHasUniversityEducation(dto.getHasUniversityEducation());
        return e;
    }
}
