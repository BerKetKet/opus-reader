package app.opusreader.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.opusreader.data.entity.TocEntryEntity

@Dao
interface TocDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTocEntries(entries: List<TocEntryEntity>)

    @Query("SELECT * FROM toc_entries WHERE bookId = :bookId ORDER BY orderIndex ASC")
    suspend fun getTocEntriesForBook(bookId: String): List<TocEntryEntity>

    @Query("DELETE FROM toc_entries WHERE bookId = :bookId")
    suspend fun deleteTocEntriesForBook(bookId: String)
}
