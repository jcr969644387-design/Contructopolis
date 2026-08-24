package com.educalab.civilestructuras.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Definición de un reto del Taller (dato semilla, ver assets/seed/challenges.json). */
@Entity(tableName = "structure_challenge")
data class StructureChallengeEntity(
    @PrimaryKey val id: String,
    val orderInChapter: Int,
    val worldChapter: Int,
    val title: String,
    val briefing: String,
    val category: String,        // "VIGA" | "COLUMNA" | "TORRE" | "CARGA" | "RETO"
    val gridWidth: Int,
    val gridHeight: Int,
    val maxBudget: Int,
    val starThreshold2: Int,
    val starThreshold3: Int,
    val allowedMaterialsCsv: String,
    val iconRes: String
)

/** Objetivo evaluable de un reto (N:1 con el reto). */
@Entity(
    tableName = "challenge_goal",
    foreignKeys = [ForeignKey(
        entity = StructureChallengeEntity::class,
        parentColumns = ["id"], childColumns = ["challengeId"], onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("challengeId")]
)
data class ChallengeGoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val challengeId: String,
    val type: String,
    val value: Int
)

/** Apoyo pre-colocado (no editable) de un reto. */
@Entity(
    tableName = "preset_support",
    foreignKeys = [ForeignKey(
        entity = StructureChallengeEntity::class,
        parentColumns = ["id"], childColumns = ["challengeId"], onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("challengeId")]
)
data class PresetSupportEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val challengeId: String,
    val x: Int,
    val y: Int,
    val supportType: String
)

/** Carga pre-colocada (no editable) de un reto. */
@Entity(
    tableName = "preset_load",
    foreignKeys = [ForeignKey(
        entity = StructureChallengeEntity::class,
        parentColumns = ["id"], childColumns = ["challengeId"], onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("challengeId")]
)
data class PresetLoadEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val challengeId: String,
    val x: Int,
    val y: Int,
    val magnitude: Int,
    val isLateral: Boolean
)
