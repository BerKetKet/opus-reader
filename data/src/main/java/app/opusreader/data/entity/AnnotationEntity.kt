package app.opusreader.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class AnnotationType {
    HIGHLIGHT,
    NOTE,
    BOOKMARK
}

@Entity(
    tableName = "annotations",
    indices = [Index(value = ["bookId"])]
)
data class AnnotationEntity(
    @PrimaryKey val id: String,
    val bookId: String,
    val spineIndex: Int,
    val startOffset: Int,
    val endOffset: Int,
    val selectedText: String,
    val noteText: String? = null,
    val colorHex: String = "#FFEB3B",
    val createdTimestamp: Long = System.currentTimeMillis(),
    val type: AnnotationType = AnnotationType.HIGHLIGHT
)
