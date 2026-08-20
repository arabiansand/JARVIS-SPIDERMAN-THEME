package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "command_memory")
data class CommandMemory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val command: String,
    val response: String,
    val timestamp: Long = System.currentTimeMillis()
)
