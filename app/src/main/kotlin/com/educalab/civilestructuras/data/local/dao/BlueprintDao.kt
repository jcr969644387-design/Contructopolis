package com.educalab.civilestructuras.data.local.dao

import androidx.room.*
import com.educalab.civilestructuras.data.local.entity.BlueprintRewardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BlueprintDao {
    @Query("SELECT * FROM blueprint_reward ORDER BY challengeId ASC")
    fun observeAll(): Flow<List<BlueprintRewardEntity>>

    @Query("SELECT COUNT(*) FROM blueprint_reward")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(rewards: List<BlueprintRewardEntity>)

    @Query("UPDATE blueprint_reward SET unlockedAt = :unlockedAt WHERE challengeId = :challengeId AND unlockedAt IS NULL")
    suspend fun markUnlocked(challengeId: String, unlockedAt: Long)
}
