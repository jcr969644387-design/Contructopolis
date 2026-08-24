package com.educalab.civilestructuras.data.local.dao

import androidx.room.*
import com.educalab.civilestructuras.data.local.entity.ProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgressDao {
    @Query("SELECT * FROM progress")
    fun observeAll(): Flow<List<ProgressEntity>>

    @Query("SELECT * FROM progress WHERE challengeId = :challengeId LIMIT 1")
    suspend fun getFor(challengeId: String): ProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(progress: ProgressEntity)

    @Query("SELECT COUNT(*) FROM progress WHERE completed = 1")
    suspend fun completedCount(): Int

    @Query(
        """
        SELECT COUNT(*) FROM progress p
        INNER JOIN structure_challenge c ON p.challengeId = c.id
        WHERE c.worldChapter = :chapter AND p.completed = 1
        """
    )
    suspend fun completedCountInChapter(chapter: Int): Int
}
