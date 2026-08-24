package com.educalab.civilestructuras.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.educalab.civilestructuras.AppContainer
import com.educalab.civilestructuras.data.repository.ChallengeSummary
import com.educalab.civilestructuras.domain.logic.ModuleState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val alias: String = "Ingeniera Junior",
    val avatarId: Int = 0,
    val overallProgressPercent: Int = 0,
    val chaptersInOrder: List<Int> = emptyList(),
    val summariesByChapter: Map<Int, List<ChallengeSummary>> = emptyMap(),
    val nextChallenge: ChallengeSummary? = null
)

class HomeViewModel(private val container: AppContainer) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            container.challengeRepository.observeSummariesByChapter().collect { grouped ->
                val overall = container.challengeRepository.overallProgressPercent()
                val next = grouped.values.flatten()
                    .sortedWith(compareBy({ it.challenge.worldChapter }, { it.challenge.orderInChapter }))
                    .firstOrNull { it.state == ModuleState.DISPONIBLE || it.state == ModuleState.INICIADO }
                _uiState.value = _uiState.value.copy(
                    overallProgressPercent = overall,
                    chaptersInOrder = grouped.keys.sorted(),
                    summariesByChapter = grouped,
                    nextChallenge = next
                )
            }
        }
        viewModelScope.launch {
            container.profileRepository.observeProfile().collect { profile ->
                if (profile != null) {
                    _uiState.value = _uiState.value.copy(alias = profile.alias, avatarId = profile.avatarId)
                }
            }
        }
    }
}
