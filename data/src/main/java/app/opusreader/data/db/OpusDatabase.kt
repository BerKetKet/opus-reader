package app.opusreader.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import app.opusreader.data.dao.BookDao
import app.opusreader.data.entity.BookEntity

@Database(entities = [BookEntity::class], version = 1, exportSchema = false)
abstract class OpusDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao

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
    }
}
