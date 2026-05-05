"""
generate_report.py
Módulo de analítica completo para el sistema de gestión empresarial.

Genera un informe visual en PNG con todos los análisis requeridos:
  · Capital humano (antigüedad, edad, estado civil, evolución temporal)
  · Cartera de proyectos (estado, duración, distribución geográfica)
  · Cruce empleado-proyecto (carga, top proyectos, anomalías)

Uso:
    python generate_report.py

Salida:
    Carpeta reports/ con un PNG por análisis y resumen de insights en consola.
"""

import os
import textwrap
import warnings
import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
import matplotlib.ticker as mticker
import seaborn as sns
from matplotlib.patches import FancyBboxPatch

from data_engine import DataEngine

warnings.filterwarnings("ignore")

# ──────────────────────────────────────────────────────────────────────
# SISTEMA DE DISEÑO
# ──────────────────────────────────────────────────────────────────────

class Style:
    """Paleta de colores y constantes tipográficas del informe."""

    # Colores principales (alineados con el diseño de la aplicación)
    BLUE    = "#1E40AF"
    BLUE_LT = "#BFDBFE"
    GREEN   = "#10B981"
    AMBER   = "#F59E0B"
    RED     = "#EF4444"
    SLATE   = "#64748B"
    DARK    = "#0F172A"
    BG      = "#F8FAFC"
    WHITE   = "#FFFFFF"

    # Paleta secuencial para gráficos comparativos
    SEQ = ["#1E40AF", "#3B82F6", "#60A5FA", "#93C5FD", "#BFDBFE"]

    # Paleta categórica para múltiples series
    CAT = ["#1E40AF", "#10B981", "#F59E0B", "#EF4444", "#8B5CF6", "#06B6D4"]

    FONT_TITLE  = 13
    FONT_LABEL  = 10
    FONT_TICK   = 8
    FONT_ANNOT  = 8


def setup_style() -> None:
    """Aplica el tema visual global a matplotlib y seaborn."""
    plt.rcParams.update({
        "figure.facecolor":     Style.WHITE,
        "axes.facecolor":       Style.WHITE,
        "axes.edgecolor":       "#E2E8F0",
        "axes.linewidth":       0.8,
        "axes.grid":            True,
        "axes.grid.axis":       "y",
        "grid.color":           "#F1F5F9",
        "grid.linewidth":       0.8,
        "xtick.color":          Style.SLATE,
        "ytick.color":          Style.SLATE,
        "xtick.labelsize":      Style.FONT_TICK,
        "ytick.labelsize":      Style.FONT_TICK,
        "font.family":          "sans-serif",
        "text.color":           Style.DARK,
        "legend.frameon":       False,
        "legend.fontsize":      Style.FONT_TICK,
    })
    sns.set_theme(style="white", rc=plt.rcParams)


def save_figure(fig: plt.Figure, filename: str, output_dir: str) -> None:
    """
    Guarda la figura en disco con configuración de calidad estándar.

    Args:
        fig:        Figura matplotlib a guardar.
        filename:   Nombre del archivo (sin extensión).
        output_dir: Carpeta de destino (se crea si no existe).
    """
    os.makedirs(output_dir, exist_ok=True)
    path = os.path.join(output_dir, f"{filename}.png")
    fig.savefig(path, dpi=150, bbox_inches="tight", facecolor=Style.WHITE)
    plt.close(fig)
    print(f"  ✓ {filename}.png")


def styled_title(ax: plt.Axes, title: str, subtitle: str = "") -> None:
    """Aplica título y subtítulo con la jerarquía tipográfica del informe."""
    ax.set_title(
        title + (f"\n{subtitle}" if subtitle else ""),
        fontsize=Style.FONT_TITLE,
        fontweight="bold",
        color=Style.DARK,
        pad=14,
        loc="left",
    )


def add_bar_labels(ax: plt.Axes, fmt: str = "{:.0f}", color: str = Style.SLATE) -> None:
    """Añade etiquetas de valor encima de cada barra."""
    for patch in ax.patches:
        h = patch.get_height()
        if h > 0:
            ax.text(
                patch.get_x() + patch.get_width() / 2,
                h + ax.get_ylim()[1] * 0.01,
                fmt.format(h),
                ha="center", va="bottom",
                fontsize=Style.FONT_ANNOT,
                color=color,
            )


