package app.opusreader.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.opusreader.data.entity.BookEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {
    @Query("SELECT * FROM books ORDER BY lastReadTimestamp DESC, addedTimestamp DESC")
    fun getAllBooks(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE id = :id LIMIT 1")
    suspend fun getBookById(id: String): BookEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: BookEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooks(books: List<BookEntity>)

    @Query("DELETE FROM books WHERE id = :id")
    suspend fun deleteBookById(id: String)

    @Query("UPDATE books SET spineIndex = :spineIndex, charOffset = :charOffset, progressPercent = :progressPercent, lastReadTimestamp = :lastReadTimestamp WHERE id = :id")
    suspend fun updateProgress(
        id: String,
        spineIndex: Int,
        charOffset: Int,
        progressPercent: Float,
        lastReadTimestamp: Long
    )

    @Query("UPDATE books SET collectionName = :collectionName WHERE id = :id")
    suspend fun updateCollection(id: String, collectionName: String?)

    @Query("SELECT DISTINCT collectionName FROM books WHERE collectionName IS NOT NULL AND collectionName != '' ORDER BY collectionName ASC")
    fun getDistinctCollections(): Flow<List<String>>

    @Query("SELECT * FROM books WHERE collectionName = :collectionName ORDER BY lastReadTimestamp DESC")
    fun getBooksByCollection(collectionName: String): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE isDownloaded = 1 ORDER BY lastReadTimestamp DESC")
    fun getDownloadedBooks(): Flow<List<BookEntity>>
}
