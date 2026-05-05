# Business Intelligence: Data Engine & Strategic Insights

El módulo de analítica de **Future Space Manager** proporciona la capa de inteligencia necesaria para transformar los datos de gestión en decisiones estratégicas sobre el capital humano y la cartera de proyectos.

---

## Módulos de Análisis

* **Análisis de Capital Humano:** Estudio de la pirámide de edad, rotación (altas/bajas) y ratios académicos de la plantilla.
* **Control de Proyectos:** Seguimiento de la duración media, saturación geográfica y estados operativos.
* **Detección de Anomalías:** Identificación automática de proyectos activos sin cobertura de personal o empleados infrautilizados.
* **Generación de Reportes:** Sistema automatizado que exporta visualizaciones de alta calidad (PNG) para su integración en presentaciones ejecutivas.

---

## Características Técnicas

* **Data Engine Flexible:** Capacidad para conectar con la base de datos MySQL de producción o trabajar en modo demo mediante la generación de datos sintéticos realistas.
* **Visualización Avanzada:** Uso de **Seaborn** y **Matplotlib** con una paleta de colores corporativa personalizada para garantizar la coherencia visual con la aplicación.
* **Arquitectura Orientada a Datos:** Procesamiento eficiente mediante **Pandas**, permitiendo transformaciones complejas y cálculos estadísticos en segundos.

---

## Stack Tecnológico

* **Lenguaje:** Python 3.11+
* **Data Science:** Pandas, NumPy
* **Visualización:** Seaborn, Matplotlib
* **Entorno:** Jupyter Notebooks para análisis exploratorio

---

## Puesta en Marcha

### Configuración del entorno
```bash
python -m venv .venv
source .venv/bin/activate  # En Windows: .venv\Scripts\activate
pip install -r requirements.txt
```

### Generación del informe
```bash
python generate_report.py
```
Los resultados se guardarán automáticamente en la carpeta `/reports`.

---
**Desarrollado por:** Susana Bitar
