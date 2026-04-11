package com.gs.pickleball.data

import javax.inject.Inject

class MatchRepository @Inject constructor(
    private val matchDao: MatchDao,
    private val playerDao: PlayerDao
) {
    suspend fun getPlayers(): List<PlayerEntity> = playerDao.getAll()

    suspend fun findOrCreatePlayer(name: String): PlayerEntity {
        val existing = playerDao.findByName(name)
        if (existing != null) return existing
        val id = playerDao.insert(PlayerEntity(name = name.trim()))
        return PlayerEntity(id = id, name = name.trim())
    }

    suspend fun insert(match: MatchEntity) {
        matchDao.insert(match)
    }

    suspend fun getMatches(): List<MatchEntity> = matchDao.getAll()
}
