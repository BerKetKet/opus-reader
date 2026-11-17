package app.opusreader.data.repository

import android.content.Context
import app.opusreader.data.dao.AnnotationDao
import app.opusreader.data.dao.BookDao
import app.opusreader.data.dao.SpineDao
import app.opusreader.data.dao.TocDao
import app.opusreader.data.db.OpusDatabase
import app.opusreader.data.entity.AnnotationEntity
import app.opusreader.data.entity.BookEntity
import app.opusreader.data.entity.SpineItemEntity
import app.opusreader.data.entity.TocEntryEntity
import app.opusreader.data.importer.EpubImporter
import kotlinx.coroutines.flow.Flow

interface LibraryRepository {
    fun getBooks(): Flow<List<BookEntity>>
    fun getDownloadedBooks(): Flow<List<BookEntity>>
    fun getBooksByCollection(collectionName: String): Flow<List<BookEntity>>
    fun getDistinctCollections(): Flow<List<String>>

    suspend fun getBook(id: String): BookEntity?
    suspend fun saveBook(book: BookEntity)
    suspend fun deleteBook(id: String)
    suspend fun updateProgress(id: String, spineIndex: Int, charOffset: Int, progressPercent: Float)
    suspend fun updateCollection(id: String, collectionName: String?)

    suspend fun getSpine(bookId: String): List<SpineItemEntity>
    suspend fun getToc(bookId: String): List<TocEntryEntity>

    fun getAnnotations(bookId: String): Flow<List<AnnotationEntity>>
    suspend fun addAnnotation(annotation: AnnotationEntity)
    suspend fun deleteAnnotation(id: String)
    suspend fun toggleAnnotationFavorite(id: String, isFavorite: Boolean)

    fun createImporter(context: Context): EpubImporter

    companion object {
        fun create(context: Context): LibraryRepository {
            val db = OpusDatabase.getInstance(context)
            return DefaultLibraryRepository(
                db = db,
                bookDao = db.bookDao(),
                spineDao = db.spineDao(),
                tocDao = db.tocDao(),
                annotationDao = db.annotationDao()
            )
        }

        fun createForDatabase(db: OpusDatabase): LibraryRepository {
            return DefaultLibraryRepository(
                db = db,
                bookDao = db.bookDao(),
                spineDao = db.spineDao(),
                tocDao = db.tocDao(),
                annotationDao = db.annotationDao()
            )
        }
    }
}

class DefaultLibraryRepository(
    private val db: OpusDatabase,
    private val bookDao: BookDao,
    private val spineDao: SpineDao,
    private val tocDao: TocDao,
    private val annotationDao: AnnotationDao
) : LibraryRepository {

    override fun getBooks(): Flow<List<BookEntity>> = bookDao.getAllBooks()

    override fun getDownloadedBooks(): Flow<List<BookEntity>> = bookDao.getDownloadedBooks()

    override fun getBooksByCollection(collectionName: String): Flow<List<BookEntity>> =
        bookDao.getBooksByCollection(collectionName)

    override fun getDistinctCollections(): Flow<List<String>> = bookDao.getDistinctCollections()

    override suspend fun getBook(id: String): BookEntity? = bookDao.getBookById(id)

    override suspend fun saveBook(book: BookEntity) = bookDao.insertBook(book)

    override suspend fun deleteBook(id: String) {
        bookDao.deleteBookById(id)
        spineDao.deleteSpineItemsForBook(id)
        tocDao.deleteTocEntriesForBook(id)
        annotationDao.deleteAnnotationsForBook(id)
    }

    override suspend fun updateProgress(
        id: String,
        spineIndex: Int,
        charOffset: Int,
        progressPercent: Float
    ) {
        bookDao.updateProgress(
            id = id,
            spineIndex = spineIndex,
            charOffset = charOffset,
            progressPercent = progressPercent,
            lastReadTimestamp = System.currentTimeMillis()
        )
    }

    override suspend fun updateCollection(id: String, collectionName: String?) {
        bookDao.updateCollection(id, collectionName)
    }

    override suspend fun getSpine(bookId: String): List<SpineItemEntity> =
        spineDao.getSpineItemsForBook(bookId)

    override suspend fun getToc(bookId: String): List<TocEntryEntity> =
        tocDao.getTocEntriesForBook(bookId)

    override fun getAnnotations(bookId: String): Flow<List<AnnotationEntity>> =
        annotationDao.getAnnotationsForBook(bookId)

    override suspend fun addAnnotation(annotation: AnnotationEntity) =
        annotationDao.insertAnnotation(annotation)

    override suspend fun deleteAnnotation(id: String) =
        annotationDao.deleteAnnotationById(id)

    override suspend fun toggleAnnotationFavorite(id: String, isFavorite: Boolean) =
        annotationDao.updateFavorite(id, isFavorite)

    override fun createImporter(context: Context): EpubImporter =
        EpubImporter(context, db)
}
