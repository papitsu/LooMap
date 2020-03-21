package com.example.loomap

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Stat::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun statDao() : StatDao
}