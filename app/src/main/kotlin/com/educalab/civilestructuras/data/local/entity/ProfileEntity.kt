package com.educalab.civilestructuras.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Perfil local del jugador. Fila única (id fijo = 1). Nunca se pide nombre
 * real, correo ni ningún dato personal identificable: solo alias y avatar.
 */
@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = SINGLE_ID,
    val alias: String,
    val avatarId: Int,
    val soundEnabled: Boolean = true,
    val hapticEnabled: Boolean = true,
    val onboardingCompleted: Boolean = false,
    val conceptsViewed: Boolean = false,
    val materialsViewed: Boolean = false,
    val createdAt: Long
) {
    companion object {
        const val SINGLE_ID = 1
        val AVATAR_COUNT = 8
    }
}
