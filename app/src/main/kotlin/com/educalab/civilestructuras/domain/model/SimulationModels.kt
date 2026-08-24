package com.educalab.civilestructuras.domain.model

/** Estado visual de demanda de un miembro (color + icono en la UI, nunca solo color). */
enum class MemberDemandState { SIN_CARGA, BAJA, MEDIA, ALTA, FALLO }

/** Resultado individual de un miembro tras la simulación. */
data class MemberResultModel(
    val memberId: String,
    val assignedLoad: Double,
    val capacity: Double,
    val demandRatio: Double,
    val state: MemberDemandState
)

/**
 * Resultado completo de simular un [StructureDesign] contra un
 * [StructureChallengeModel]. Es lo que la UI del Constructor/Simulación
 * muestra al niño: nunca solo "correcto/incorrecto".
 */
data class SimulationResultModel(
    val isConnected: Boolean,
    val isStable: Boolean,
    val passed: Boolean,
    val starsEarned: Int,
    val maxHeight: Int,
    val totalCost: Int,
    val totalWeight: Double,
    val stabilityScore: Int,
    val triangulationPercent: Int,
    val memberResults: List<MemberResultModel>,
    val failedMemberIds: List<String>,
    val unmetGoals: List<ChallengeGoalType>,
    val feedbackKey: String
)
