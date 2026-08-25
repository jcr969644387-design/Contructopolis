package com.educalab.civilestructuras.domain.logic

import com.educalab.civilestructuras.domain.model.*
import kotlin.math.hypot
import kotlin.math.roundToInt

/**
 * StructureEngine — motor de simulación estructural educativa de Constructópolis.
 *
 * IMPORTANTE (honestidad de alcance, ver docs/MEMORIA_DESCRIPTIVA.md §Limitaciones):
 * Este motor NO es un solver de análisis estructural profesional (no resuelve
 * el sistema de equilibrio estático real con matrices de rigidez). Es un
 * modelo conceptual y determinista, pensado para enseñar a niños de 10-15
 * años las ideas centrales de la ingeniería civil:
 *   - una estructura debe estar conectada al suelo (apoyos) para sostenerse;
 *   - las cargas "bajan" repartiéndose entre los caminos disponibles;
 *   - los materiales tienen resistencia, peso y costo distintos;
 *   - las columnas largas pierden capacidad (esbeltez);
 *   - las cargas laterales (viento/sismo) necesitan triangulación (diagonales);
 *   - hay que balancear seguridad, presupuesto y economía de materiales.
 * Las cifras de resistencia/peso/costo son didácticas, no datos de ingeniería real.
 */
object StructureEngine {

    /** Propiedades educativas de cada material (unidades ficticias, no reales). */
    data class MaterialProps(val strength: Double, val weight: Double, val cost: Int)

    val MATERIALS: Map<MaterialType, MaterialProps> = mapOf(
        MaterialType.MADERA to MaterialProps(strength = 40.0, weight = 3.0, cost = 5),
        MaterialType.ACERO to MaterialProps(strength = 90.0, weight = 8.0, cost = 12),
        MaterialType.CONCRETO to MaterialProps(strength = 70.0, weight = 15.0, cost = 8)
    )

    private const val BASE_AREA_FACTOR = 12.0
    private const val SLENDERNESS_REFERENCE_LENGTH = 10.0
    private const val LATERAL_AMPLIFICATION = 1.6

    // ============================================================
    // CONECTIVIDAD
    // ============================================================

    /** Ids de nodos referenciados por al menos un miembro (nodos "en uso" del diseño). */
    private fun usedNodeIds(design: StructureDesign): Set<String> =
        design.members.flatMapTo(mutableSetOf()) { listOf(it.nodeAId, it.nodeBId) }

    private fun adjacency(design: StructureDesign): Map<String, List<String>> {
        val adj = mutableMapOf<String, MutableList<String>>()
        for (m in design.members) {
            adj.getOrPut(m.nodeAId) { mutableListOf() }.add(m.nodeBId)
            adj.getOrPut(m.nodeBId) { mutableListOf() }.add(m.nodeAId)
        }
        return adj
    }

