package com.educalab.civilestructuras.data.local

import android.content.Context
import com.educalab.civilestructuras.data.local.entity.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
private data class SeedMaterial(
    val id: String, val displayName: String, val description: String,
    val strength: Double, val weight: Double, val cost: Int, val colorHex: String, val iconRes: String
)

@Serializable
private data class SeedBadge(
    val id: String, val title: String, val description: String, val iconRes: String, val tier: Int
)

@Serializable
private data class SeedGoal(val type: String, val value: Int)

@Serializable
private data class SeedSupport(val x: Int, val y: Int, val supportType: String)

@Serializable
private data class SeedLoad(val x: Int, val y: Int, val magnitude: Int, val isLateral: Boolean)

@Serializable
private data class SeedChallenge(
    val id: String, val orderInChapter: Int, val worldChapter: Int, val title: String, val briefing: String,
    val category: String, val gridWidth: Int, val gridHeight: Int, val maxBudget: Int,
    val starThreshold2: Int, val starThreshold3: Int, val allowedMaterials: List<String>, val iconRes: String,
    val goals: List<SeedGoal>, val presetSupports: List<SeedSupport>, val presetLoads: List<SeedLoad>
)

/**
 * Carga el contenido semilla (materiales, retos, insignias) desde los
 * archivos JSON de assets/seed/ la primera vez que la base de datos está
 * vacía. Es contenido versionado con la app: no requiere red ni servidores.
 */
class Seeder(private val context: Context, private val db: ConstructopolisDatabase) {

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun seedIfNeeded() {
        if (db.materialDao().count() == 0) {
            val materials = readAsset<List<SeedMaterial>>("seed/materials.json").map {
                MaterialEntity(it.id, it.displayName, it.description, it.strength, it.weight, it.cost, it.colorHex, it.iconRes)
            }
            db.materialDao().insertAll(materials)
        }
        if (db.badgeDao().count() == 0) {
            val badges = readAsset<List<SeedBadge>>("seed/badges.json").map {
                BadgeEntity(it.id, it.title, it.description, it.iconRes, it.tier)
            }
            db.badgeDao().insertAll(badges)
        }
        if (db.challengeDao().count() == 0) {
            val challenges = readAsset<List<SeedChallenge>>("seed/challenges.json")
            val challengeEntities = challenges.map {
                StructureChallengeEntity(
                    id = it.id, orderInChapter = it.orderInChapter, worldChapter = it.worldChapter,
                    title = it.title, briefing = it.briefing, category = it.category,
                    gridWidth = it.gridWidth, gridHeight = it.gridHeight, maxBudget = it.maxBudget,
                    starThreshold2 = it.starThreshold2, starThreshold3 = it.starThreshold3,
                    allowedMaterialsCsv = it.allowedMaterials.joinToString(","), iconRes = it.iconRes
                )
            }
            db.challengeDao().insertChallenges(challengeEntities)

            val goals = challenges.flatMap { c -> c.goals.map { ChallengeGoalEntity(challengeId = c.id, type = it.type, value = it.value) } }
            if (goals.isNotEmpty()) db.challengeDao().insertGoals(goals)

            val supports = challenges.flatMap { c -> c.presetSupports.map { PresetSupportEntity(challengeId = c.id, x = it.x, y = it.y, supportType = it.supportType) } }
            if (supports.isNotEmpty()) db.challengeDao().insertPresetSupports(supports)

            val loads = challenges.flatMap { c -> c.presetLoads.map { PresetLoadEntity(challengeId = c.id, x = it.x, y = it.y, magnitude = it.magnitude, isLateral = it.isLateral) } }
            if (loads.isNotEmpty()) db.challengeDao().insertPresetLoads(loads)

            // Un plano/logro coleccionable por reto, se desbloquea la primera vez que se aprueba.
            val blueprints = challengeEntities.map {
                BlueprintRewardEntity(
                    challengeId = it.id,
                    title = "Plano: ${it.title}",
                    description = "Se desbloquea al superar el reto \"${it.title}\".",
                    iconRes = "ic_blueprint_reward",
                    unlockedAt = null
                )
            }
            db.blueprintDao().insertAll(blueprints)
        }
    }

    private inline fun <reified T> readAsset(path: String): T {
        val text = context.assets.open(path).bufferedReader(Charsets.UTF_8).use { it.readText() }
        return json.decodeFromString(text)
    }
}
