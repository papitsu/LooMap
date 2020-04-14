package com.example.loomap

import androidx.room.*

@Entity(
    tableName = "visits",
    foreignKeys = [ForeignKey(
        entity = Toilet::class,
        parentColumns = arrayOf("uid"),
        childColumns = arrayOf("toilet_id"),
        onDelete = ForeignKey.CASCADE
    )]
)
data class Visit(
    @PrimaryKey(autoGenerate = true) var uid: Int?,
    @ColumnInfo(name = "toilet_id", index = true) var toiletId: Int,
    @ColumnInfo(name = "time") var time: Long,
    @ColumnInfo(name = "comment") var comment: String?,
    @ColumnInfo(name = "rating") var rating: Float,
    @ColumnInfo(name = "photo_id") var photoId: Int?
)

@Dao
interface VisitDao {
    @Transaction
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(visit: Visit)

    @Update
    fun update(visit: Visit)

    @Query("SELECT * FROM visits WHERE toilet_id = :id")
    fun getByToilet(id: Int): List<Visit>

    @Query("SELECT * FROM visits WHERE uid = :id")
    fun getById(id: Int): Visit

    @Query("SELECT * FROM visits")
    fun getVisits(): List<Visit>

    @Query("DELETE FROM visits WHERE uid = :id")
    fun delete(id: Int)

    @Query("DELETE FROM visits")
    fun deleteAll()
}