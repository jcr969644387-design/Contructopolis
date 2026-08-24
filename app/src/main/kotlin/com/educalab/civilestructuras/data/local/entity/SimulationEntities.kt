package com.educalab.civilestructuras.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Resultado guardado de una simulación (una por cada vez que el niño pulsa "Probar"). */
@Entity(
    tableName = "simulation_run",
    foreignKeys = [
        ForeignKey(entity = StructureDesignEntity::class, parentColumns = ["id"], childColumns = ["designId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = StructureChallengeEntity::class, parentColumns = ["id"], childColumns = ["challengeId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("designId"), Index("challengeId")]
)
data class SimulationRunEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val designId: Long,
    val challengeId: String,
    val ranAt: Long,
    val isConnected: Boolean,
    val isStable: Boolean,
    val passed: Boolean,
    val starsEarned: Int,
    val maxHeight: Int,
    val totalCost: Int,
    val totalWeight: Double,
    val stabilityScore: Int,
    val triangulationPercent: Int,
    val feedbackKey: String
)

/** Resultado individual por miembro de una simulación (para el feedback visual detallado). */
@Entity(
    tableName = "member_result",
    foreignKeys = [ForeignKey(
        entity = SimulationRunEntity::class,
        parentColumns = ["id"], childColumns = ["simulationRunId"], onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("simulationRunId")]
)
data class MemberResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val simulationRunId: Long,
    val memberKey: String,
    val assignedLoad: Double,
    val capacity: Double,
    val demandRatio: Double,
    val state: String
)
