package com.gs.pickleball.data

import javax.inject.Inject

class PlayerRepository @Inject constructor(
    private val playerDao: PlayerDao
) {
    suspend fun insert(name: String) {
        playerDao.insert(PlayerEntity(name = name))
    }

    suspend fun getAll(): List<PlayerEntity> = playerDao.getAll()
}
