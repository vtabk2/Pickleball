package com.gs.pickleball.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface MatchDao {
    @Insert
    suspend fun insert(match: MatchEntity)

    @Query("SELECT * FROM matches ORDER BY id DESC")
    suspend fun getAll(): List<MatchEntity>

    @Query("SELECT * FROM matches WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): MatchEntity?
}