def print_insight(text: str) -> None:
    """Imprime un insight formateado en consola."""
    wrapped = textwrap.fill(text, width=80, initial_indent="  → ", subsequent_indent="    ")
    print(f"\n  Insight:\n{wrapped}\n")


# ──────────────────────────────────────────────────────────────────────
# BLOQUE 1 — CAPITAL HUMANO
# ──────────────────────────────────────────────────────────────────────

def plot_antiguedad_top(df: pd.DataFrame, output_dir: str) -> None:
    """
    Gráfico de barras horizontal: Top 5 empleados más veteranos vs. 5 más recientes.

    Args:
        df:         DataFrame de empleados (requiere TX_NOMBRE, ANTIGUEDAD_ANOS).
        output_dir: Carpeta de salida.
    """
    top_old = df.nlargest(5, "ANTIGUEDAD_ANOS")[["TX_NOMBRE", "ANTIGUEDAD_ANOS"]].copy()
    top_new = df.nsmallest(5, "ANTIGUEDAD_ANOS")[["TX_NOMBRE", "ANTIGUEDAD_ANOS"]].copy()
    top_old["Categoría"] = "Veteranos"
    top_new["Categoría"] = "Incorporaciones recientes"
    combined = pd.concat([top_old, top_new]).sort_values("ANTIGUEDAD_ANOS")

    fig, ax = plt.subplots(figsize=(10, 5))
    colors = [Style.BLUE if c == "Veteranos" else Style.GREEN
              for c in combined["Categoría"]]

    bars = ax.barh(combined["TX_NOMBRE"], combined["ANTIGUEDAD_ANOS"],
                   color=colors, height=0.6, zorder=3)

    # Etiquetas de valor al final de cada barra
    for bar, val in zip(bars, combined["ANTIGUEDAD_ANOS"]):
        ax.text(bar.get_width() + 0.1, bar.get_y() + bar.get_height() / 2,
                f"{val:.1f} años", va="center", fontsize=Style.FONT_ANNOT,
                color=Style.SLATE)

    # Leyenda manual
    from matplotlib.patches import Patch
    legend_elements = [
        Patch(facecolor=Style.BLUE,  label="Veteranos"),
        Patch(facecolor=Style.GREEN, label="Incorporaciones recientes"),
    ]
    ax.legend(handles=legend_elements, loc="lower right")

    styled_title(ax, "Antigüedad: Top 5 Veteranos vs. 5 más Recientes",
                 "Años de permanencia en la empresa")
    ax.set_xlabel("Antigüedad (años)", fontsize=Style.FONT_LABEL)
    ax.set_xlim(0, combined["ANTIGUEDAD_ANOS"].max() * 1.18)
    ax.grid(axis="x", linestyle="--", alpha=0.5)
    ax.grid(axis="y", visible=False)
    fig.tight_layout()
    save_figure(fig, "01_antiguedad_top", output_dir)

    max_name = top_old.iloc[-1]["TX_NOMBRE"]
    max_yrs  = top_old.iloc[-1]["ANTIGUEDAD_ANOS"]
    print_insight(
        f"El empleado con mayor antigüedad es {max_name} con {max_yrs:.1f} años en la empresa. "
        "La brecha entre el perfil más veterano y el más reciente refleja la madurez y rotación "
        "de la plantilla."
    )


