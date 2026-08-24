package com.educalab.civilestructuras.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Catálogo de materiales del Taller (dato semilla, ver assets/seed/materials.json). */
@Entity(tableName = "material")
data class MaterialEntity(
    @PrimaryKey val id: String,            // "MADERA" | "ACERO" | "CONCRETO"
    val displayName: String,
    val description: String,
    val strength: Double,
    val weight: Double,
    val cost: Int,
    val colorHex: String,
    val iconRes: String
)
