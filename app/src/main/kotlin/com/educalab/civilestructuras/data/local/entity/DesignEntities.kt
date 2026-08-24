package com.educalab.civilestructuras.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Un diseño guardado por el niño para un reto concreto. Se sobreescribe cada
 * vez que guarda ("Guardar y continuar") para poder retomar la sesión.
 */
@Entity(
    tableName = "structure_design",
    foreignKeys = [ForeignKey(
        entity = StructureChallengeEntity::class,
        parentColumns = ["id"], childColumns = ["challengeId"], onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("challengeId", unique = true)]
)
data class StructureDesignEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val challengeId: String,
    val createdAt: Long,
    val updatedAt: Long
)

/** Nodo del diseño. nodeKey es el identificador lógico usado por StructureEngine ("A","B"...). */
@Entity(
    tableName = "structure_node",
    foreignKeys = [ForeignKey(
        entity = StructureDesignEntity::class,
        parentColumns = ["id"], childColumns = ["designId"], onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("designId")]
)
data class StructureNodeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val designId: Long,
    val nodeKey: String,
    val x: Int,
    val y: Int,
    val supportType: String
)

/** Miembro (viga/columna/diagonal) que conecta dos nodos del diseño. */
@Entity(
    tableName = "structure_member",
    foreignKeys = [ForeignKey(
        entity = StructureDesignEntity::class,
        parentColumns = ["id"], childColumns = ["designId"], onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("designId")]
)
data class StructureMemberEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val designId: Long,
    val memberKey: String,
    val nodeAKey: String,
    val nodeBKey: String,
    val material: String,
    val role: String
)

/** Carga colocada por el niño sobre un nodo del diseño. */
@Entity(
    tableName = "load",
    foreignKeys = [ForeignKey(
        entity = StructureDesignEntity::class,
        parentColumns = ["id"], childColumns = ["designId"], onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("designId")]
)
data class LoadEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val designId: Long,
    val loadKey: String,
    val nodeKey: String,
    val magnitude: Int,
    val isLateral: Boolean
)
