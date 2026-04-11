package com.gs.pickleball.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [PlayerEntity::class, MatchEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun playerDao(): PlayerDao
    abstract fun matchDao(): MatchDao
}
