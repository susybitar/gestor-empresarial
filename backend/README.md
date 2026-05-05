# Business Core: Spring Boot REST API & JPA Persistence

El núcleo del **Future Space Manager** es una API REST robusta construida con la suite de Spring, diseñada para garantizar la integridad de los datos empresariales y la escalabilidad del sistema.

---

## Características Técnicas

* **Arquitectura de 3 Capas:** Implementación del patrón **Controller -> Service -> Repository**, garantizando un desacoplamiento total entre los contratos de la API y la lógica de negocio.
* **Persistencia Robusta:** Uso de **Spring Data JPA** para la gestión de entidades, con repositorios optimizados y consultas personalizadas.
* **Integridad Referencial:** Lógica de negocio avanzada para el control de bajas lógicas y restricciones de eliminación física (evitando el borrado de proyectos con empleados asignados).
* **Validación Profesional:** Uso de **Jakarta Bean Validation** para asegurar que los datos persistidos cumplen con los estándares corporativos (NIF, Emails, Fechas).
* **Manejo Global de Excepciones:** Implementación de `@RestControllerAdvice` para centralizar la gestión de errores y devolver respuestas HTTP semánticas y coherentes.

---

## Stack Tecnológico

* **Lenguaje:** Java 17
* **Framework:** Spring Boot 3.x
* **Acceso a Datos:** Spring Data JPA / Hibernate
* **Base de Datos:** MySQL 8.0
* **Documentación:** Swagger / OpenAPI (disponible en `/swagger-ui.html`)

---

## Configuración y Ejecución

### Requisitos
* Java JDK 17
* Maven 3.8+
* MySQL Server activo

### Instalación
1. Clonar el repositorio.
2. Configurar las credenciales de la base de datos en `src/main/resources/application.properties`.
3. Ejecutar la aplicación:
```bash
./mvnw spring-boot:run
```

---

## Diseño de Entidades

* **Employee:** Gestión de datos personales, académicos y estado laboral.
* **Project:** Control de sedes, duraciones y estados operativos.
* **EmployeeProject:** Entidad relacional para la gestión de asignaciones históricas y vigentes.

---
**Desarrollado por:** Susana Bitar
