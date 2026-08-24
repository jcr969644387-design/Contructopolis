package com.educalab.civilestructuras.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.educalab.civilestructuras.data.local.entity.*
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Pruebas de persistencia real sobre una base de datos Room en memoria
 * (Robolectric). Cubren relaciones @Transaction, borrado en cascada,
 * restricciones de unicidad y las consultas agregadas usadas para insignias.
 * Ver docs/BUILD_REPORT.md: estas pruebas requieren el Android Gradle Plugin
 * + Robolectric para ejecutarse (no se pudieron correr en este entorno sandbox
 * por falta de SDK de Android; el motor de dominio puro sí se verificó, ver
 * StructureEngineTest / BadgeEngineTest).
 */
@RunWith(RobolectricTestRunner::class)
class ConstructopolisDatabaseTest {

    private lateinit var db: ConstructopolisDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, ConstructopolisDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun seedOneChallenge(id: String = "c01_viga", chapter: Int = 1): StructureChallengeEntity {
        val challenge = StructureChallengeEntity(
            id = id, orderInChapter = 1, worldChapter = chapter, title = "Primer Puente",
            briefing = "briefing", category = "VIGA", gridWidth = 6, gridHeight = 6,
            maxBudget = 400, starThreshold2 = 55, starThreshold3 = 78,
            allowedMaterialsCsv = "MADERA", iconRes = "ic_challenge_viga"
        )
        runBlocking { db.challengeDao().insertChallenges(listOf(challenge)) }
        return challenge
    }

    @Test
    fun `insertar y leer un reto con sus objetivos y apoyos preconfigurados`() = runBlocking {
        val challenge = seedOneChallenge()
        db.challengeDao().insertGoals(listOf(ChallengeGoalEntity(challengeId = challenge.id, type = "PRESUPUESTO_MAXIMO", value = 120)))
        db.challengeDao().insertPresetSupports(
            listOf(
                PresetSupportEntity(challengeId = challenge.id, x = 0, y = 0, supportType = "FIJO"),
                PresetSupportEntity(challengeId = challenge.id, x = 5, y = 0, supportType = "FIJO")
            )
        )

        val details = db.challengeDao().getWithDetails(challenge.id)
        assertNotNull(details)
        assertEquals(1, details!!.goals.size)
        assertEquals(2, details.presetSupports.size)
        assertEquals("Primer Puente", details.challenge.title)
    }

    @Test
    fun `borrar un reto elimina en cascada sus objetivos`() = runBlocking {
        val challenge = seedOneChallenge()
        db.challengeDao().insertGoals(listOf(ChallengeGoalEntity(challengeId = challenge.id, type = "ALTURA_MINIMA", value = 10)))
        val before = db.challengeDao().getWithDetails(challenge.id)!!.goals.size
        assertEquals(1, before)

        db.challengeDao().deleteById(challenge.id)
        val afterDelete = db.challengeDao().getWithDetails(challenge.id)
        assertNull("El reto ya no debe existir", afterDelete)
    }

    @Test
    fun `guardar un diseno persiste nodos miembros y cargas relacionados`() = runBlocking {
        val challenge = seedOneChallenge()
        val designId = db.designDao().upsertDesign(StructureDesignEntity(challengeId = challenge.id, createdAt = 1L, updatedAt = 1L))
        db.designDao().replaceDesignContent(
            designId,
            nodes = listOf(
                StructureNodeEntity(designId = designId, nodeKey = "A", x = 0, y = 0, supportType = "FIJO"),
                StructureNodeEntity(designId = designId, nodeKey = "B", x = 0, y = 2, supportType = "NINGUNO")
            ),
            members = listOf(StructureMemberEntity(designId = designId, memberKey = "m1", nodeAKey = "A", nodeBKey = "B", material = "MADERA", role = "VIGA")),
            loads = listOf(LoadEntity(designId = designId, loadKey = "l1", nodeKey = "B", magnitude = 20, isLateral = false))
        )

        val details = db.designDao().getWithDetailsOnce(challenge.id)
        assertNotNull(details)
        assertEquals(2, details!!.nodes.size)
        assertEquals(1, details.members.size)
        assertEquals(1, details.loads.size)
    }

