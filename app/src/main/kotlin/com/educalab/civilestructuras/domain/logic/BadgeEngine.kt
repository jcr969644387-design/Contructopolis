package com.educalab.civilestructuras.domain.logic

import com.educalab.civilestructuras.domain.model.MaterialType

/** Identificadores estables de las insignias/planos de Constructópolis. */
enum class BadgeId {
    PRIMER_LADRILLO,
    MAESTRA_DEL_ACERO,
    ARQUITECTA_DE_MADERA,
    TORRE_AL_CIELO,
    TRIANGULACION_PERFECTA,
    INGENIERA_AHORRADORA,
    RESISTENTE_AL_VIENTO,
    CAPITULO_UNO_COMPLETO,
    MAESTRA_CONSTRUCTORA,
    PERFECCIONISTA
}

/**
 * Estadísticas acumuladas del jugador, derivadas de [com.educalab.civilestructuras.data.local.entity]
 * (persistidas en Room). Es una foto de solo-lectura que BadgeEngine usa para decidir
 * qué insignias desbloquear; nunca se genera aleatoriamente, siempre a partir de acciones reales.
 */
data class PlayerStats(
    val totalChallengesCompleted: Int = 0,
    val challengesCompletedChapter1: Int = 0,
    val totalChallengesInChapter1: Int = 0,
    val maxHeightEverBuilt: Int = 0,
    val bestTriangulationPercent: Int = 0,
    val designsPassedWithOnlyMaterial: Map<MaterialType, Int> = emptyMap(),
    val cheapestPassRatio: Double = 1.0, // totalCost / maxBudget de la mejor (más baja) aprobación
    val lateralChallengesPassed: Int = 0,
    val threeStarChallenges: Int = 0
)

object BadgeEngine {

    /** Evalúa de forma determinista qué insignias corresponden a estas estadísticas. */
    fun evaluateEarnedBadges(stats: PlayerStats): Set<BadgeId> {
        val earned = mutableSetOf<BadgeId>()
        if (stats.totalChallengesCompleted >= 1) earned += BadgeId.PRIMER_LADRILLO
        if ((stats.designsPassedWithOnlyMaterial[MaterialType.ACERO] ?: 0) >= 5) earned += BadgeId.MAESTRA_DEL_ACERO
        if ((stats.designsPassedWithOnlyMaterial[MaterialType.MADERA] ?: 0) >= 5) earned += BadgeId.ARQUITECTA_DE_MADERA
        if (stats.maxHeightEverBuilt >= 20) earned += BadgeId.TORRE_AL_CIELO
        if (stats.bestTriangulationPercent >= 50) earned += BadgeId.TRIANGULACION_PERFECTA
        if (stats.cheapestPassRatio <= 0.5) earned += BadgeId.INGENIERA_AHORRADORA
        if (stats.lateralChallengesPassed >= 3) earned += BadgeId.RESISTENTE_AL_VIENTO
        if (stats.totalChallengesInChapter1 > 0 && stats.challengesCompletedChapter1 >= stats.totalChallengesInChapter1) {
            earned += BadgeId.CAPITULO_UNO_COMPLETO
        }
        if (stats.totalChallengesCompleted >= 20) earned += BadgeId.MAESTRA_CONSTRUCTORA
        if (stats.threeStarChallenges >= 10) earned += BadgeId.PERFECCIONISTA
        return earned
    }

    /** Insignias nuevas obtenidas al pasar de [before] a [after] (para animar el desbloqueo). */
    fun newlyEarned(before: PlayerStats, after: PlayerStats): Set<BadgeId> =
        evaluateEarnedBadges(after) - evaluateEarnedBadges(before)
}

/** Estado visual de un módulo/reto dentro del mapa del Taller. */
enum class ModuleState { BLOQUEADO, DISPONIBLE, INICIADO, COMPLETADO, DOMINADO }

object ProgressEngine {

    /**
     * Calcula el estado de un reto según su posición (order) dentro del capítulo,
     * si ya fue iniciado/completado, y si el reto anterior fue superado (progresión
     * gradual: no se desbloquea todo desde el principio, pero el reto nº1 de cada
     * capítulo siempre está disponible).
     */
    fun stateFor(
        order: Int,
        previousOrderCompleted: Boolean,
        started: Boolean,
        completed: Boolean,
        masteredWithThreeStars: Boolean
    ): ModuleState = when {
        masteredWithThreeStars -> ModuleState.DOMINADO
        completed -> ModuleState.COMPLETADO
        started -> ModuleState.INICIADO
        order == 1 || previousOrderCompleted -> ModuleState.DISPONIBLE
        else -> ModuleState.BLOQUEADO
    }

    /** Progreso global 0-100 en función de retos completados sobre el total disponible. */
    fun overallProgressPercent(completed: Int, total: Int): Int {
        if (total <= 0) return 0
        return ((completed.toDouble() / total) * 100).toInt().coerceIn(0, 100)
    }
}
