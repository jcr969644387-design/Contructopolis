package com.educalab.civilestructuras.data.repository

import com.educalab.civilestructuras.data.local.dao.BadgeDao
import com.educalab.civilestructuras.data.local.dao.BlueprintDao
import com.educalab.civilestructuras.data.local.dao.ChallengeDao
import com.educalab.civilestructuras.data.local.dao.ProgressDao
import com.educalab.civilestructuras.data.local.dao.SimulationDao
import com.educalab.civilestructuras.data.local.entity.MemberResultEntity
import com.educalab.civilestructuras.data.local.entity.SimulationRunEntity
import com.educalab.civilestructuras.data.local.entity.UserBadgeEntity
import com.educalab.civilestructuras.domain.logic.BadgeEngine
import com.educalab.civilestructuras.domain.logic.BadgeId
import com.educalab.civilestructuras.domain.logic.PlayerStats
import com.educalab.civilestructuras.domain.logic.StructureEngine
import com.educalab.civilestructuras.domain.model.MaterialType
import com.educalab.civilestructuras.domain.model.SimulationResultModel
import com.educalab.civilestructuras.domain.model.StructureChallengeModel
import com.educalab.civilestructuras.domain.model.StructureDesign

/** Resultado de "Probar" un diseño: el feedback de la simulación + qué se desbloqueó de verdad. */
data class SimulationOutcome(
    val result: SimulationResultModel,
    val newlyUnlockedBadges: Set<BadgeId>,
    val blueprintUnlocked: Boolean
)

/** Capítulo introductorio ("Cimientos") usado para la insignia de capítulo completo. */
private const val FIRST_CHAPTER = 1

class SimulationRepository(
    private val designRepository: DesignRepository,
    private val simulationDao: SimulationDao,
    private val challengeRepository: ChallengeRepository,
    private val blueprintDao: BlueprintDao,
    private val badgeDao: BadgeDao,
    private val challengeDao: ChallengeDao,
    private val progressDao: ProgressDao
) {

    /** Ejecuta el motor sobre el diseño actual del reto, persiste todo y evalúa insignias reales. */
    suspend fun runSimulation(challenge: StructureChallengeModel, design: StructureDesign): SimulationOutcome {
        val result = StructureEngine.simulate(design, challenge)

        val statsBefore = currentPlayerStats()
        designRepository.saveDesign(design)
        val designEntityId = requireNotNull(designRepository.getDesignEntityId(challenge.id)) {
            "El diseño debe existir antes de simular"
        }

        val runEntity = SimulationRunEntity(
            designId = designEntityId,
            challengeId = challenge.id,
            ranAt = System.currentTimeMillis(),
            isConnected = result.isConnected,
            isStable = result.isStable,
            passed = result.passed,
            starsEarned = result.starsEarned,
            maxHeight = result.maxHeight,
            totalCost = result.totalCost,
            totalWeight = result.totalWeight,
            stabilityScore = result.stabilityScore,
            triangulationPercent = result.triangulationPercent,
            feedbackKey = result.feedbackKey
        )
        val memberResults = result.memberResults.map {
            MemberResultEntity(
                simulationRunId = 0, memberKey = it.memberId, assignedLoad = it.assignedLoad,
                capacity = it.capacity, demandRatio = it.demandRatio, state = it.state.name
            )
        }
        simulationDao.saveRunWithMembers(runEntity, memberResults)
        challengeRepository.recordAttempt(challenge.id, result.passed, result.starsEarned)

        var blueprintUnlocked = false
        if (result.passed) {
            blueprintDao.markUnlocked(challenge.id, System.currentTimeMillis())
            blueprintUnlocked = true
        }

        val statsAfter = currentPlayerStats()
        val newlyEarned = BadgeEngine.newlyEarned(statsBefore, statsAfter)
        if (newlyEarned.isNotEmpty()) {
            val now = System.currentTimeMillis()
            newlyEarned.forEach { badgeDao.unlock(UserBadgeEntity(it.name, now)) }
        }

        return SimulationOutcome(result, newlyEarned, blueprintUnlocked)
    }

    /**
     * Reconstruye las estadísticas reales del jugador a partir de lo persistido en Room.
     * Nunca se inventan valores: cada campo viene de una consulta SQL sobre acciones reales.
     */
    private suspend fun currentPlayerStats(): PlayerStats {
        val passedRuns = simulationDao.distinctPassedRuns().distinctBy { it.challengeId }
        val perMaterial = mutableMapOf<MaterialType, Int>()
        for (run in passedRuns) {
            val materials = designRepository.getDistinctMaterials(run.designId)
            if (materials.size == 1) {
                val mat = materials[0]
                perMaterial[mat] = (perMaterial[mat] ?: 0) + 1
            }
        }
        val lateralChallengeIds = challengeDao.challengeIdsWithLateralLoad().toSet()
        val lateralPassed = passedRuns.count { it.challengeId in lateralChallengeIds }

        val costVsBudget = simulationDao.passedCostVsBudget()
        val cheapestRatio = costVsBudget.minOfOrNull { it.totalCost.toDouble() / it.maxBudget.coerceAtLeast(1) } ?: 1.0

        return PlayerStats(
            totalChallengesCompleted = passedRuns.size,
            challengesCompletedChapter1 = progressDao.completedCountInChapter(FIRST_CHAPTER),
            totalChallengesInChapter1 = challengeDao.countInChapter(FIRST_CHAPTER),
            maxHeightEverBuilt = simulationDao.maxHeightEverPassed() ?: 0,
            bestTriangulationPercent = simulationDao.bestTriangulationPercent() ?: 0,
            designsPassedWithOnlyMaterial = perMaterial,
            cheapestPassRatio = cheapestRatio,
            lateralChallengesPassed = lateralPassed,
            threeStarChallenges = simulationDao.threeStarCount()
        )
    }
}