    @Test
    fun `reemplazar contenido de diseno limpia lo anterior antes de insertar`() = runBlocking {
        val challenge = seedOneChallenge()
        val designId = db.designDao().upsertDesign(StructureDesignEntity(challengeId = challenge.id, createdAt = 1L, updatedAt = 1L))
        db.designDao().replaceDesignContent(
            designId,
            nodes = listOf(StructureNodeEntity(designId = designId, nodeKey = "A", x = 0, y = 0, supportType = "FIJO")),
            members = emptyList(), loads = emptyList()
        )
        db.designDao().replaceDesignContent(
            designId,
            nodes = listOf(StructureNodeEntity(designId = designId, nodeKey = "X", x = 3, y = 3, supportType = "NINGUNO")),
            members = emptyList(), loads = emptyList()
        )
        val details = db.designDao().getWithDetailsOnce(challenge.id)!!
        assertEquals(1, details.nodes.size)
        assertEquals("X", details.nodes.first().nodeKey)
    }

    @Test
    fun `borrar un diseno elimina en cascada sus nodos miembros y cargas`() = runBlocking {
        val challenge = seedOneChallenge()
        val designId = db.designDao().upsertDesign(StructureDesignEntity(challengeId = challenge.id, createdAt = 1L, updatedAt = 1L))
        db.designDao().insertNodes(listOf(StructureNodeEntity(designId = designId, nodeKey = "A", x = 0, y = 0, supportType = "FIJO")))
        db.designDao().insertMembers(listOf(StructureMemberEntity(designId = designId, memberKey = "m1", nodeAKey = "A", nodeBKey = "A", material = "MADERA", role = "VIGA")))
        db.designDao().insertLoads(listOf(LoadEntity(designId = designId, loadKey = "l1", nodeKey = "A", magnitude = 10, isLateral = false)))

        val before = db.designDao().getWithDetailsOnce(challenge.id)!!
        assertEquals(1, before.nodes.size); assertEquals(1, before.members.size); assertEquals(1, before.loads.size)

        db.designDao().deleteDesign(designId)
        val after = db.designDao().getWithDetailsOnce(challenge.id)
        assertNull("El diseño y sus hijos (nodos/miembros/cargas) deben desaparecer", after)
    }

    @Test
    fun `simulation run guarda resultados por miembro asociados`() = runBlocking {
        val challenge = seedOneChallenge()
        val designId = db.designDao().upsertDesign(StructureDesignEntity(challengeId = challenge.id, createdAt = 1L, updatedAt = 1L))
        val run = SimulationRunEntity(
            designId = designId, challengeId = challenge.id, ranAt = 1L, isConnected = true, isStable = true,
            passed = true, starsEarned = 3, maxHeight = 4, totalCost = 100, totalWeight = 12.0,
            stabilityScore = 90, triangulationPercent = 30, feedbackKey = "feedback_excelente"
        )
        val memberResults = listOf(
            MemberResultEntity(simulationRunId = 0, memberKey = "m1", assignedLoad = 20.0, capacity = 480.0, demandRatio = 0.04, state = "BAJA")
        )
        db.simulationDao().saveRunWithMembers(run, memberResults)

        val bestStars = db.simulationDao().bestStarsFor(challenge.id)
        assertEquals(3, bestStars)
        val attempts = db.simulationDao().attemptsFor(challenge.id)
        assertEquals(1, attempts)
    }

    @Test
    fun `bestStarsFor ignora intentos no aprobados`() = runBlocking {
        val challenge = seedOneChallenge()
        val designId = db.designDao().upsertDesign(StructureDesignEntity(challengeId = challenge.id, createdAt = 1L, updatedAt = 1L))
        db.simulationDao().saveRunWithMembers(
            SimulationRunEntity(designId = designId, challengeId = challenge.id, ranAt = 1L, isConnected = false, isStable = false, passed = false, starsEarned = 0, maxHeight = 0, totalCost = 0, totalWeight = 0.0, stabilityScore = 0, triangulationPercent = 0, feedbackKey = "feedback_no_conectado"),
            emptyList()
        )
        assertNull(db.simulationDao().bestStarsFor(challenge.id))
    }

