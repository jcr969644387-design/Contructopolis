package com.educalab.civilestructuras.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.educalab.civilestructuras.AppContainer
import com.educalab.civilestructuras.data.local.entity.BlueprintRewardEntity
import com.educalab.civilestructuras.data.repository.BadgeWithStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class BlueprintsUiState(
    val blueprints: List<BlueprintRewardEntity> = emptyList(),
    val badges: List<BadgeWithStatus> = emptyList()
)

class BlueprintsViewModel(container: AppContainer) : ViewModel() {
    private val _uiState = MutableStateFlow(BlueprintsUiState())
    val uiState: StateFlow<BlueprintsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            container.blueprintRepository.observeAll().collect { list ->
                _uiState.value = _uiState.value.copy(blueprints = list)
            }
        }
        viewModelScope.launch {
            container.badgeRepository.observeAllWithStatus().collect { list ->
                _uiState.value = _uiState.value.copy(badges = list)
            }
        }
    }
}
