package com.educalab.civilestructuras.data.local.dao

import androidx.room.*
import com.educalab.civilestructuras.data.local.entity.BadgeEntity
import com.educalab.civilestructuras.data.local.entity.UserBadgeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BadgeDao {
    @Query("SELECT * FROM badge ORDER BY tier ASC")
    fun observeAllBadges(): Flow<List<BadgeEntity>>

    @Query("SELECT COUNT(*) FROM badge")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(badges: List<BadgeEntity>)

    @Query("SELECT * FROM user_badge")
    fun observeUnlocked(): Flow<List<UserBadgeEntity>>

    @Query("SELECT badgeId FROM user_badge")
    suspend fun unlockedIds(): List<String>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun unlock(userBadge: UserBadgeEntity)
}
