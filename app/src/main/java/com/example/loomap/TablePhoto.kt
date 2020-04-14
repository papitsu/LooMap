package com.example.loomap

import androidx.room.*

@Entity(
    tableName = "photos",
    foreignKeys = [
        ForeignKey(
            entity = Toilet::class,
            parentColumns = arrayOf("uid"),
            childColumns = arrayOf("toilet_id"),
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Visit::class,
            parentColumns = arrayOf("uid"),
            childColumns = arrayOf("visit_id"),
            onDelete = ForeignKey.CASCADE
        )]
)

data class Photo(
    @PrimaryKey(autoGenerate = true) var uid: Int?,
    @ColumnInfo(name = "filename") var filename: String,
    @ColumnInfo(name = "toilet_id", index = true) var toiletId: Int,
    @ColumnInfo(name = "visit_id", index = true) var visitId: Int?
)

@Dao
interface PhotoDao {
    @Transaction
    @Insert
    fun insert(photo: Photo)

    @Query("SELECT * FROM photos")
    fun getPhotos(): List<Photo>
}