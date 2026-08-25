package com.educalab.civilestructuras.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.educalab.civilestructuras.AppContainer
import com.educalab.civilestructuras.data.repository.ChallengeSummary
import com.educalab.civilestructuras.domain.logic.ModuleState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class HomeUiState(
    val alias: String = "Ingeniera Junior",
    val avatarId: Int = 0,
    val overallProgressPercent: Int = 0,
    val chaptersInOrder: List<Int> = emptyList(),
    val summariesByChapter: Map<Int, List<ChallengeSummary>> = emptyMap(),
    val nextChallenge: ChallengeSummary? = null,
    val conceptsViewed: Boolean = false,
    val materialsViewed: Boolean = false,
    /** Capítulos cuyo módulo puede abrirse desde Inicio (ver [HomeViewModel.unlockedChapters]). */
    val unlockedChapters: Set<Int> = emptySet()
)

class HomeViewModel(private val container: AppContainer) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                container.challengeRepository.observeSummariesByChapter(),
                container.profileRepository.observeProfile()
            ) { grouped, profile -> grouped to profile }.collect { (grouped, profile) ->
                val overall = container.challengeRepository.overallProgressPercent()
                val conceptsViewed = profile?.conceptsViewed == true
                val materialsViewed = profile?.materialsViewed == true
                val unlocked = unlockedChapters(grouped, conceptsViewed && materialsViewed)
                val next = grouped.values.flatten()
                    .filter { it.challenge.worldChapter in unlocked }
                    .sortedWith(compareBy({ it.challenge.worldChapter }, { it.challenge.orderInChapter }))
                    .firstOrNull { it.state == ModuleState.DISPONIBLE || it.state == ModuleState.INICIADO }
                _uiState.value = _uiState.value.copy(
                    overallProgressPercent = overall,
                    chaptersInOrder = grouped.keys.sorted(),
                    summariesByChapter = grouped,
                    nextChallenge = next,
                    alias = profile?.alias ?: _uiState.value.alias,
                    avatarId = profile?.avatarId ?: _uiState.value.avatarId,
                    conceptsViewed = conceptsViewed,
                    materialsViewed = materialsViewed,
                    unlockedChapters = unlocked
                )
            }
        }
    }

    companion object {
        /**
         * Orden de desbloqueo de los 4 capítulos principales (coincide con
         * Routes.CHAPTER_VIGAS..CARGAS, duplicado aquí como enteros para no
         * acoplar el ViewModel a la capa de navegación/UI). El capítulo de
         * Retos (5) se desbloquea aparte, solo cuando los 4 anteriores están
         * al 100%.
         */
        private val gatedChapterOrder = listOf(1, 2, 3, 4)
        private const val RETOS_CHAPTER = 5

        fun chapterCompletionPercent(grouped: Map<Int, List<ChallengeSummary>>, chapter: Int): Int {
            val list = grouped[chapter].orEmpty()
            if (list.isEmpty()) return 0
            return (list.count { it.state == ModuleState.COMPLETADO || it.state == ModuleState.DOMINADO } * 100) / list.size
        }

        fun unlockedChapters(grouped: Map<Int, List<ChallengeSummary>>, basicsRead: Boolean): Set<Int> {
            val unlocked = mutableSetOf<Int>()
            unlocked += grouped.keys.filter { it !in gatedChapterOrder && it != RETOS_CHAPTER }
            if (!basicsRead) return unlocked
            var previousOk = true
            for (chapter in gatedChapterOrder) {
                if (!previousOk) break
                unlocked += chapter
                previousOk = chapterCompletionPercent(grouped, chapter) >= 50
            }
            if (gatedChapterOrder.all { chapterCompletionPercent(grouped, it) >= 100 }) {
                unlocked += RETOS_CHAPTER
            }
            return unlocked
        }
    }
}
