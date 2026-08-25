package com.educalab.civilestructuras.ui.navigation

/** Rutas de navegación de Constructópolis (Navigation Compose). */
object Routes {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val PROFILE_SETUP = "profile_setup"
    const val HOME = "home"
    const val CONCEPTS = "concepts"
    const val MATERIALS = "materials"
    const val PROFILE = "profile"
    const val BLUEPRINTS = "blueprints"

    private const val CHAPTER_BASE = "chapter"
    const val CHAPTER_PATTERN = "$CHAPTER_BASE/{chapter}"
    fun chapter(chapter: Int) = "$CHAPTER_BASE/$chapter"

    private const val BUILDER_BASE = "builder"
    const val BUILDER_PATTERN = "$BUILDER_BASE/{challengeId}"
    fun builder(challengeId: String) = "$BUILDER_BASE/$challengeId"

    // Capítulos temáticos (coinciden con StructureChallengeEntity.worldChapter, ver Seeder/generate_seed.py)
    const val CHAPTER_VIGAS = 1
    const val CHAPTER_COLUMNAS = 2
    const val CHAPTER_TORRES = 3
    const val CHAPTER_CARGAS = 4
    const val CHAPTER_RETOS = 5

    fun titleForChapter(chapter: Int): String = when (chapter) {
        CHAPTER_VIGAS -> "Vigas"
        CHAPTER_COLUMNAS -> "Columnas"
        CHAPTER_TORRES -> "Torres"
        CHAPTER_CARGAS -> "Cargas y Viento"
        CHAPTER_RETOS -> "Gran Taller de Retos"
        else -> "Capítulo $chapter"
    }
}
