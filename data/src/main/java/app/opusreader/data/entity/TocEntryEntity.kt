package app.opusreader.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "toc_entries",
    indices = [Index(value = ["bookId"])]
)
data class TocEntryEntity(
    @PrimaryKey val id: String,
    val bookId: String,
    val title: String,
    val href: String,
    val orderIndex: Int,
    val level: Int = 0
)