def plot_distribucion_antiguedad(df: pd.DataFrame, output_dir: str) -> None:
    """
    Histograma + KDE de la distribución de años de antigüedad en la empresa.

    Args:
        df:         DataFrame de empleados (requiere ANTIGUEDAD_ANOS).
        output_dir: Carpeta de salida.
    """
    activos = df[df["ESTADO"] == "Activo"]["ANTIGUEDAD_ANOS"].dropna()

    fig, ax = plt.subplots(figsize=(9, 4.5))
    sns.histplot(activos, bins=12, color=Style.BLUE, alpha=0.7,
                 edgecolor=Style.WHITE, ax=ax, zorder=3)
    ax.axvline(activos.mean(), color=Style.RED, linewidth=1.5,
               linestyle="--", label=f"Media: {activos.mean():.1f} años", zorder=4)
    ax.axvline(activos.median(), color=Style.AMBER, linewidth=1.5,
               linestyle=":", label=f"Mediana: {activos.median():.1f} años", zorder=4)

    styled_title(ax, "Distribución de Antigüedad (empleados activos)",
                 f"n={len(activos)}  ·  σ={activos.std():.1f} años")
    ax.set_xlabel("Años en la empresa", fontsize=Style.FONT_LABEL)
    ax.set_ylabel("Número de empleados", fontsize=Style.FONT_LABEL)
    ax.legend()
    fig.tight_layout()
    save_figure(fig, "02_distribucion_antiguedad", output_dir)

    print_insight(
        f"La antigüedad media es {activos.mean():.1f} años con una desviación de "
        f"{activos.std():.1f} años. "
        "Una distribución desplazada hacia la derecha indica una plantilla consolidada; "
        "hacia la izquierda, crecimiento reciente."
    )


def plot_evolucion_altas_bajas(df: pd.DataFrame, output_dir: str) -> None:
    """
    Gráfico de líneas: evolución anual de altas y bajas. Identifica los dos
    años de mayor y menor crecimiento neto.

    Args:
        df:         DataFrame de empleados (requiere F_ALTA, F_BAJA).
        output_dir: Carpeta de salida.
    """
    altas  = df["F_ALTA"].dt.year.value_counts().rename("Altas").sort_index()
    bajas  = df["F_BAJA"].dropna().dt.year.value_counts().rename("Bajas").sort_index()
    evo    = pd.DataFrame({"Altas": altas, "Bajas": bajas}).fillna(0).astype(int)
    evo["Neto"] = evo["Altas"] - evo["Bajas"]

    top2_growth   = evo["Neto"].nlargest(2).index.tolist()
    top2_decline  = evo["Neto"].nsmallest(2).index.tolist()

    fig, ax = plt.subplots(figsize=(10, 4.5))
    ax.plot(evo.index, evo["Altas"], marker="o", linewidth=2.5,
            color=Style.GREEN, label="Altas", zorder=4)
    ax.plot(evo.index, evo["Bajas"], marker="o", linewidth=2.5,
            color=Style.RED, label="Bajas", zorder=4)
    ax.fill_between(evo.index, evo["Altas"], evo["Bajas"],
                    alpha=0.08, color=Style.BLUE)

    # Marcar años de mayor crecimiento
    for yr in top2_growth:
        ax.axvline(yr, color=Style.BLUE, linewidth=1, linestyle="--", alpha=0.5)
        ax.text(yr, ax.get_ylim()[1] * 0.92, f"↑{yr}", fontsize=7,
                color=Style.BLUE, ha="center")

    ax.set_xticks(evo.index)
    ax.xaxis.set_tick_params(rotation=45)
    styled_title(ax, "Evolución Anual de Altas y Bajas",
                 f"Años de mayor crecimiento: {', '.join(map(str, top2_growth))}")
    ax.set_ylabel("Número de empleados", fontsize=Style.FONT_LABEL)
    ax.legend()
    fig.tight_layout()
    save_figure(fig, "03_evolucion_altas_bajas", output_dir)

    print_insight(
        f"Los años de mayor incorporación neta son {top2_growth[0]} y {top2_growth[1]}. "
        "Un cruce entre las líneas de altas y bajas señala periodos de reducción de plantilla "
        "que merecen análisis específico."
    )


def plot_estado_civil(df: pd.DataFrame, output_dir: str) -> None:
    """
    Gráfico de donut: distribución del estado civil de los empleados activos.

    Args:
        df:         DataFrame de empleados (requiere CX_EDOCIVIL, ESTADO).
        output_dir: Carpeta de salida.
    """
    mapa = {"S": "Soltero/a", "C": "Casado/a", "V": "Viudo/a", "D": "Divorciado/a"}
    activos = df[df["ESTADO"] == "Activo"].copy()
    conteo  = activos["CX_EDOCIVIL"].map(mapa).value_counts()

    fig, ax = plt.subplots(figsize=(7, 5))
    wedges, texts, autotexts = ax.pie(
        conteo,
        labels=conteo.index,
        autopct="%1.1f%%",
        startangle=90,
        colors=Style.CAT[:len(conteo)],
        pctdistance=0.75,
        wedgeprops=dict(width=0.55, edgecolor=Style.WHITE, linewidth=2),
    )
    for t in autotexts:
        t.set_fontsize(Style.FONT_ANNOT)
        t.set_color(Style.WHITE)
        t.set_fontweight("bold")

    styled_title(ax, "Distribución por Estado Civil",
                 f"Empleados activos · n={len(activos)}")
    fig.tight_layout()
    save_figure(fig, "04_estado_civil", output_dir)

    pct_casados = conteo.get("Casado/a", 0) / conteo.sum() * 100
    print_insight(
        f"El {pct_casados:.1f} % del personal activo está casado/a. "
        "Este dato es relevante para políticas de conciliación y beneficios sociales."
    )


