package com.gs.pickleball.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface PlayerDao {
    @Insert
    suspend fun insert(player: PlayerEntity)

    @Query("SELECT * FROM players ORDER BY id DESC")
    suspend fun getAll(): List<PlayerEntity>
}
