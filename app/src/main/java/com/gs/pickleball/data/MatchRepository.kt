package com.gs.pickleball.data

import javax.inject.Inject

class MatchRepository @Inject constructor(
    private val matchDao: MatchDao,
    private val playerDao: PlayerDao
) {
    suspend fun getPlayers(): List<PlayerEntity> = playerDao.getAll()

    suspend fun findOrCreatePlayer(name: String): PlayerEntity {
        val trimmed = name.trim()
        val existing = playerDao.findByName(trimmed)
        if (existing != null) return existing
        val id = playerDao.insert(PlayerEntity(name = trimmed))
        return PlayerEntity(id = id, name = trimmed)
    }

    suspend fun insert(match: MatchEntity) {
        matchDao.insert(match)
    }

    suspend fun getMatches(): List<MatchEntity> = matchDao.getAll()

    suspend fun getMatchById(id: Long): MatchEntity? = matchDao.getById(id)

    suspend fun getPlayersByIds(ids: List<Long>): List<PlayerEntity> = playerDao.getByIds(ids)
}