def plot_distribucion_edades(df: pd.DataFrame, output_dir: str) -> None:
    """
    Histograma de la distribución de edades con media y desviación estándar.

    Args:
        df:         DataFrame de empleados (requiere EDAD).
        output_dir: Carpeta de salida.
    """
    edades = df["EDAD"].dropna()
    media, std = edades.mean(), edades.std()

    fig, ax = plt.subplots(figsize=(9, 4.5))
    sns.histplot(edades, bins=14, color=Style.BLUE, alpha=0.7,
                 edgecolor=Style.WHITE, ax=ax, zorder=3)
    ax.axvline(media, color=Style.RED, linewidth=1.8, linestyle="--",
               label=f"Media: {media:.1f} años", zorder=4)
    ax.axvspan(media - std, media + std, alpha=0.07, color=Style.BLUE,
               label=f"±1σ ({media-std:.0f}–{media+std:.0f} años)")

    styled_title(ax, "Pirámide de Edad de la Plantilla",
                 f"Media: {media:.1f} años  ·  σ: {std:.1f} años")
    ax.set_xlabel("Edad (años)", fontsize=Style.FONT_LABEL)
    ax.set_ylabel("Número de empleados", fontsize=Style.FONT_LABEL)
    ax.legend()
    fig.tight_layout()
    save_figure(fig, "05_distribucion_edades", output_dir)

    print_insight(
        f"La plantilla tiene una edad media de {media:.1f} años (σ={std:.1f}). "
        "Una distribución bimodal puede indicar dos generaciones diferenciadas "
        "y puede anticipar un relevo generacional próximo."
    )


# ──────────────────────────────────────────────────────────────────────
# BLOQUE 2 — CARTERA DE PROYECTOS
# ──────────────────────────────────────────────────────────────────────

def plot_estado_proyectos(df: pd.DataFrame, output_dir: str) -> None:
    """
    Gráfico de barras: proyectos activos vs. cesados con totales.

    Args:
        df:         DataFrame de proyectos (requiere ESTADO).
        output_dir: Carpeta de salida.
    """
    conteo = df["ESTADO"].value_counts()
    colores = {
        "Activo": Style.GREEN,
        "Cesado": Style.SLATE,
    }

    fig, ax = plt.subplots(figsize=(7, 4))
    bars = ax.bar(conteo.index, conteo.values,
                  color=[colores.get(e, Style.BLUE) for e in conteo.index],
                  width=0.45, zorder=3)
    add_bar_labels(ax)

    # Anotación del total
    ax.text(0.98, 0.96, f"Total: {len(df)} proyectos",
            transform=ax.transAxes, ha="right", va="top",
            fontsize=Style.FONT_ANNOT, color=Style.SLATE)

    styled_title(ax, "Estado de la Cartera de Proyectos")
    ax.set_ylabel("Número de proyectos", fontsize=Style.FONT_LABEL)
    ax.set_ylim(0, conteo.max() * 1.2)
    fig.tight_layout()
    save_figure(fig, "06_estado_proyectos", output_dir)

    activos = conteo.get("Activo", 0)
    total   = len(df)
    print_insight(
        f"{activos} de {total} proyectos ({activos/total*100:.0f} %) están activos. "
        "Una ratio alta de proyectos activos indica expansión del negocio, "
        "pero también mayor presión sobre los recursos disponibles."
    )


