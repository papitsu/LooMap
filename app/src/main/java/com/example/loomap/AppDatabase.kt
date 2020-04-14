package com.example.loomap

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        Stat::class,
        Toilet::class,
        Visit::class,
        Photo::class
    ],
    version = 1
)

abstract class AppDatabase : RoomDatabase() {
    abstract fun statDao(): StatDao
    abstract fun visitDao(): VisitDao
    abstract fun toiletDao(): ToiletDao
    abstract fun photoDao(): PhotoDao
}