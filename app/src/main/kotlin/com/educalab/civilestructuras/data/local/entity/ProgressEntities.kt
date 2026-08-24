package com.educalab.civilestructuras.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Progreso del jugador sobre un reto concreto (1:1 con StructureChallengeEntity). */
@Entity(
    tableName = "progress",
    foreignKeys = [ForeignKey(
        entity = StructureChallengeEntity::class,
        parentColumns = ["id"], childColumns = ["challengeId"], onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("challengeId", unique = true)]
)
data class ProgressEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val challengeId: String,
    val started: Boolean = false,
    val completed: Boolean = false,
    val bestStars: Int = 0,
    val attempts: Int = 0,
    val lastPlayedAt: Long = 0L
)

/** Plano/logro ilustrado que se desbloquea al superar un reto (colección local). */
@Entity(
    tableName = "blueprint_reward",
    foreignKeys = [ForeignKey(
        entity = StructureChallengeEntity::class,
        parentColumns = ["id"], childColumns = ["challengeId"], onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("challengeId", unique = true)]
)
data class BlueprintRewardEntity(
    @PrimaryKey val challengeId: String,
    val title: String,
    val description: String,
    val iconRes: String,
    val unlockedAt: Long? = null
)

/** Catálogo de insignias (dato semilla, ver assets/seed/badges.json). */
@Entity(tableName = "badge")
data class BadgeEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val iconRes: String,
    val tier: Int
)

/** Insignias efectivamente desbloqueadas por el jugador. */
@Entity(
    tableName = "user_badge",
    foreignKeys = [ForeignKey(
        entity = BadgeEntity::class, parentColumns = ["id"], childColumns = ["badgeId"], onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("badgeId", unique = true)]
)
data class UserBadgeEntity(
    @PrimaryKey val badgeId: String,
    val unlockedAt: Long
)