    /**
     * Distancia (en número de miembros) desde cada nodo hasta el apoyo más
     * cercano, mediante BFS multi-fuente desde todos los nodos con apoyo.
     * -1 si el nodo no tiene camino hacia ningún apoyo.
     */
    fun computeDistanceToSupport(design: StructureDesign): Map<String, Int> {
        val distances = mutableMapOf<String, Int>()
        val adj = adjacency(design)
        val queue = ArrayDeque<String>()
        for (node in design.nodes) {
            if (node.support != SupportType.NINGUNO) {
                distances[node.id] = 0
                queue.add(node.id)
            }
        }
        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            val currentDist = distances.getValue(current)
            for (neighbor in adj[current].orEmpty()) {
                if (neighbor !in distances) {
                    distances[neighbor] = currentDist + 1
                    queue.add(neighbor)
                }
            }
        }
        for (node in design.nodes) {
            distances.putIfAbsent(node.id, -1)
        }
        return distances
    }

    /** Conjunto de nodos que tienen un camino estructural real hasta el suelo. */
    fun computeGroundedNodeIds(design: StructureDesign): Set<String> =
        computeDistanceToSupport(design).filterValues { it >= 0 }.keys

    /**
     * true si TODOS los nodos "en uso" (parte de al menos un miembro) están
     * conectados al suelo. Un diseño sin ningún apoyo nunca está conectado.
     * Un diseño sin miembros tampoco (no hay nada que sostener).
     */
    fun isFullyConnected(design: StructureDesign): Boolean {
        val used = usedNodeIds(design)
        if (used.isEmpty()) return false
        val hasSupport = design.nodes.any { it.support != SupportType.NINGUNO }
        if (!hasSupport) return false
        val grounded = computeGroundedNodeIds(design)
        return used.all { it in grounded }
    }

    // ============================================================
    // REPARTO DE CARGAS
    // ============================================================

    /**
     * Reparte cada carga entre los miembros "cuesta abajo" (hacia el suelo)
     * de forma recursiva: en cada nodo, el flujo de carga entrante se divide
     * a partes iguales entre todos los miembros que llevan a un nodo con
     * distancia-al-apoyo exactamente una unidad menor. Al ser estrictamente
     * decreciente en distancia, el proceso siempre termina (nunca hay bucles
     * infinitos, incluso si el grafo original tiene ciclos).
     */
    fun distributeLoads(design: StructureDesign, distances: Map<String, Int>): Map<String, Double> {
        val assigned = design.members.associate { it.id to 0.0 }.toMutableMap()
        if (design.members.isEmpty()) return assigned

        val adj = adjacency(design)
        // flujo pendiente de repartir en cada nodo, agrupado por distancia (para procesar de mayor a menor)
        val flowByNode = mutableMapOf<String, Double>()

        for (load in design.loads) {
            val nodeDist = distances[load.nodeId] ?: -1
            if (nodeDist <= 0) continue // no conectado, o ya es un apoyo: no genera demanda en miembros
            val hasDiagonalIncident = design.members.any {
                (it.nodeAId == load.nodeId || it.nodeBId == load.nodeId) && it.role == MemberRole.DIAGONAL
            }
            val magnitude = if (load.isLateral && !hasDiagonalIncident) {
                load.magnitude * LATERAL_AMPLIFICATION
            } else {
                load.magnitude.toDouble()
            }
            flowByNode[load.nodeId] = (flowByNode[load.nodeId] ?: 0.0) + magnitude
        }

        val maxDistance = distances.values.maxOrNull() ?: 0
        for (d in maxDistance downTo 1) {
            val nodesAtThisDistance = distances.filterValues { it == d }.keys
            for (nodeId in nodesAtThisDistance) {
                val flow = flowByNode[nodeId] ?: continue
                if (flow <= 0.0) continue
                val downhillMembers = design.members.filter { m ->
                    val other = when (nodeId) {
                        m.nodeAId -> m.nodeBId
                        m.nodeBId -> m.nodeAId
                        else -> null
                    }
                    other != null && distances[other] == d - 1
                }
                if (downhillMembers.isEmpty()) continue
                val share = flow / downhillMembers.size
                for (m in downhillMembers) {
                    assigned[m.id] = (assigned[m.id] ?: 0.0) + share
                    val other = if (m.nodeAId == nodeId) m.nodeBId else m.nodeAId
                    flowByNode[other] = (flowByNode[other] ?: 0.0) + share
                }
            }
        }
        return assigned
    }

    // ============================================================
    // CAPACIDAD DE MIEMBROS
    // ============================================================

    /** Longitud euclidiana de un miembro dado su diseño (en "metros de juego"). */
    fun memberLength(design: StructureDesign, member: StructureMemberModel): Double {
        val a = design.nodeById(member.nodeAId)?.position ?: return 0.0
        val b = design.nodeById(member.nodeBId)?.position ?: return 0.0
        return hypot((a.x - b.x).toDouble(), (a.y - b.y).toDouble()).coerceAtLeast(0.1)
    }

    /**
     * Capacidad de un miembro. Las vigas no sufren penalización de esbeltez
     * en este modelo simplificado (se apoyan en toda su longitud); columnas y
     * diagonales sí, con un tope de pérdida del 50% para piezas muy largas.
     */
    fun memberCapacity(member: StructureMemberModel, length: Double): Double {
        val base = MATERIALS.getValue(member.material).strength * BASE_AREA_FACTOR
        return when (member.role) {
            MemberRole.VIGA -> base
            MemberRole.COLUMNA, MemberRole.DIAGONAL -> {
                val penalty = (length / SLENDERNESS_REFERENCE_LENGTH).coerceIn(0.0, 1.0) * 0.5
                base * (1.0 - penalty)
            }
        }
    }

    // ============================================================
    // ESTADOS DE DEMANDA
    // ============================================================

    fun demandStateFor(ratio: Double): MemberDemandState = when {
        ratio <= 0.0 -> MemberDemandState.SIN_CARGA
        ratio < 0.4 -> MemberDemandState.BAJA
        ratio < 0.75 -> MemberDemandState.MEDIA
        ratio <= 1.0 -> MemberDemandState.ALTA
        else -> MemberDemandState.FALLO
    }

    // ============================================================
    // TRIANGULACIÓN
    // ============================================================

    /** Proporción de miembros que son diagonales (0.0 - 1.0). */
    fun triangulationRatio(design: StructureDesign): Double {
        if (design.members.isEmpty()) return 0.0
        val diagonals = design.members.count { it.role == MemberRole.DIAGONAL }
        return diagonals.toDouble() / design.members.size
    }

    // ============================================================
    // SIMULACIÓN COMPLETA
    // ============================================================

    /**
     * true si todos los apoyos del reto están realmente incorporados a la
     * estructura construida (tocados por al menos un miembro). Sin esto, un
     * diseño podía "aprobar" apoyándose en un solo apoyo y dejando el resto
     * sin usar: piezas colgando de un lado, no una estructura real cerrada
     * entre sus puntos de anclaje (ej. una columna con vigas sueltas encima,
     * sin llegar nunca al segundo apoyo).
     */
    fun allSupportsUsed(design: StructureDesign): Boolean {
        val supportNodes = design.nodes.filter { it.support != SupportType.NINGUNO }
        if (supportNodes.isEmpty()) return true
        val used = usedNodeIds(design)
        return supportNodes.all { it.id in used }
    }

    fun simulate(design: StructureDesign, challenge: StructureChallengeModel): SimulationResultModel {
        val isConnected = isFullyConnected(design)
        val allSupportsUsed = allSupportsUsed(design)
        val grounded = computeGroundedNodeIds(design)
        val distances = computeDistanceToSupport(design)
        val assignedLoads = distributeLoads(design, distances)

        val memberResults = design.members.map { member ->
            val length = memberLength(design, member)
            val capacity = memberCapacity(member, length)
            val assigned = assignedLoads[member.id] ?: 0.0
            val ratio = if (capacity > 0.0) assigned / capacity else 0.0
            MemberResultModel(
                memberId = member.id,
                assignedLoad = assigned,
                capacity = capacity,
                demandRatio = ratio,
                state = demandStateFor(ratio)
            )
        }
        val failedMemberIds = memberResults.filter { it.state == MemberDemandState.FALLO }.map { it.memberId }

        val totalWeight = design.members.sumOf { memberLength(design, it) * MATERIALS.getValue(it.material).weight }
        val totalCostDouble = design.members.sumOf { memberLength(design, it) * MATERIALS.getValue(it.material).cost }
        val totalCost = totalCostDouble.roundToInt()

        val maxHeight = grounded.mapNotNull { id -> design.nodeById(id)?.position?.y }.maxOrNull() ?: 0
        val triangulation = triangulationRatio(design)
        val triangulationPercent = (triangulation * 100).roundToInt()

        val hasLateralLoad = design.loads.any { it.isLateral }
        var stabilityScore = 100
        if (!isConnected) {
            stabilityScore = 0
        } else {
            val failedPenalty = if (memberResults.isNotEmpty()) {
                (failedMemberIds.size.toDouble() / memberResults.size) * 60.0
            } else 60.0
            val highDemandCount = memberResults.count { it.state == MemberDemandState.ALTA }
            val highDemandPenalty = if (memberResults.isNotEmpty()) {
                (highDemandCount.toDouble() / memberResults.size) * 15.0
            } else 0.0
            val lateralPenalty = if (hasLateralLoad && triangulation == 0.0) 25.0 else 0.0
            stabilityScore = (100.0 - failedPenalty - highDemandPenalty - lateralPenalty).roundToInt()
        }
        stabilityScore = stabilityScore.coerceIn(0, 100)
        val isStable = isConnected && failedMemberIds.isEmpty() && stabilityScore >= 55

        // Evaluación de objetivos del reto
        val unmetGoals = mutableListOf<ChallengeGoalType>()
        for (goal in challenge.goals) {
            val met = when (goal.type) {
                ChallengeGoalType.ALTURA_MINIMA -> maxHeight >= goal.value
                ChallengeGoalType.PRESUPUESTO_MAXIMO -> totalCost <= goal.value
                ChallengeGoalType.RESISTIR_CARGA_LATERAL -> isStable
                ChallengeGoalType.TRIANGULACION_MINIMA -> triangulationPercent >= goal.value
                ChallengeGoalType.PESO_MAXIMO -> totalWeight <= goal.value
                ChallengeGoalType.ESTABILIDAD_MINIMA -> stabilityScore >= goal.value
                ChallengeGoalType.COLUMNAS_MINIMAS -> design.members.count { it.role == MemberRole.COLUMNA } >= goal.value
                ChallengeGoalType.VIGAS_MINIMAS -> design.members.count { it.role == MemberRole.VIGA } >= goal.value
                ChallengeGoalType.DIAGONALES_MINIMAS -> design.members.count { it.role == MemberRole.DIAGONAL } >= goal.value
                ChallengeGoalType.MATERIALES_MINIMOS -> design.members.map { it.material }.distinct().size >= goal.value
            }
            if (!met) unmetGoals.add(goal.type)
        }

        val passed = isConnected &&
            allSupportsUsed &&
            failedMemberIds.isEmpty() &&
            totalCost <= challenge.maxBudget &&
            unmetGoals.isEmpty()

        val starsEarned = when {
            !passed -> 0
            stabilityScore >= challenge.starThresholds.second -> 3
            stabilityScore >= challenge.starThresholds.first -> 2
            else -> 1
        }

        val feedbackKey = when {
            !isConnected -> "feedback_no_conectado"
            !allSupportsUsed -> "feedback_apoyo_sin_usar"
            failedMemberIds.isNotEmpty() -> "feedback_miembro_fallido"
            totalCost > challenge.maxBudget -> "feedback_presupuesto_excedido"
            unmetGoals.contains(ChallengeGoalType.ALTURA_MINIMA) -> "feedback_altura_insuficiente"
            unmetGoals.contains(ChallengeGoalType.DIAGONALES_MINIMAS) || unmetGoals.contains(ChallengeGoalType.TRIANGULACION_MINIMA) -> "feedback_falta_triangulacion"
            unmetGoals.contains(ChallengeGoalType.COLUMNAS_MINIMAS) -> "feedback_faltan_columnas"
            unmetGoals.contains(ChallengeGoalType.VIGAS_MINIMAS) -> "feedback_faltan_vigas"
            unmetGoals.contains(ChallengeGoalType.MATERIALES_MINIMOS) -> "feedback_falta_variedad_material"
            unmetGoals.isNotEmpty() -> "feedback_objetivo_pendiente"
            starsEarned == 3 -> "feedback_excelente"
            starsEarned == 2 -> "feedback_solido"
            passed -> "feedback_aprobado"
            else -> "feedback_generico"
        }

        return SimulationResultModel(
            isConnected = isConnected,
            isStable = isStable,
            passed = passed,
            starsEarned = starsEarned,
            maxHeight = maxHeight,
            totalCost = totalCost,
            totalWeight = totalWeight,
            stabilityScore = stabilityScore,
            triangulationPercent = triangulationPercent,
            memberResults = memberResults,
            failedMemberIds = failedMemberIds,
            unmetGoals = unmetGoals,
            feedbackKey = feedbackKey
        )
    }
}
