package com.educalab.civilestructuras.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.educalab.civilestructuras.AppContainer
import com.educalab.civilestructuras.data.local.entity.UserProfileEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(private val container: AppContainer) : ViewModel() {
    private val _profile = MutableStateFlow<UserProfileEntity?>(null)
    val profile: StateFlow<UserProfileEntity?> = _profile.asStateFlow()

    init {
        viewModelScope.launch {
            container.profileRepository.observeProfile().collect { _profile.value = it }
        }
    }

    fun updateAlias(alias: String) = viewModelScope.launch { container.profileRepository.updateAlias(alias) }
    fun updateAvatar(avatarId: Int) = viewModelScope.launch { container.profileRepository.updateAvatar(avatarId) }
    fun setSoundEnabled(enabled: Boolean) = viewModelScope.launch { container.profileRepository.setSoundEnabled(enabled) }
    fun setHapticEnabled(enabled: Boolean) = viewModelScope.launch { container.profileRepository.setHapticEnabled(enabled) }
    fun markConceptsViewed() = viewModelScope.launch { container.profileRepository.markConceptsViewed() }
    fun markMaterialsViewed() = viewModelScope.launch { container.profileRepository.markMaterialsViewed() }
}
