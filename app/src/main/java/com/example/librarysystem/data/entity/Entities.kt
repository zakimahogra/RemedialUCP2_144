package com.example.librarysystem.data.entity

import androidx.room.*

@Entity(tableName = "authors")
data class Author(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val biography: String
)

@Entity(
    tableName = "categories",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["parentId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index(value = ["parentId"])]
)
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val parentId: Long? = null,
    val isDeleted: Boolean = false
)

@Entity(
    tableName = "books",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index(value = ["categoryId"])]
)
data class Book(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val isbn: String,
    val categoryId: Long?,
    val isDeleted: Boolean = false
)

@Entity(
    tableName = "book_authors",
    primaryKeys = ["bookId", "authorId"],
    foreignKeys = [
        ForeignKey(entity = Book::class, parentColumns = ["id"], childColumns = ["bookId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Author::class, parentColumns = ["id"], childColumns = ["authorId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("authorId")]
)
data class BookAuthorCrossRef(
    val bookId: Long,
    val authorId: Long
)

@Entity(
    tableName = "book_copies",
    foreignKeys = [
        ForeignKey(entity = Book::class, parentColumns = ["id"], childColumns = ["bookId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("bookId")]
)
data class BookCopy(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookId: Long,
    val physicalId: String,
    val status: String
)

@Entity(tableName = "audit_logs")
data class AuditLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tableName: String,
    val entityId: Long,
    val action: String,
    val preValue: String?,
    val postValue: String?,
    val timestamp: Long = System.currentTimeMillis()
)
