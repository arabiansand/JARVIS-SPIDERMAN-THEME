package com.example.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [CommandMemory::class], version = 1, exportSchema = false)
abstract class JarvisDatabase : RoomDatabase() {
    abstract fun commandMemoryDao(): CommandMemoryDao
}
