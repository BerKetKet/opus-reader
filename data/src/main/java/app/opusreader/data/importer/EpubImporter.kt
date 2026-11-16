package app.opusreader.data.importer

import android.content.Context
import app.opusreader.data.db.OpusDatabase
import app.opusreader.data.entity.BookEntity
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

class EpubImporter(
    private val context: Context,
    private val database: OpusDatabase
) {
    suspend fun importFromStream(
        inputStream: InputStream,
        originalName: String? = null
    ): Result<BookEntity> {
        val tempId = UUID.randomUUID().toString()
        val epubsDir = File(context.filesDir, "epubs").apply { mkdirs() }
        val coversDir = File(context.filesDir, "covers").apply { mkdirs() }

        val tempFile = File(epubsDir, "temp_$tempId.epub")
        try {
            // Write stream to temp file
            FileOutputStream(tempFile).use { out ->
                inputStream.copyTo(out)
            }

            // Parse EPUB
            val parsed = tempFile.inputStream().use { stream ->
                EpubParser.parse(stream, tempFile.absolutePath)
            }

            val finalBookId = parsed.book.id
            val finalFile = File(epubsDir, "$finalBookId.epub")
            if (tempFile != finalFile) {
                tempFile.renameTo(finalFile)
            }

            // Save cover if present
            var coverPath: String? = null
            if (parsed.coverBytes != null) {
                val coverExtension = if (parsed.coverEntryPath?.endsWith(".jpg", true) == true ||
                    parsed.coverEntryPath?.endsWith(".jpeg", true) == true
                ) "jpg" else "png"
                val coverFile = File(coversDir, "$finalBookId.$coverExtension")
                FileOutputStream(coverFile).use { it.write(parsed.coverBytes) }
                coverPath = coverFile.absolutePath
            }

            val bookToSave = parsed.book.copy(
                filePath = finalFile.absolutePath,
                coverPath = coverPath
            )

            // Save to database in transaction
            val bookDao = database.bookDao()
            val spineDao = database.spineDao()
            val tocDao = database.tocDao()

            bookDao.insertBook(bookToSave)
            spineDao.insertSpineItems(parsed.spineItems)
            tocDao.insertTocEntries(parsed.tocEntries)

            return Result.success(bookToSave)
        } catch (e: Throwable) {
            if (tempFile.exists()) {
                tempFile.delete()
            }
            return Result.failure(e)
        }
    }
}
