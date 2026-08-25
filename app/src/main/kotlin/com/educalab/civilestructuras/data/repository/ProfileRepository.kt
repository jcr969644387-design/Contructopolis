package com.educalab.civilestructuras.data.repository

import com.educalab.civilestructuras.data.local.dao.ProfileDao
import com.educalab.civilestructuras.data.local.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow

class ProfileRepository(private val profileDao: ProfileDao) {

    fun observeProfile(): Flow<UserProfileEntity?> = profileDao.observe()

    suspend fun getOrCreateDefault(): UserProfileEntity {
        return profileDao.get() ?: UserProfileEntity(
            alias = "Ingeniera Junior",
            avatarId = 0,
            createdAt = System.currentTimeMillis()
        ).also { profileDao.upsert(it) }
    }

    suspend fun updateAlias(alias: String) {
        val current = profileDao.get() ?: return
        profileDao.upsert(current.copy(alias = alias.take(MAX_ALIAS_LENGTH)))
    }

    suspend fun updateAvatar(avatarId: Int) {
        val current = profileDao.get() ?: return
        profileDao.upsert(current.copy(avatarId = avatarId.coerceIn(0, UserProfileEntity.AVATAR_COUNT - 1)))
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        val current = profileDao.get() ?: return
        profileDao.upsert(current.copy(soundEnabled = enabled))
    }

    suspend fun setHapticEnabled(enabled: Boolean) {
        val current = profileDao.get() ?: return
        profileDao.upsert(current.copy(hapticEnabled = enabled))
    }

    suspend fun markConceptsViewed() {
        val current = profileDao.get() ?: return
        if (!current.conceptsViewed) profileDao.upsert(current.copy(conceptsViewed = true))
    }

    suspend fun markMaterialsViewed() {
        val current = profileDao.get() ?: return
        if (!current.materialsViewed) profileDao.upsert(current.copy(materialsViewed = true))
    }

    suspend fun completeOnboarding(alias: String, avatarId: Int) {
        val current = profileDao.get() ?: return
        profileDao.upsert(
            current.copy(
                alias = alias.take(MAX_ALIAS_LENGTH).ifBlank { current.alias },
                avatarId = avatarId.coerceIn(0, UserProfileEntity.AVATAR_COUNT - 1),
                onboardingCompleted = true
            )
        )
    }

    companion object {
        const val MAX_ALIAS_LENGTH = 18
    }
}
