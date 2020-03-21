package com.example.loomap

import androidx.room.*

@Entity(tableName = "stats")
data class Stat(
    @PrimaryKey(autoGenerate = true) var uid: Int?,
    @ColumnInfo(name = "value") var value: String?,
    @ColumnInfo(name = "label") var message: String
)

@Dao
interface StatDao {
    @Transaction @Insert
    fun insert(stat: Stat)

    @Query("SELECT * FROM stats")
    fun getStats(): List<Stat>
}