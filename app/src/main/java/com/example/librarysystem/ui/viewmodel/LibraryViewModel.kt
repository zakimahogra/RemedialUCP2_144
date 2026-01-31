package com.example.librarysystem.ui.viewmodel

import androidx.lifecycle.*
import com.example.librarysystem.data.entity.*
import com.example.librarysystem.data.repository.LibraryRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class LibraryViewModel(private val repository: LibraryRepository) : ViewModel() {

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun clearError() { _errorMessage.value = null }

    val authors: StateFlow<List<Author>> = repository.allAuthors.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addAuthor(name: String, bio: String) {
        viewModelScope.launch {
            try {
                repository.insertAuthor(Author(name = name, biography = bio))
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    val categories: StateFlow<List<Category>> = repository.allCategories.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addCategory(name: String, parentId: Long?) {
        viewModelScope.launch {
            try {
                repository.insertCategory(Category(name = name, parentId = parentId))
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    fun deleteCategory(categoryId: Long, deleteBooks: Boolean, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            try {
                repository.deleteCategory(categoryId, deleteBooks)
                onSuccess()
            } catch (e: Exception) {
                _errorMessage.value = e.message
                onError(e.message ?: "Unknown error")
            }
        }
    }

    private val _selectedCategoryId = MutableStateFlow<Long?>(null)
    val selectedCategoryId: StateFlow<Long?> = _selectedCategoryId

    val books: StateFlow<List<Book>> = _selectedCategoryId.flatMapLatest { categoryId ->
        if (categoryId == null) {
            repository.allBooks
        } else {
            val ids = repository.getRecursiveCategoryIds(categoryId)
            repository.getBooksForCategoryIds(ids)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectCategory(categoryId: Long?) {
        _selectedCategoryId.value = categoryId
    }

    fun addBook(title: String, isbn: String, categoryId: Long?, authorIds: List<Long>) {
        viewModelScope.launch {
            try {
                repository.insertBook(Book(title = title, isbn = isbn, categoryId = categoryId), authorIds)
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    fun deleteBook(book: Book) {
        viewModelScope.launch {
            try {
                repository.softDeleteBook(book)
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    fun getCopies(bookId: Long): Flow<List<BookCopy>> = repository.getCopiesForBook(bookId)

    fun addCopy(bookId: Long, physicalId: String) {
        viewModelScope.launch {
            try {
                repository.insertBookCopy(BookCopy(bookId = bookId, physicalId = physicalId, status = "TERSEDIA"))
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    fun updateCopyStatus(copy: BookCopy, status: String) {
        viewModelScope.launch {
            try {
                repository.updateBookCopy(copy.copy(status = status))
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    val auditLogs: StateFlow<List<AuditLog>> = repository.auditLogs.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

class LibraryViewModelFactory(private val repository: LibraryRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LibraryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LibraryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
