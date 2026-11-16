package app.opusreader.data.repository

import android.content.Context
import app.opusreader.data.dao.BookDao
import app.opusreader.data.db.OpusDatabase
import app.opusreader.data.entity.BookEntity
import kotlinx.coroutines.flow.Flow

interface LibraryRepository {
    fun getBooks(): Flow<List<BookEntity>>
    suspend fun getBook(id: String): BookEntity?
    suspend fun saveBook(book: BookEntity)
    suspend fun deleteBook(id: String)

    companion object {
        fun create(context: Context): LibraryRepository {
            val db = OpusDatabase.getInstance(context)
            return DefaultLibraryRepository(db.bookDao())
        }
    }
}

class DefaultLibraryRepository(
    private val bookDao: BookDao
) : LibraryRepository {
    override fun getBooks(): Flow<List<BookEntity>> = bookDao.getAllBooks()
    override suspend fun getBook(id: String): BookEntity? = bookDao.getBookById(id)
    override suspend fun saveBook(book: BookEntity) = bookDao.insertBook(book)
    override suspend fun deleteBook(id: String) = bookDao.deleteBookById(id)
}
