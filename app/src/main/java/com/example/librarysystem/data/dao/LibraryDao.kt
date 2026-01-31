package com.example.librarysystem.data.dao

import androidx.room.*
import com.example.librarysystem.data.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LibraryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuthor(author: Author): Long

    @Update
    suspend fun updateAuthor(author: Author)

    @Query("SELECT * FROM authors")
    fun getAllAuthors(): Flow<List<Author>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category): Long

    @Update
    suspend fun updateCategory(category: Category)

    @Query("SELECT * FROM categories WHERE isDeleted = 0")
    fun getAllCategories(): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE parentId = :parentId AND isDeleted = 0")
    suspend fun getSubCategories(parentId: Long): List<Category>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: Book): Long

    @Update
    suspend fun updateBook(book: Book)

    @Query("SELECT * FROM books WHERE isDeleted = 0")
    fun getAllBooks(): Flow<List<Book>>

    @Query("SELECT * FROM books WHERE categoryId IN (:categoryIds) AND isDeleted = 0")
    fun getBooksInCategories(categoryIds: List<Long>): Flow<List<Book>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBookAuthorCrossRef(crossRef: BookAuthorCrossRef)

    @Transaction
    @Query("SELECT * FROM authors WHERE id IN (SELECT authorId FROM book_authors WHERE bookId = :bookId)")
    fun getAuthorsForBook(bookId: Long): Flow<List<Author>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookCopy(copy: BookCopy): Long

    @Update
    suspend fun updateBookCopy(copy: BookCopy)

    @Query("SELECT * FROM book_copies WHERE bookId = :bookId")
    fun getCopiesForBook(bookId: Long): Flow<List<BookCopy>>

    @Query("SELECT * FROM book_copies WHERE bookId IN (SELECT id FROM books WHERE categoryId IN (:categoryIds)) AND status = 'DIPINJAM'")
    suspend fun getBorrowedCopiesInCategories(categoryIds: List<Long>): List<BookCopy>

    @Insert
    suspend fun insertAuditLog(log: AuditLog)

    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC")
    fun getAllAuditLogs(): Flow<List<AuditLog>>
}
