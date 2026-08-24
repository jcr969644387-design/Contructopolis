package com.educalab.civilestructuras.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.educalab.civilestructuras.AppContainer
import com.educalab.civilestructuras.data.local.entity.MaterialEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MaterialsViewModel(container: AppContainer) : ViewModel() {
    private val _materials = MutableStateFlow<List<MaterialEntity>>(emptyList())
    val materials: StateFlow<List<MaterialEntity>> = _materials.asStateFlow()

    init {
        viewModelScope.launch {
            container.materialRepository.observeAll().collect { _materials.value = it }
        }
    }
}
