# Informe de Decisiones Técnicas y Arquitectura: Future Space Manager

Este documento constituye la memoria técnica completa del proyecto **Future Space | Manager**. Detalla cada decisión tomada, desde la infraestructura del backend hasta el diseño de la interfaz y el motor de analítica, asegurando el cumplimiento total de los requisitos de la práctica final y aportando mejoras de valor añadido.

---

## 1. Alineación con los Objetivos del Proyecto (PDF)
El proyecto se ha diseñado para cubrir el ciclo de vida completo de un ERP de gestión de recursos humanos y proyectos, cumpliendo con los 5 bloques principales exigidos:
1.  **Módulo de Empleados:** Gestión completa (Altas, Bajas, Modificaciones).
2.  **Módulo de Proyectos:** Seguimiento de estados y localizaciones.
3.  **Módulo de Asignaciones:** Cruce dinámico entre personal y proyectos.
4.  **Seguridad:** Control de acceso para administradores.
5.  **Analítica:** Cuadro de mando estratégico en Python.

---

## 2. Ingeniería de Backend (Java & Spring Boot)
El backend se ha construido sobre **Spring Boot 3.2**, priorizando la robustez y la seguridad.

### Decisiones de Arquitectura:
*   **Patrón de Diseño:** Se aplica una **Arquitectura en Capas** (Controller -> Service -> Repository). La lógica de negocio está aislada en los `Services`, permitiendo que los `Controllers` solo se encarguen de la comunicación HTTP.
*   **Gestión de Datos (JPA/Hibernate):**
    *   **Mapeo de Entidades:** Se han mapeado tablas complejas usando relaciones `@ManyToOne` y `@OneToMany` para reflejar la realidad de la base de datos de Future Space.
    *   **Clave Compuesta en Asignaciones:** Para la tabla `PR_EMPLEADOS_PROYECTO`, se ha implementado `@EmbeddedId`. Esta es una decisión crítica para asegurar que la base de datos no permita errores de integridad referencial.
    *   **Nomenclatura:** Se mantiene la convención de nombres en inglés en el código (`Employee`, `ProjectAssignment`) para seguir estándares internacionales, mapeando mediante `@Column` a los nombres físicos en español exigidos.

### Decisiones de Seguridad y Lógica Extra:
*   **Protección de Datos:** Se ha implementado el **borrado lógico (Soft Delete)** mediante el campo `F_BAJA`. Esto permite que la aplicación cumpla con el requisito de "mantener históricos" sin perder información.
*   **Cifrado BCrypt:** Las contraseñas de los usuarios administradores se procesan con un algoritmo de hashing BCrypt con un factor de trabajo de 10, garantizando que sean irreversibles.
*   **Validaciones en Servidor:** Uso de la API `Bean Validation` para asegurar que ningún dato corrupto entre en la base de datos (ej: formatos de NIF, fechas obligatorias).

---

## 3. Frontend y Experiencia de Usuario (Vue 3)
El frontend se ha diseñado para ser una herramienta de "Grado Profesional", alejándose de interfaces académicas simples.

### Decisiones de UI/UX:
*   **Stack Tecnológico:** Vue 3 por su rendimiento y **Vite** como empaquetador por su velocidad de desarrollo instantánea.
*   **Sistema de Diseño:**
    *   **Tipografía Inter:** Se ha integrado la fuente Inter de Google Fonts para dar un aspecto de aplicación moderna y técnica.
    *   **Psicología del Color:** Uso de `Slate-900` para denotar autoridad y `Sky-500` para llamadas a la acción claras. El fondo neutro `#F8FAFC` previene la fatiga visual.
    *   **Consistencia:** Uso de un sistema de espaciado basado en 8px para asegurar que todos los componentes estén perfectamente alineados.

### Decisiones de Lógica y Estado:
*   **Gestión de Estado (Pinia):** Se ha centralizado el estado del usuario y la sesión en un Store de Pinia con persistencia, para evitar pérdidas de sesión al recargar el navegador.
*   **Validaciones en Tiempo Real:** Los formularios dan feedback instantáneo (rojo/verde) antes de enviar los datos, mejorando la experiencia del usuario y reduciendo errores.
*   **Seguridad en Rutas:** Implementación de `Navigation Guards`. El sistema intercepta cada cambio de página para verificar que el usuario tiene permisos, redirigiendo al login si no está autenticado.

---

## 4. Módulo de Analítica y BI (Python)
El módulo de analítica es donde se cruzan los requisitos estadísticos del PDF con la visualización de datos moderna.

### Decisiones de Ingeniería de Datos:
*   **DataEngine Independiente:** Se ha desarrollado un motor de extracción de datos en Python que actúa como una capa de abstracción. Esto permite cambiar la base de datos en el futuro sin tocar los gráficos.
*   **Limpieza y Transformación:**
    *   **Casting de tipos:** Conversión estricta de `Edad` y `Antigüedad` a `int` para eliminar ruidos visuales de decimales.
    *   **Merge de Tablas:** Unión dinámica de las tablas de asignaciones y proyectos para poder mostrar nombres legibles en lugar de códigos ID.

### Decisiones de Visualización (Extras Masterpiece):
*   **Plotly Interactivo:** Se ha elegido Plotly sobre librerías estáticas (como Matplotlib) para permitir que el usuario explore los datos pasando el ratón sobre los gráficos.
*   **Sistema de Medallas (Ranking):** Introducción de lógica visual (🥇🥈🥉) en las tablas de sedes y veteranos para destacar los puntos clave sin necesidad de leer toda la tabla.
*   **Detección Automática de Anomalías:** Algoritmo que cruza los proyectos activos con la tabla de personal para lanzar alertas rojas en caso de detectar proyectos sin asignación.
*   **Insights en Lenguaje Natural:** Inclusión de bloques de texto que resumen el "porqué" de los datos, aportando valor añadido al análisis puro.

---

## 5. Resumen de Cumplimiento del PDF
*   **Punto 5.5 (Estadísticas):** Se ha implementado el cálculo exacto de la **Media** y la **Desviación Típica** de las edades, presentado en tarjetas de alta visibilidad.
*   **Control de Estados:** Gestión completa de los estados de empleados y proyectos (Activo, Baja, Pendiente).
*   **Integración:** El notebook de Python consume el `data_engine.py` desarrollado específicamente para este proyecto.

---

## 6. Filosofía de Desarrollo
La decisión técnica final ha sido la de **calidad sobre cantidad**. Se ha preferido una estructura de carpetas limpia, un código bien comentado y una interfaz pulida que demuestre que el proyecto está listo para ser usado en un entorno de producción real.

---
**Firmado: Proyecto Final - Beca Future Space 2026**
