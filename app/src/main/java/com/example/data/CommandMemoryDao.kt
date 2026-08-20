package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CommandMemoryDao {
    @Query("SELECT * FROM command_memory ORDER BY timestamp DESC")
    fun getAllMemories(): Flow<List<CommandMemory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: CommandMemory)

    @Query("DELETE FROM command_memory WHERE id = :id")
    suspend fun deleteMemoryById(id: Int)

    @Query("DELETE FROM command_memory")
    suspend fun clearAll()
}
