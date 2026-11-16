package app.opusreader.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import app.opusreader.data.dao.AnnotationDao
import app.opusreader.data.dao.BookDao
import app.opusreader.data.dao.SpineDao
import app.opusreader.data.dao.TocDao
import app.opusreader.data.entity.AnnotationEntity
import app.opusreader.data.entity.BookEntity
import app.opusreader.data.entity.SpineItemEntity
import app.opusreader.data.entity.TocEntryEntity

@Database(
    entities = [
        BookEntity::class,
        SpineItemEntity::class,
        TocEntryEntity::class,
        AnnotationEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class OpusDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
    abstract fun spineDao(): SpineDao
    abstract fun tocDao(): TocDao
    abstract fun annotationDao(): AnnotationDao

    companion object {
        @Volatile
        private var INSTANCE: OpusDatabase? = null

        fun getInstance(context: Context): OpusDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    OpusDatabase::class.java,
                    "opus_reader.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }

        fun createInMemory(context: Context): OpusDatabase {
            return Room.inMemoryDatabaseBuilder(
                context.applicationContext,
                OpusDatabase::class.java
            ).allowMainThreadQueries().build()
        }
    }
}
