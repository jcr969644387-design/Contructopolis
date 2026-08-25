package com.educalab.civilestructuras.domain.model

/** Tipo de objetivo evaluable dentro de un reto de Constructópolis. */
enum class ChallengeGoalType {
    ALTURA_MINIMA,
    PRESUPUESTO_MAXIMO,
    RESISTIR_CARGA_LATERAL,
    TRIANGULACION_MINIMA,
    PESO_MAXIMO,
    ESTABILIDAD_MINIMA,
    /** Cantidad mínima de piezas con rol COLUMNA en el diseño (cuenta absoluta, no porcentaje). */
    COLUMNAS_MINIMAS,
    /** Cantidad mínima de piezas con rol VIGA en el diseño. */
    VIGAS_MINIMAS,
    /** Cantidad mínima de piezas con rol DIAGONAL en el diseño (triangulaciones reales, no un porcentaje fácil de inflar con pocas piezas). */
    DIAGONALES_MINIMAS,
    /** Cantidad mínima de materiales distintos realmente usados en el diseño construido. */
    MATERIALES_MINIMOS
}

/** Objetivo concreto de un reto: tipo + valor umbral (unidad depende del tipo). */
data class ChallengeGoal(
    val type: ChallengeGoalType,
    val value: Int
)

/** Apoyo pre-colocado por el reto (el niño no puede moverlo). */
data class PresetSupport(val position: NodePosition, val support: SupportType)

/** Carga pre-colocada por el reto (aparece ya definida en el escenario). */
data class PresetLoad(val position: NodePosition, val magnitude: Int, val isLateral: Boolean = false)

/**
 * Definición de un reto del módulo "Retos" (o de las lecciones guiadas de
 * Vigas/Columnas/Torres/Cargas, que internamente son retos con objetivos
 * específicos). worldChapter agrupa los retos en capítulos del Taller.
 */
data class StructureChallengeModel(
    val id: String,
    val order: Int,
    val worldChapter: Int,
    val title: String,
    val briefing: String,
    val gridWidth: Int,
    val gridHeight: Int,
    val fixedSupports: List<PresetSupport>,
    val presetLoads: List<PresetLoad>,
    val goals: List<ChallengeGoal>,
    val maxBudget: Int,
    val allowedMaterials: List<MaterialType>,
    val starThresholds: Pair<Int, Int> // (umbral 2 estrellas, umbral 3 estrellas) sobre stabilityScore 0-100
)