def plot_duracion_proyectos(df: pd.DataFrame, output_dir: str) -> None:
    """
    Histograma de la distribución de duración de proyectos en días.

    Args:
        df:         DataFrame de proyectos (requiere DURACION_DIAS).
        output_dir: Carpeta de salida.
    """
    dur = df["DURACION_DIAS"].dropna()
    dur = dur[dur > 0]

    fig, ax = plt.subplots(figsize=(9, 4.5))
    sns.histplot(dur, bins=15, color=Style.BLUE, alpha=0.7,
                 edgecolor=Style.WHITE, ax=ax, zorder=3)
    media = dur.mean()
    ax.axvline(media, color=Style.RED, linewidth=1.8, linestyle="--",
               label=f"Media: {media:.0f} días")

    styled_title(ax, "Distribución de Duración de Proyectos",
                 f"Media: {media:.0f} días  ·  n={len(dur)}")
    ax.set_xlabel("Duración (días)", fontsize=Style.FONT_LABEL)
    ax.set_ylabel("Número de proyectos", fontsize=Style.FONT_LABEL)
    ax.legend()
    fig.tight_layout()
    save_figure(fig, "07_duracion_proyectos", output_dir)

    p90 = dur.quantile(0.9)
    print_insight(
        f"La duración media de un proyecto es {media:.0f} días. "
        f"El 10 % de los proyectos supera los {p90:.0f} días, lo que puede indicar "
        "proyectos estratégicos de largo alcance o desviaciones en la planificación."
    )


def plot_proyectos_por_sede(df: pd.DataFrame, output_dir: str) -> None:
    """
    Gráfico de barras horizontal: distribución de proyectos por sede geográfica.

    Args:
        df:         DataFrame de proyectos (requiere TX_LUGAR).
        output_dir: Carpeta de salida.
    """
    conteo = df["TX_LUGAR"].value_counts().sort_values()

    fig, ax = plt.subplots(figsize=(9, max(4, len(conteo) * 0.55)))
    bars = ax.barh(conteo.index, conteo.values,
                   color=Style.BLUE, alpha=0.8, height=0.6, zorder=3)

    for bar, val in zip(bars, conteo.values):
        ax.text(bar.get_width() + 0.1, bar.get_y() + bar.get_height() / 2,
                str(val), va="center", fontsize=Style.FONT_ANNOT, color=Style.SLATE)

    styled_title(ax, "Distribución de Proyectos por Sede Geográfica")
    ax.set_xlabel("Número de proyectos", fontsize=Style.FONT_LABEL)
    ax.set_xlim(0, conteo.max() * 1.18)
    ax.grid(axis="x", linestyle="--", alpha=0.5)
    ax.grid(axis="y", visible=False)
    fig.tight_layout()
    save_figure(fig, "08_proyectos_por_sede", output_dir)

    sede_principal = conteo.index[-1]
    pct = conteo.iloc[-1] / conteo.sum() * 100
    print_insight(
        f"{sede_principal} concentra el {pct:.0f} % de la actividad. "
        "Una concentración geográfica elevada puede suponer un riesgo operativo "
        "ante contingencias locales."
    )


# ──────────────────────────────────────────────────────────────────────
# BLOQUE 3 — CRUCE EMPLEADO-PROYECTO
# ──────────────────────────────────────────────────────────────────────

