package app.opusreader.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey val id: String,
    val title: String,
    val author: String,
    val filePath: String,
    val coverPath: String? = null,
    val spineIndex: Int = 0,
    val charOffset: Int = 0,
    val progressPercent: Float = 0f,
    val isSample: Boolean = false,
    val isNew: Boolean = false,
    val isDownloaded: Boolean = true,
    val lastReadTimestamp: Long = 0L
)
