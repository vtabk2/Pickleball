package com.gs.pickleball.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "matches",
    foreignKeys = [
        ForeignKey(
            entity = PlayerEntity::class,
            parentColumns = ["id"],
            childColumns = ["player1Id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PlayerEntity::class,
            parentColumns = ["id"],
            childColumns = ["player2Id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PlayerEntity::class,
            parentColumns = ["id"],
            childColumns = ["player3Id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PlayerEntity::class,
            parentColumns = ["id"],
            childColumns = ["player4Id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["player1Id"]),
        Index(value = ["player2Id"]),
        Index(value = ["player3Id"]),
        Index(value = ["player4Id"])
    ]
)
data class MatchEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val matchType: Int, // 2 or 4
    val player1Id: Long,
    val player2Id: Long,
    val player3Id: Long? = null,
    val player4Id: Long? = null,
    val scoreTeamA: Int,
    val scoreTeamB: Int,
    val createdAt: Long = System.currentTimeMillis()
)
