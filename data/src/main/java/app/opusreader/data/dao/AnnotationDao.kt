package app.opusreader.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.opusreader.data.entity.AnnotationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AnnotationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnotation(annotation: AnnotationEntity)

    @Query("SELECT * FROM annotations WHERE bookId = :bookId ORDER BY spineIndex ASC, startOffset ASC")
    fun getAnnotationsForBook(bookId: String): Flow<List<AnnotationEntity>>

    @Query("DELETE FROM annotations WHERE id = :id")
    suspend fun deleteAnnotationById(id: String)

    @Query("DELETE FROM annotations WHERE bookId = :bookId")
    suspend fun deleteAnnotationsForBook(bookId: String)
}
