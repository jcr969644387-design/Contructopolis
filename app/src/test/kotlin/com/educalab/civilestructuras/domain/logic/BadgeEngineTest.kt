package com.educalab.civilestructuras.domain.logic

import com.educalab.civilestructuras.domain.model.MaterialType
import org.junit.Assert.*
import org.junit.Test

/** Réplica JUnit4 de tools/verify_domain (ver docs/BUILD_REPORT.md). */
class BadgeEngineTest {

    @Test fun `sin retos completados no hay insignias`() {
        assertTrue(BadgeEngine.evaluateEarnedBadges(PlayerStats()).isEmpty())
    }

    @Test fun `primer reto completado desbloquea Primer Ladrillo`() {
        val earned = BadgeEngine.evaluateEarnedBadges(PlayerStats(totalChallengesCompleted = 1))
        assertTrue(BadgeId.PRIMER_LADRILLO in earned)
    }

    @Test fun `cinco disenos solo con acero desbloquean Maestra del Acero`() {
        val earned = BadgeEngine.evaluateEarnedBadges(PlayerStats(designsPassedWithOnlyMaterial = mapOf(MaterialType.ACERO to 5)))
        assertTrue(BadgeId.MAESTRA_DEL_ACERO in earned)
    }

    @Test fun `cuatro disenos con acero no alcanzan para la insignia`() {
        val earned = BadgeEngine.evaluateEarnedBadges(PlayerStats(designsPassedWithOnlyMaterial = mapOf(MaterialType.ACERO to 4)))
        assertFalse(BadgeId.MAESTRA_DEL_ACERO in earned)
    }

    @Test fun `torre de altura 20 desbloquea Torre al Cielo`() {
        val earned = BadgeEngine.evaluateEarnedBadges(PlayerStats(maxHeightEverBuilt = 20))
        assertTrue(BadgeId.TORRE_AL_CIELO in earned)
    }

    @Test fun `triangulacion sobre el 50 por ciento desbloquea Triangulacion Perfecta`() {
        val earned = BadgeEngine.evaluateEarnedBadges(PlayerStats(bestTriangulationPercent = 60))
        assertTrue(BadgeId.TRIANGULACION_PERFECTA in earned)
    }

    @Test fun `aprobar gastando la mitad del presupuesto desbloquea Ingeniera Ahorradora`() {
        val earned = BadgeEngine.evaluateEarnedBadges(PlayerStats(cheapestPassRatio = 0.4))
        assertTrue(BadgeId.INGENIERA_AHORRADORA in earned)
    }

    @Test fun `tres retos de viento superados desbloquean Resistente al Viento`() {
        val earned = BadgeEngine.evaluateEarnedBadges(PlayerStats(lateralChallengesPassed = 3))
        assertTrue(BadgeId.RESISTENTE_AL_VIENTO in earned)
    }

    @Test fun `completar todos los retos del capitulo 1 desbloquea la insignia de capitulo`() {
        val earned = BadgeEngine.evaluateEarnedBadges(
            PlayerStats(challengesCompletedChapter1 = 5, totalChallengesInChapter1 = 5)
        )
        assertTrue(BadgeId.CAPITULO_UNO_COMPLETO in earned)
    }

    @Test fun `capitulo con cero retos definidos no desbloquea la insignia de capitulo`() {
        val earned = BadgeEngine.evaluateEarnedBadges(
            PlayerStats(challengesCompletedChapter1 = 0, totalChallengesInChapter1 = 0)
        )
        assertFalse(BadgeId.CAPITULO_UNO_COMPLETO in earned)
    }

    @Test fun `veinte retos completados desbloquean Maestra Constructora`() {
        val earned = BadgeEngine.evaluateEarnedBadges(PlayerStats(totalChallengesCompleted = 20))
        assertTrue(BadgeId.MAESTRA_CONSTRUCTORA in earned)
    }

    @Test fun `diez retos con tres estrellas desbloquean Perfeccionista`() {
        val earned = BadgeEngine.evaluateEarnedBadges(PlayerStats(threeStarChallenges = 10))
        assertTrue(BadgeId.PERFECCIONISTA in earned)
    }

    @Test fun `newlyEarned detecta solo las insignias nuevas entre dos estados`() {
        val before = PlayerStats(totalChallengesCompleted = 0)
        val after = PlayerStats(totalChallengesCompleted = 1)
        assertEquals(setOf(BadgeId.PRIMER_LADRILLO), BadgeEngine.newlyEarned(before, after))
    }

    @Test fun `newlyEarned es vacio si no cambian las estadisticas relevantes`() {
        val stats = PlayerStats(totalChallengesCompleted = 5)
        assertTrue(BadgeEngine.newlyEarned(stats, stats).isEmpty())
    }
}

class ProgressEngineTest {

    @Test fun `primer reto de un capitulo siempre esta disponible`() {
        val state = ProgressEngine.stateFor(1, previousOrderCompleted = false, started = false, completed = false, masteredWithThreeStars = false)
        assertEquals(ModuleState.DISPONIBLE, state)
    }

    @Test fun `segundo reto bloqueado si el anterior no se completo`() {
        val state = ProgressEngine.stateFor(2, previousOrderCompleted = false, started = false, completed = false, masteredWithThreeStars = false)
        assertEquals(ModuleState.BLOQUEADO, state)
    }

    @Test fun `segundo reto disponible si el anterior se completo`() {
        val state = ProgressEngine.stateFor(2, previousOrderCompleted = true, started = false, completed = false, masteredWithThreeStars = false)
        assertEquals(ModuleState.DISPONIBLE, state)
    }

    @Test fun `reto iniciado pero no completado muestra estado iniciado`() {
        val state = ProgressEngine.stateFor(2, previousOrderCompleted = true, started = true, completed = false, masteredWithThreeStars = false)
        assertEquals(ModuleState.INICIADO, state)
    }

    @Test fun `reto completado con tres estrellas se muestra como dominado`() {
        val state = ProgressEngine.stateFor(3, previousOrderCompleted = true, started = true, completed = true, masteredWithThreeStars = true)
        assertEquals(ModuleState.DOMINADO, state)
    }

    @Test fun `progreso global cero cuando no hay retos totales`() {
        assertEquals(0, ProgressEngine.overallProgressPercent(0, 0))
    }

    @Test fun `progreso global se calcula correctamente`() {
        assertEquals(50, ProgressEngine.overallProgressPercent(5, 10))
    }

    @Test fun `progreso global nunca supera cien`() {
        assertEquals(100, ProgressEngine.overallProgressPercent(12, 10))
    }
}
