package com.gs.pickleball

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gs.pickleball.data.PlayerEntity
import com.gs.pickleball.data.PlayerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: PlayerRepository
) : ViewModel() {
    private val _players = MutableStateFlow<List<PlayerEntity>>(emptyList())
    val players: StateFlow<List<PlayerEntity>> = _players.asStateFlow()

    init {
        refresh()
    }

    fun savePlayer(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insert(name)
            _players.value = repository.getAll()
        }
    }

    fun refresh() {
        viewModelScope.launch(Dispatchers.IO) {
            _players.value = repository.getAll()
        }
    }
}
