package com.educalab.civilestructuras.data.repository

import com.educalab.civilestructuras.data.local.dao.BadgeDao
import com.educalab.civilestructuras.data.local.entity.BadgeEntity
import com.educalab.civilestructuras.data.local.entity.UserBadgeEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class BadgeWithStatus(val badge: BadgeEntity, val unlocked: Boolean, val unlockedAt: Long?)

class BadgeRepository(private val badgeDao: BadgeDao) {
    fun observeAllWithStatus(): Flow<List<BadgeWithStatus>> =
        combine(badgeDao.observeAllBadges(), badgeDao.observeUnlocked()) { all, unlocked ->
            val unlockedMap = unlocked.associateBy { it.badgeId }
            all.map { badge ->
                val u = unlockedMap[badge.id]
                BadgeWithStatus(badge, u != null, u?.unlockedAt)
            }
        }
}
