package com.educalab.civilestructuras.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.educalab.civilestructuras.AppContainer
import com.educalab.civilestructuras.data.repository.ChallengeSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChapterListViewModel(private val container: AppContainer) : ViewModel() {

    private val _challenges = MutableStateFlow<List<ChallengeSummary>>(emptyList())
    fun challengesFor(chapter: Int): StateFlow<List<ChallengeSummary>> {
        viewModelScope.launch {
            container.challengeRepository.observeSummariesByChapter().collect { grouped ->
                _challenges.value = grouped[chapter].orEmpty().sortedBy { it.challenge.orderInChapter }
            }
        }
        return _challenges.asStateFlow()
    }
}
