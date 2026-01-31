package com.example.librarysystem.data.repository

import com.example.librarysystem.data.dao.LibraryDao
import com.example.librarysystem.data.entity.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

class LibraryRepository(private val libraryDao: LibraryDao) {

    val allAuthors: Flow<List<Author>> = libraryDao.getAllAuthors()

    suspend fun insertAuthor(author: Author) {
        val id = libraryDao.insertAuthor(author)
        logAudit("authors", id, "INSERT", null, author.toString())
    }

    suspend fun updateAuthor(author: Author) {
        val old = libraryDao.getAllAuthors().first().find { it.id == author.id }
        libraryDao.updateAuthor(author)
        logAudit("authors", author.id, "UPDATE", old.toString(), author.toString())
    }

    val allCategories: Flow<List<Category>> = libraryDao.getAllCategories()

    suspend fun insertCategory(category: Category) {
        if (category.parentId != null) {
            val parentExists = allCategories.first().any { it.id == category.parentId }
            if (!parentExists) throw Exception("Parent category does not exist.")
        }
        
        val id = libraryDao.insertCategory(category)
        logAudit("categories", id, "INSERT", null, category.toString())
    }

    suspend fun updateCategory(category: Category) {
        if (category.parentId == category.id) throw Exception("Category cannot be its own parent.")

        if (category.parentId != null) {
            val descendants = getAllSubCategoryIds(category.id)
            if (descendants.contains(category.parentId)) {
                throw Exception("Cyclic reference detected: New parent is a child of this category.")
            }
            
            val parentExists = allCategories.first().any { it.id == category.parentId }
            if (!parentExists) throw Exception("Parent category does not exist.")
        }
        
        val old = libraryDao.getAllCategories().first().find { it.id == category.id }
        libraryDao.updateCategory(category)
        logAudit("categories", category.id, "UPDATE", old.toString(), category.toString())
    }

    suspend fun deleteCategory(categoryId: Long, deleteBooks: Boolean) {
        val categoryIds = getAllSubCategoryIds(categoryId)
        val borrowed = libraryDao.getBorrowedCopiesInCategories(categoryIds)

        if (borrowed.isNotEmpty()) {
            throw Exception("Tidak dapat menghapus kategori: Buku sedang dipinjam.")
        }

        if (deleteBooks) {
            categoryIds.forEach { catId ->
                val books = libraryDao.getBooksInCategories(listOf(catId)).first()
                books.forEach { book ->
                    softDeleteBook(book)
                }
                val cat = libraryDao.getAllCategories().first().find { it.id == catId }
                cat?.let { libraryDao.updateCategory(it.copy(isDeleted = true)) }
            }
        } else {
            categoryIds.forEach { catId ->
                val books = libraryDao.getBooksInCategories(listOf(catId)).first()
                books.forEach { book ->
                    libraryDao.updateBook(book.copy(categoryId = null))
                }
                val cat = libraryDao.getAllCategories().first().find { it.id == catId }
                cat?.let { libraryDao.updateCategory(it.copy(isDeleted = true)) }
            }
        }
    }

    private suspend fun getAllSubCategoryIds(parentId: Long): List<Long> {
        val result = mutableListOf(parentId)
        val children = libraryDao.getSubCategories(parentId)
        children.forEach {
            result.addAll(getAllSubCategoryIds(it.id))
        }
        return result
    }

    fun getBooksInHierarchy(categoryId: Long): Flow<List<Book>> {
        return libraryDao.getBooksInCategories(listOf(categoryId))
    }

    suspend fun getRecursiveCategoryIds(categoryId: Long): List<Long> {
        return getAllSubCategoryIds(categoryId)
    }

    fun getBooksForCategoryIds(ids: List<Long>): Flow<List<Book>> {
        return libraryDao.getBooksInCategories(ids)
    }

    val allBooks: Flow<List<Book>> = libraryDao.getAllBooks()

    suspend fun insertBook(book: Book, authorIds: List<Long>) {
        if (book.categoryId != null) {
            val catExists = allCategories.first().any { it.id == book.categoryId }
            if (!catExists) throw Exception("Category does not exist.")
        }

        val authorsList = libraryDao.getAllAuthors().first()
        authorIds.forEach { authorId ->
            if (authorsList.none { it.id == authorId }) throw Exception("Author with ID $authorId does not exist.")
        }

        val id = libraryDao.insertBook(book)
        authorIds.forEach { authorId ->
            libraryDao.insertBookAuthorCrossRef(BookAuthorCrossRef(id, authorId))
        }
        logAudit("books", id, "INSERT", null, book.toString())
    }

    suspend fun softDeleteBook(book: Book) {
        val updatedBook = book.copy(isDeleted = true)
        libraryDao.updateBook(updatedBook)
        logAudit("books", book.id, "SOFT_DELETE", book.toString(), updatedBook.toString())
    }

    fun getCopiesForBook(bookId: Long): Flow<List<BookCopy>> = libraryDao.getCopiesForBook(bookId)

    suspend fun insertBookCopy(copy: BookCopy) {
        val id = libraryDao.insertBookCopy(copy)
        logAudit("book_copies", id, "TAMBAH", null, copy.toString())
    }

    suspend fun updateBookCopy(copy: BookCopy) {
        val old = libraryDao.getCopiesForBook(copy.bookId).first().find { it.id == copy.id }
        libraryDao.updateBookCopy(copy)
        logAudit("book_copies", copy.id, "UPDATE", old.toString(), copy.toString())
    }

    private suspend fun logAudit(tableName: String, entityId: Long, action: String, pre: String?, post: String?) {
        libraryDao.insertAuditLog(AuditLog(tableName = tableName, entityId = entityId, action = action, preValue = pre, postValue = post))
    }

    val auditLogs: Flow<List<AuditLog>> = libraryDao.getAllAuditLogs()
}
