package com.educalab.civilestructuras.data.repository

import com.educalab.civilestructuras.data.local.dao.ChallengeDao
import com.educalab.civilestructuras.data.local.dao.ChallengeWithDetails
import com.educalab.civilestructuras.data.local.dao.ProgressDao
import com.educalab.civilestructuras.data.local.entity.ProgressEntity
import com.educalab.civilestructuras.data.local.entity.StructureChallengeEntity
import com.educalab.civilestructuras.domain.logic.ModuleState
import com.educalab.civilestructuras.domain.logic.ProgressEngine
import com.educalab.civilestructuras.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

/** Reto + su estado visual de progreso, listo para pintar en el mapa del Taller. */
data class ChallengeSummary(
    val challenge: StructureChallengeEntity,
    val state: ModuleState,
    val bestStars: Int
)

class ChallengeRepository(
    private val challengeDao: ChallengeDao,
    private val progressDao: ProgressDao
) {
    fun observeSummariesByChapter(): Flow<Map<Int, List<ChallengeSummary>>> =
        combine(challengeDao.observeAll(), progressDao.observeAll()) { challenges, progressList ->
            val progressByChallenge = progressList.associateBy { it.challengeId }
            val grouped = challenges.groupBy { it.worldChapter }
            grouped.mapValues { (_, list) ->
                val ordered = list.sortedBy { it.orderInChapter }
                ordered.mapIndexed { index, challenge ->
                    val progress = progressByChallenge[challenge.id]
                    val previousCompleted = if (index == 0) true else {
                        val prev = ordered[index - 1]
                        progressByChallenge[prev.id]?.completed == true
                    }
                    val state = ProgressEngine.stateFor(
                        order = challenge.orderInChapter,
                        previousOrderCompleted = previousCompleted,
                        started = progress?.started == true,
                        completed = progress?.completed == true,
                        masteredWithThreeStars = (progress?.bestStars ?: 0) == 3
                    )
                    ChallengeSummary(challenge, state, progress?.bestStars ?: 0)
                }
            }
        }

    suspend fun getChallengeModel(challengeId: String): StructureChallengeModel? {
        val details = challengeDao.getWithDetails(challengeId) ?: return null
        return details.toDomainModel()
    }

    suspend fun overallProgressPercent(): Int {
        val total = challengeDao.count()
        val completed = progressDao.completedCount()
        return ProgressEngine.overallProgressPercent(completed, total)
    }

    suspend fun markStarted(challengeId: String) {
        val existing = progressDao.getFor(challengeId)
        progressDao.upsert(
            (existing ?: ProgressEntity(challengeId = challengeId)).copy(
                started = true,
                lastPlayedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun recordAttempt(challengeId: String, passed: Boolean, stars: Int) {
        val existing = progressDao.getFor(challengeId) ?: ProgressEntity(challengeId = challengeId)
        progressDao.upsert(
            existing.copy(
                started = true,
                completed = existing.completed || passed,
                bestStars = maxOf(existing.bestStars, stars),
                attempts = existing.attempts + 1,
                lastPlayedAt = System.currentTimeMillis()
            )
        )
    }
}

private fun ChallengeWithDetails.toDomainModel(): StructureChallengeModel {
    val allowedMaterials = challenge.allowedMaterialsCsv.split(",")
        .filter { it.isNotBlank() }
        .map { MaterialType.valueOf(it) }
    return StructureChallengeModel(
        id = challenge.id,
        order = challenge.orderInChapter,
        worldChapter = challenge.worldChapter,
        title = challenge.title,
        briefing = challenge.briefing,
        gridWidth = challenge.gridWidth,
        gridHeight = challenge.gridHeight,
        fixedSupports = presetSupports.map { PresetSupport(NodePosition(it.x, it.y), SupportType.valueOf(it.supportType)) },
        presetLoads = presetLoads.map { PresetLoad(NodePosition(it.x, it.y), it.magnitude, it.isLateral) },
        goals = goals.map { ChallengeGoal(ChallengeGoalType.valueOf(it.type), it.value) },
        maxBudget = challenge.maxBudget,
        allowedMaterials = allowedMaterials,
        starThresholds = Pair(challenge.starThreshold2, challenge.starThreshold3)
    )
}
