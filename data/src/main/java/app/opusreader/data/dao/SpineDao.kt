package app.opusreader.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.opusreader.data.entity.SpineItemEntity

@Dao
interface SpineDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpineItems(items: List<SpineItemEntity>)

    @Query("SELECT * FROM spine_items WHERE bookId = :bookId ORDER BY orderIndex ASC")
    suspend fun getSpineItemsForBook(bookId: String): List<SpineItemEntity>

    @Query("DELETE FROM spine_items WHERE bookId = :bookId")
    suspend fun deleteSpineItemsForBook(bookId: String)
}
