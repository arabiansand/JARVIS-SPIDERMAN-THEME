package com.example.data

import kotlinx.coroutines.flow.Flow

class CommandRepository(private val commandMemoryDao: CommandMemoryDao) {
    val allMemories: Flow<List<CommandMemory>> = commandMemoryDao.getAllMemories()

    suspend fun insertMemory(command: String, response: String) {
        commandMemoryDao.insertMemory(CommandMemory(command = command, response = response))
    }

    suspend fun clearMemory() {
        commandMemoryDao.clearAll()
    }
}