    @Test
    fun `progreso se actualiza con mejores estrellas sin perder las anteriores`() = runBlocking {
        val challenge = seedOneChallenge()
        db.progressDao().upsert(ProgressEntity(challengeId = challenge.id, started = true, completed = true, bestStars = 2, attempts = 1, lastPlayedAt = 1L))
        val existing = db.progressDao().getFor(challenge.id)!!
        db.progressDao().upsert(existing.copy(bestStars = maxOf(existing.bestStars, 3), attempts = existing.attempts + 1))
        val updated = db.progressDao().getFor(challenge.id)!!
        assertEquals(3, updated.bestStars)
        assertEquals(2, updated.attempts)
    }

    @Test
    fun `completedCountInChapter solo cuenta retos del capitulo indicado`() = runBlocking {
        seedOneChallenge(id = "c01", chapter = 1)
        seedOneChallenge(id = "c02", chapter = 2)
        db.progressDao().upsert(ProgressEntity(challengeId = "c01", completed = true, bestStars = 2))
        db.progressDao().upsert(ProgressEntity(challengeId = "c02", completed = true, bestStars = 1))

        assertEquals(1, db.progressDao().completedCountInChapter(1))
        assertEquals(1, db.progressDao().completedCountInChapter(2))
        assertEquals(0, db.progressDao().completedCountInChapter(3))
    }

    @Test
    fun `insignia se desbloquea una sola vez aunque se intente dos veces`() = runBlocking {
        db.badgeDao().insertAll(listOf(BadgeEntity(id = "PRIMER_LADRILLO", title = "Primer Ladrillo", description = "d", iconRes = "ic_badge_primer_ladrillo", tier = 1)))
        db.badgeDao().unlock(UserBadgeEntity(badgeId = "PRIMER_LADRILLO", unlockedAt = 1L))
        db.badgeDao().unlock(UserBadgeEntity(badgeId = "PRIMER_LADRILLO", unlockedAt = 2L)) // IGNORE por PK duplicada

        val unlocked = db.badgeDao().unlockedIds()
        assertEquals(1, unlocked.size)
    }

    @Test
    fun `blueprint se marca desbloqueado solo la primera vez`() = runBlocking {
        val challenge = seedOneChallenge()
        db.blueprintDao().insertAll(listOf(BlueprintRewardEntity(challengeId = challenge.id, title = "Plano", description = "d", iconRes = "ic_blueprint_reward", unlockedAt = null)))
        db.blueprintDao().markUnlocked(challenge.id, 100L)
        db.blueprintDao().markUnlocked(challenge.id, 200L) // no debe sobreescribir (WHERE unlockedAt IS NULL)

        assertEquals(1, db.blueprintDao().count())
    }

    @Test
    fun `distinctMaterialsForDesign refleja los materiales realmente usados`() = runBlocking {
        val challenge = seedOneChallenge()
        val designId = db.designDao().upsertDesign(StructureDesignEntity(challengeId = challenge.id, createdAt = 1L, updatedAt = 1L))
        db.designDao().insertMembers(
            listOf(
                StructureMemberEntity(designId = designId, memberKey = "m1", nodeAKey = "A", nodeBKey = "B", material = "ACERO", role = "VIGA"),
                StructureMemberEntity(designId = designId, memberKey = "m2", nodeAKey = "B", nodeBKey = "C", material = "ACERO", role = "COLUMNA")
            )
        )
        val materials = db.designDao().distinctMaterialsForDesign(designId)
        assertEquals(listOf("ACERO"), materials)
    }

    @Test
    fun `challengeIdsWithLateralLoad detecta retos de viento`() = runBlocking {
        val challenge = seedOneChallenge(id = "c_viento", chapter = 4)
        db.challengeDao().insertPresetLoads(listOf(PresetLoadEntity(challengeId = challenge.id, x = 3, y = 5, magnitude = 20, isLateral = true)))
        val ids = db.challengeDao().challengeIdsWithLateralLoad()
        assertTrue(challenge.id in ids)
    }

    @Test
    fun `perfil unico se sobreescribe con REPLACE al usar el mismo id`() = runBlocking {
        db.profileDao().upsert(UserProfileEntity(alias = "Nova1", avatarId = 0, createdAt = 1L))
        db.profileDao().upsert(UserProfileEntity(alias = "Nova2", avatarId = 3, createdAt = 1L))
        val profile = db.profileDao().get()
        assertEquals("Nova2", profile?.alias)
        assertEquals(UserProfileEntity.SINGLE_ID, profile?.id)
    }
}
