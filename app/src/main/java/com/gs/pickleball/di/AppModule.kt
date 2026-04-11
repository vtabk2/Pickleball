package com.gs.pickleball.di

import android.content.Context
import androidx.room.Room
import com.gs.pickleball.data.AppDatabase
import com.gs.pickleball.data.MatchDao
import com.gs.pickleball.data.PlayerDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "pickleball.db")
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun providePlayerDao(db: AppDatabase): PlayerDao = db.playerDao()

    @Provides
    fun provideMatchDao(db: AppDatabase): MatchDao = db.matchDao()
}