def plot_carga_trabajo(df_emp: pd.DataFrame, df_asg: pd.DataFrame,
                       output_dir: str) -> None:
    """
    Gráfico de barras: cuántos empleados activos tienen 1, 2, 3... proyectos asignados.

    Args:
        df_emp:     DataFrame de empleados.
        df_asg:     DataFrame de asignaciones.
        output_dir: Carpeta de salida.
    """
    activos = set(df_emp[df_emp["ESTADO"] == "Activo"]["ID_EMPLEADO"])
    carga   = (df_asg[df_asg["ID_EMPLEADO"].isin(activos)]
               .groupby("ID_EMPLEADO")
               .size()
               .value_counts()
               .sort_index())

    fig, ax = plt.subplots(figsize=(8, 4.5))
    bars = ax.bar(carga.index.astype(str), carga.values,
                  color=Style.BLUE, alpha=0.8, width=0.55, zorder=3)
    add_bar_labels(ax)

    # Etiquetas descriptivas
    for x, (n_proy, n_emp) in enumerate(zip(carga.index, carga.values)):
        plural = "s" if n_proy > 1 else ""
        ax.text(x, -ax.get_ylim()[1] * 0.06,
                f"{n_emp} persona{'s' if n_emp > 1 else ''}",
                ha="center", fontsize=7, color=Style.SLATE)

    styled_title(ax, "Carga de Trabajo: Proyectos por Empleado Activo",
                 "Distribución de asignaciones simultáneas")
    ax.set_xlabel("Número de proyectos asignados", fontsize=Style.FONT_LABEL)
    ax.set_ylabel("Número de empleados", fontsize=Style.FONT_LABEL)
    ax.set_ylim(0, carga.max() * 1.25)
    fig.tight_layout()
    save_figure(fig, "09_carga_trabajo", output_dir)

    sin_asig = len(activos) - df_asg[df_asg["ID_EMPLEADO"].isin(activos)]["ID_EMPLEADO"].nunique()
    print_insight(
        f"{sin_asig} empleados activos no tienen ningún proyecto asignado actualmente. "
        "Identificarlos permite reasignar recursos infrautilizados antes de contratar "
        "nuevo personal."
    )


def plot_top_proyectos_staff(df_asg: pd.DataFrame, output_dir: str) -> None:
    """
    Gráfico de barras: Top 5 proyectos con mayor número de empleados asignados.

    Args:
        df_asg:     DataFrame de asignaciones.
        output_dir: Carpeta de salida.
    """
    top5 = (df_asg.groupby("PROYECTO")["ID_EMPLEADO"]
            .nunique()
            .nlargest(5)
            .sort_values())

    fig, ax = plt.subplots(figsize=(9, 4.5))
    colors = sns.color_palette(
        [Style.BLUE_LT, "#93C5FD", "#60A5FA", "#3B82F6", Style.BLUE], n_colors=5
    )
    bars = ax.barh(top5.index, top5.values, color=colors, height=0.6, zorder=3)

    for bar, val in zip(bars, top5.values):
        ax.text(bar.get_width() + 0.1, bar.get_y() + bar.get_height() / 2,
                f"{val} personas", va="center", fontsize=Style.FONT_ANNOT,
                color=Style.SLATE)

    styled_title(ax, "Top 5 Proyectos con Mayor Personal Asignado")
    ax.set_xlabel("Número de empleados", fontsize=Style.FONT_LABEL)
    ax.set_xlim(0, top5.max() * 1.22)
    ax.grid(axis="x", linestyle="--", alpha=0.5)
    ax.grid(axis="y", visible=False)
    fig.tight_layout()
    save_figure(fig, "10_top_proyectos_staff", output_dir)

    proyecto_lider = top5.index[-1]
    n_lider = top5.iloc[-1]
    print_insight(
        f"'{proyecto_lider}' lidera con {n_lider} empleados asignados. "
        "Los proyectos con alta concentración de recursos requieren "
        "especial atención en la gestión del riesgo de dependencia."
    )


