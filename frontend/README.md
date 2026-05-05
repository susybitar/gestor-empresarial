# Enterprise UI: Minimalist Design & Vue 3 Engine

Este módulo contiene la interfaz de usuario del **Future Space Manager**, una SPA (Single Page Application) diseñada bajo principios de minimalismo científico y alta reactividad.

---

## Características Técnicas

* **Arquitectura Reactiva:** Implementación basada en **Vue 3 (Composition API)** para un código modular y altamente mantenible.
* **Sistema de Diseño:** Estética **Fine-Line Minimalist** desarrollada con **Vuetify 3**, priorizando el uso de espacios en blanco, tipografía clara (`Outfit`) y una jerarquía visual equilibrada.
* **Gestión de Estado:** Uso de **Pinia** para la sincronización global de la autenticación y los estados de búsqueda.
* **Componentes Custom:** Desarrollo de una librería de componentes propios (`CrystalInput`, `FButton`, `StatCard`) que garantizan la coherencia visual en toda la plataforma.
* **Validaciones en Tiempo Real:** Sistema de validación reactivo que proporciona feedback inmediato al usuario sin necesidad de recarga.

---

## Stack Tecnológico

* **Framework:** Vue 3 (Vite)
* **UI Framework:** Vuetify 3
* **Iconografía:** Lucide Vue Next
* **Estilos:** CSS3 nativo con variables globales (Design Tokens)
* **Comunicación:** Axios para la integración con la API REST

---

## Guía de Desarrollo

### Instalación de dependencias
```bash
npm install
```

### Servidor de desarrollo (Hot Reload)
```bash
npm run dev
```

### Compilación para producción
```bash
npm run build
```

---

## Estructura de Directorios

* `src/views/`: Páginas principales (Dashboard, Employees, Projects, Assignments).
* `src/components/`: Componentes reutilizables organizados por responsabilidad (Common, Layout, Dashboard).
* `src/services/`: Capa de abstracción para la comunicación con la API.
* `src/stores/`: Gestión de estado global con Pinia.

---
**Desarrollado por:** Susana Bitar
