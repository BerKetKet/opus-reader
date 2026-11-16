package app.opusreader.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "spine_items",
    indices = [Index(value = ["bookId"])]
)
data class SpineItemEntity(
    @PrimaryKey val id: String,
    val bookId: String,
    val href: String,
    val mediaType: String,
    val orderIndex: Int,
    val linear: Boolean = true
)