def plot_proyectos_sin_asignacion(df_prj: pd.DataFrame, df_asg: pd.DataFrame,
                                   output_dir: str) -> None:
    """
    Detección de anomalías: proyectos activos sin ningún empleado asignado.
    Genera una tabla visual o un mensaje de ausencia de anomalías.

    Args:
        df_prj:     DataFrame de proyectos.
        df_asg:     DataFrame de asignaciones.
        output_dir: Carpeta de salida.
    """
    proyectos_activos   = set(df_prj[df_prj["ESTADO"] == "Activo"]["ID_PROYECTO"])
    proyectos_asignados = set(df_asg["ID_PROYECTO"].unique())
    sin_cobertura_ids   = proyectos_activos - proyectos_asignados

    sin_cobertura = df_prj[df_prj["ID_PROYECTO"].isin(sin_cobertura_ids)][
        ["ID_PROYECTO", "TX_DESCRIPCION", "TX_LUGAR", "F_INICIO"]
    ].copy()
    sin_cobertura["F_INICIO"] = sin_cobertura["F_INICIO"].dt.strftime("%d/%m/%Y")

    fig, ax = plt.subplots(figsize=(10, max(2.5, len(sin_cobertura) * 0.55 + 1.5)))
    ax.axis("off")

    if sin_cobertura.empty:
        ax.text(0.5, 0.5, "✓  No se detectaron proyectos activos sin personal asignado",
                ha="center", va="center", fontsize=12,
                color=Style.GREEN, fontweight="bold",
                transform=ax.transAxes)
        styled_title(ax, "Anomalías: Proyectos sin Cobertura de Personal")
    else:
        col_labels = ["ID", "Descripción", "Sede", "Inicio"]
        tabla = ax.table(
            cellText=sin_cobertura.values,
            colLabels=col_labels,
            loc="center",
            cellLoc="left",
        )
        tabla.auto_set_font_size(False)
        tabla.set_fontsize(9)
        tabla.auto_set_column_width(col=list(range(len(col_labels))))

        # Cabecera en rojo
        for j in range(len(col_labels)):
            tabla[0, j].set_facecolor(Style.RED)
            tabla[0, j].set_text_props(color=Style.WHITE, fontweight="bold")
        # Filas alternadas
        for i in range(1, len(sin_cobertura) + 1):
            bg = "#FEF2F2" if i % 2 == 0 else Style.WHITE
            for j in range(len(col_labels)):
                tabla[i, j].set_facecolor(bg)

        styled_title(ax,
                     f"⚠  Anomalía: {len(sin_cobertura)} proyecto(s) activo(s) sin personal",
                     "Requieren asignación inmediata de recursos")

    fig.tight_layout()
    save_figure(fig, "11_anomalias_sin_asignacion", output_dir)

    if sin_cobertura.empty:
        print_insight("Todos los proyectos activos cuentan con al menos un empleado asignado.")
    else:
        nombres = ", ".join(f"'{n}'" for n in sin_cobertura["TX_DESCRIPCION"])
        print_insight(
            f"Se detectaron {len(sin_cobertura)} proyecto(s) activos sin cobertura: {nombres}. "
            "Esta situación representa un riesgo operativo inmediato y debe resolverse "
            "antes del próximo ciclo de revisión."
        )


# ──────────────────────────────────────────────────────────────────────
# ORQUESTADOR PRINCIPAL
# ──────────────────────────────────────────────────────────────────────

def generate_full_report(output_dir: str = "reports") -> None:
    """
    Función principal que genera todo el informe de analítica.
    
    Carga los datos usando el DataEngine, crea los diferentes tipos de gráficos 
    (Capital humano, Proyectos y Asignaciones) y guarda los resultados en archivos PNG.
    La idea es que con estos gráficos se puedan tomar mejores decisiones en la empresa.
    """
    print("\n" + "═" * 60)
    print("  INFORME ANALÍTICO — GESTOR EMPRESARIAL")
    print("═" * 60)

    setup_style()
    engine = DataEngine()
    df_emp = engine.get_employees()
    df_prj = engine.get_projects()
    df_asg = engine.get_assignments()

    # ── Bloque 1: Capital humano ────────────────────────────────────
    print("\n── BLOQUE 1: CAPITAL HUMANO ──────────────────────────────")
    plot_antiguedad_top(df_emp, output_dir)
    plot_distribucion_antiguedad(df_emp, output_dir)
    plot_evolucion_altas_bajas(df_emp, output_dir)
    plot_estado_civil(df_emp, output_dir)
    plot_distribucion_edades(df_emp, output_dir)

    # ── Bloque 2: Proyectos ─────────────────────────────────────────
    print("\n── BLOQUE 2: CARTERA DE PROYECTOS ────────────────────────")
    plot_estado_proyectos(df_prj, output_dir)
    plot_duracion_proyectos(df_prj, output_dir)
    plot_proyectos_por_sede(df_prj, output_dir)

    # ── Bloque 3: Cruce empleado-proyecto ──────────────────────────
    print("\n── BLOQUE 3: CRUCE EMPLEADO-PROYECTO ─────────────────────")
    plot_carga_trabajo(df_emp, df_asg, output_dir)
    plot_top_proyectos_staff(df_asg, output_dir)
    plot_proyectos_sin_asignacion(df_prj, df_asg, output_dir)

    print("\n" + "═" * 60)
    print(f"  Informe completo generado en: {os.path.abspath(output_dir)}/")
    print("═" * 60 + "\n")


if __name__ == "__main__":
    generate_full_report()
