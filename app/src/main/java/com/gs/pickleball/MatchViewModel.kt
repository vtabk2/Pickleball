package com.gs.pickleball

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gs.pickleball.data.MatchEntity
import com.gs.pickleball.data.MatchRepository
import com.gs.pickleball.data.PlayerEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MatchViewModel @Inject constructor(
    private val repository: MatchRepository
) : ViewModel() {
    private val _players = MutableStateFlow<List<PlayerEntity>>(emptyList())
    val players: StateFlow<List<PlayerEntity>> = _players.asStateFlow()

    private val _matches = MutableStateFlow<List<MatchEntity>>(emptyList())
    val matches: StateFlow<List<MatchEntity>> = _matches.asStateFlow()

    init {
        refreshAll()
    }

    fun refreshAll() {
        viewModelScope.launch(Dispatchers.IO) {
            _players.value = repository.getPlayers()
            _matches.value = repository.getMatches()
        }
    }

    fun saveMatch(match: MatchEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insert(match)
            _matches.value = repository.getMatches()
        }
    }
}
