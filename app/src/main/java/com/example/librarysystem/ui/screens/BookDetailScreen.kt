package com.example.librarysystem.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.librarysystem.ui.viewmodel.LibraryViewModel
import com.example.librarysystem.data.entity.Book

@Composable
fun BookDetailScreen(bookId: Long, viewModel: LibraryViewModel) {
    val books by viewModel.books.collectAsState()
    val book = books.find { it.id == bookId }
    val copies by viewModel.getCopies(bookId).collectAsState(initial = emptyList())
    val allAuthors by viewModel.authors.collectAsState()
    
    // In a real app, we should fetch authors for this specific book from the repository
    // For now, we'll show a simple "Authors linked to this book" label or filter
    // Let's assume we want to show which authors are assigned.
    
    var physicalId by remember { mutableStateOf("") }

    if (book == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Buku tidak ditemukan")
        }
        return
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = book.title, style = MaterialTheme.typography.headlineMedium)
        Text(text = "ISBN: ${book.isbn}", style = MaterialTheme.typography.bodyLarge)
        
        // Find category name
        val categories by viewModel.categories.collectAsState()
        val categoryName = categories.find { it.id == book.categoryId }?.name ?: "Tanpa Kategori"
        Text(text = "Kategori: $categoryName", style = MaterialTheme.typography.bodyMedium)

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Eksemplar", style = MaterialTheme.typography.titleLarge)
        
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            TextField(
                value = physicalId,
                onValueChange = { physicalId = it },
                label = { Text("ID Fisik Eksemplar Baru") },
                modifier = Modifier.weight(1f)
            )
            Button(onClick = {
                viewModel.addCopy(bookId, physicalId)
                physicalId = ""
            }, modifier = Modifier.padding(start = 8.dp)) {
                Text("Tambah")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(copies) { copy ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "ID: ${copy.physicalId}")
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = copy.status, style = MaterialTheme.typography.bodyMedium)
                            Switch(
                                checked = copy.status == "TERSEDIA",
                                onCheckedChange = { isAvailable ->
                                    viewModel.updateCopyStatus(copy, if (isAvailable) "TERSEDIA" else "DIPINJAM")
                                },
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
