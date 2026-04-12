package com.gs.pickleball.ui.match

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
class MatchDetailViewModel @Inject constructor(
    private val repository: MatchRepository
) : ViewModel() {
    private val _match = MutableStateFlow<MatchEntity?>(null)
    val match: StateFlow<MatchEntity?> = _match.asStateFlow()

    private val _players = MutableStateFlow<List<PlayerEntity>>(emptyList())
    val players: StateFlow<List<PlayerEntity>> = _players.asStateFlow()

    fun load(matchId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val match = repository.getMatchById(matchId)
            _match.value = match
            if (match != null) {
                val ids = buildList {
                    add(match.player1Id)
                    add(match.player2Id)
                    match.player3Id?.let { add(it) }
                    match.player4Id?.let { add(it) }
                }
                _players.value = repository.getPlayersByIds(ids)
            } else {
                _players.value = emptyList()
            }
        }
    }
}
