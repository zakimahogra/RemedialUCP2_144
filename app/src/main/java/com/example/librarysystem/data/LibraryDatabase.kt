package com.example.librarysystem.data

import android.content.Context
import androidx.room.*
import com.example.librarysystem.data.dao.LibraryDao
import com.example.librarysystem.data.entity.*

@Database(
    entities = [
        Author::class,
        Category::class,
        Book::class,
        BookAuthorCrossRef::class,
        BookCopy::class,
        AuditLog::class
    ],
    version = 1,
    exportSchema = false
)
abstract class LibraryDatabase : RoomDatabase() {
    abstract fun libraryDao(): LibraryDao

    companion object {
        @Volatile
        private var INSTANCE: LibraryDatabase? = null

        fun getDatabase(context: Context): LibraryDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LibraryDatabase::class.java,
                    "library_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
