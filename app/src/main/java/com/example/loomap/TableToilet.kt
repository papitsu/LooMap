package com.example.loomap

import androidx.room.*


@Entity(tableName = "toilets")
data class Toilet(
    @PrimaryKey(autoGenerate = true) var uid: Int?,
    @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "category") var category: String,
    @ColumnInfo(name = "description") var description: String?,
    @ColumnInfo(name = "latitude") var latitude: Double,
    @ColumnInfo(name = "longitude") var longitude: Double
)

@Dao
interface ToiletDao {
    @Transaction
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(toilet: Toilet): Long

    @Update
    fun update(toilet: Toilet)

    @Query("SELECT * FROM toilets")
    fun getAllToilets(): List<Toilet>

    @Query("SELECT * FROM toilets WHERE uid = :id")
    fun getById(id: Int): Toilet

    @Query("SELECT * FROM toilets WHERE name = :name")
    fun getByName(name: String): List<Toilet>

    @Query("DELETE FROM toilets WHERE uid = :id")
    fun delete(id: Int)

    @Query("DELETE FROM toilets")
    fun deleteAll()
}