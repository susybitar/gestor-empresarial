# Future Space Manager: Enterprise Management System

Aplicación fullstack para la gestión de empleados, proyectos y asignaciones
de una empresa. El sistema combina una interfaz SPA con una API REST y un
módulo de analítica, formando un ciclo completo de
gestión → persistencia → análisis.

---

## Módulos del Repositorio

### Enterprise UI — Frontend SPA

Interfaz de usuario construida con **Vue 3** y **Vuetify**, con diseño
minimalista orientado a producto. Cubre la gestión completa de empleados,
proyectos y asignaciones desde el navegador.

* **Destacado:** Sistema de diseño propio con componentes reutilizables
  (`CrystalInput`, `CrystalCard`, `FButton`, `EliteSearch`) y tipografía
  dual Outfit / Inter para separar marca de datos.
* **Arquitectura Reactiva:** Composition API (`<script setup>`) con stores
  Pinia para autenticación y estado global.
* **Dashboard Analítico:** Panel con KPIs en tiempo real, gráfico de flujo
  de proyectos y reparto geográfico por sedes
  ([DashboardView.vue](frontend/src/views/dashboard/DashboardView.vue)).
* **Comunicación:** Axios centralizado en services con interceptores para
  tokens y manejo de errores.

### Business Core — Backend API

API REST construida con **Java 17** y **Spring Boot**, encargada de toda la
lógica de negocio y la integridad referencial de la base de datos.

* **Destacado:** Gestión de **bajas lógicas** — los empleados y proyectos
  nunca se eliminan físicamente, se marcan con fecha de baja para mantener
  el histórico intacto. Restricción de borrado en proyectos con asignaciones
  activas.
* **Arquitectura de 3 Capas:** Controller → Service → Repository con DTOs
  de entrada y salida desacoplados de las entidades JPA.
* **Autenticación:** Módulo `AuthController` con login, registro y reset de
  contraseña.
* **Testing:** Tests unitarios e integración con JUnit y Mockito para
  controllers y services
  ([EmployeeControllerTest](backend/src/test/java/com/futurespace/backend/controller/EmployeeControllerTest.java),
  [ProjectServiceTest](backend/src/test/java/com/futurespace/backend/service/ProjectServiceTest.java)).
* **Validación:** Jakarta Bean Validation en DTOs y entidades (NIF, emails,
  fechas) con `GlobalExceptionHandler` centralizado.

### Business Intelligence — Analytics Module

Motor de análisis en **Python** que consume los datos de la API y genera
visualizaciones interactivas dentro de un Jupyter Notebook.

* **Destacado:** [data_analitycs.ipynb](analytics/data_analitycs.ipynb)
  — notebook interactivo con Plotly que analiza antigüedad, demografía,
  evolución de plantilla, duración de proyectos, carga de asignaciones y
  detección de proyectos sin personal asignado.
* **Data Engine:** [data_engine.py](analytics/data_engine.py) — capa de
  acceso a datos que conecta con MySQL en producción o genera datos
  sintéticos realistas en modo demo.
* **Generación de Reportes:** [generate_report.py](analytics/generate_report.py)
  — script que exporta gráficos en PNG para informes ejecutivos.

---

## Stack Tecnológico

* **Frontend:** Vue 3, Vite, Pinia, Vuetify 3, Axios, Lucide Icons
* **Backend:** Java 17, Spring Boot 3.x, Spring Data JPA, MySQL 8.0
* **Analítica:** Python 3.11+, Pandas, Plotly, NumPy, Jupyter
* **Testing:** JUnit 5, Mockito, Spring Boot Test
* **Metodologías:** Clean Code, SOLID, OOP, documentación JSDoc/JavaDoc

---

## Puesta en Marcha

### Con Docker (recomendado)

Requiere únicamente **Docker Desktop**. Levanta MySQL, backend y frontend con un solo comando:

```bash
docker-compose up --build
```

| Servicio  | URL                        |
|-----------|----------------------------|
| Frontend  | http://localhost:5173       |
| Backend   | http://localhost:8080       |
| MySQL     | localhost:3307              |

La primera ejecución descarga imágenes y compila el proyecto (~3-5 min).
Las siguientes arrancan en segundos.

Para detener: `docker-compose down`
Para detener y borrar datos: `docker-compose down -v`

---

### Manual (sin Docker)

El sistema requiere **Node.js 18+**, **Java 17** y **Python 3.11+**.

#### Backend

```bash
cd backend
./mvnw spring-boot:run
# API disponible en http://localhost:8080
```

#### Frontend

```bash
cd frontend
npm install
npm run dev
# App disponible en http://localhost:5173
```

#### Analytics

```bash
cd analytics
python -m venv .venv
.venv\Scripts\activate          # Windows
pip install -r requirements.txt
# Abrir data_analytics.ipynb en VS Code o Jupyter
```

---

## Arquitectura del Proyecto

```
gestor-empresarial/
├── frontend/                   Cliente SPA (Vue 3 + Vuetify)
│   ├── src/
│   │   ├── views/              Vistas: Dashboard, Employees, Projects, Assignments
│   │   ├── components/         Componentes: common/, dashboard/, layout/
│   │   ├── services/           Capa HTTP (Axios)
│   │   ├── stores/             Estado global (Pinia)
│   │   └── router/             Configuración de rutas
│   └── vite.config.js
├── backend/                    API REST (Spring Boot)
│   └── src/main/java/com/futurespace/backend/
│       ├── controller/         Endpoints REST
│       ├── service/            Lógica de negocio
│       ├── repository/         Acceso a datos (JPA)
│       ├── model/              Entidades y DTOs
│       ├── exception/          Manejo global de errores
│       └── config/             Seguridad (CORS, Auth)
├── analytics/                  Motor de inteligencia (Python)
│   ├── data_analitycs.ipynb     Notebook interactivo (Plotly)
│   ├── data_engine.py          Acceso a datos (MySQL / Demo)
│   ├── generate_report.py      Exportación de informes (PNG)
│   └── requirements.txt
├── postman/                    Colecciones Postman para pruebas
├── docs/                       Documentación técnica adicional
└── README.md
```

---

**Desarrollado por:** Susana Bitar
