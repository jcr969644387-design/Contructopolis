package com.educalab.civilestructuras.data.local.dao

import androidx.room.*
import com.educalab.civilestructuras.data.local.entity.*
import kotlinx.coroutines.flow.Flow

/** Reto completo con sus objetivos, apoyos y cargas pre-colocadas (solo lectura). */
data class ChallengeWithDetails(
    @Embedded val challenge: StructureChallengeEntity,
    @Relation(parentColumn = "id", entityColumn = "challengeId") val goals: List<ChallengeGoalEntity>,
    @Relation(parentColumn = "id", entityColumn = "challengeId") val presetSupports: List<PresetSupportEntity>,
    @Relation(parentColumn = "id", entityColumn = "challengeId") val presetLoads: List<PresetLoadEntity>
)

@Dao
interface ChallengeDao {
    @Query("SELECT COUNT(*) FROM structure_challenge")
    suspend fun count(): Int

    @Query("DELETE FROM structure_challenge WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT COUNT(*) FROM structure_challenge WHERE worldChapter = :chapter")
    suspend fun countInChapter(chapter: Int): Int

    @Query("SELECT DISTINCT challengeId FROM preset_load WHERE isLateral = 1")
    suspend fun challengeIdsWithLateralLoad(): List<String>

    @Query("SELECT * FROM structure_challenge ORDER BY worldChapter ASC, orderInChapter ASC")
    fun observeAll(): Flow<List<StructureChallengeEntity>>

    @Transaction
    @Query("SELECT * FROM structure_challenge WHERE id = :id")
    suspend fun getWithDetails(id: String): ChallengeWithDetails?

    @Transaction
    @Query("SELECT * FROM structure_challenge WHERE worldChapter = :chapter ORDER BY orderInChapter ASC")
    suspend fun getChapterWithDetails(chapter: Int): List<ChallengeWithDetails>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChallenges(challenges: List<StructureChallengeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoals(goals: List<ChallengeGoalEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPresetSupports(supports: List<PresetSupportEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPresetLoads(loads: List<PresetLoadEntity>)
}
