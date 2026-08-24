package com.educalab.civilestructuras.data.local.dao

import androidx.room.*
import com.educalab.civilestructuras.data.local.entity.MemberResultEntity
import com.educalab.civilestructuras.data.local.entity.SimulationRunEntity
import kotlinx.coroutines.flow.Flow

data class SimulationRunWithMembers(
    @Embedded val run: SimulationRunEntity,
    @Relation(parentColumn = "id", entityColumn = "simulationRunId") val memberResults: List<MemberResultEntity>
)

@Dao
interface SimulationDao {
    @Insert
    suspend fun insertRun(run: SimulationRunEntity): Long

    @Insert
    suspend fun insertMemberResults(results: List<MemberResultEntity>)

    @Transaction
    suspend fun saveRunWithMembers(run: SimulationRunEntity, results: List<MemberResultEntity>): Long {
        val runId = insertRun(run)
        if (results.isNotEmpty()) insertMemberResults(results.map { it.copy(simulationRunId = runId) })
        return runId
    }

    @Transaction
    @Query("SELECT * FROM simulation_run WHERE challengeId = :challengeId ORDER BY ranAt DESC LIMIT 1")
    fun observeLastRunForChallenge(challengeId: String): Flow<SimulationRunWithMembers?>

    @Query("SELECT MAX(starsEarned) FROM simulation_run WHERE challengeId = :challengeId AND passed = 1")
    suspend fun bestStarsFor(challengeId: String): Int?

    @Query("SELECT COUNT(*) FROM simulation_run WHERE challengeId = :challengeId")
    suspend fun attemptsFor(challengeId: String): Int

    @Query("SELECT MAX(maxHeight) FROM simulation_run WHERE passed = 1")
    suspend fun maxHeightEverPassed(): Int?

    @Query("SELECT MAX(triangulationPercent) FROM simulation_run WHERE passed = 1")
    suspend fun bestTriangulationPercent(): Int?

    @Query("SELECT COUNT(*) FROM simulation_run WHERE passed = 1 AND starsEarned = 3")
    suspend fun threeStarCount(): Int

    /** Un (challengeId, designId) por cada reto que el jugador superó alguna vez (para insignias). */
    @Query("SELECT DISTINCT challengeId, designId FROM simulation_run WHERE passed = 1")
    suspend fun distinctPassedRuns(): List<PassedRunRef>

    @Query(
        """
        SELECT sr.totalCost as totalCost, sc.maxBudget as maxBudget
        FROM simulation_run sr
        INNER JOIN structure_challenge sc ON sr.challengeId = sc.id
        WHERE sr.passed = 1
        """
    )
    suspend fun passedCostVsBudget(): List<CostVsBudget>
}

data class PassedRunRef(val challengeId: String, val designId: Long)
data class CostVsBudget(val totalCost: Int, val maxBudget: Int)
