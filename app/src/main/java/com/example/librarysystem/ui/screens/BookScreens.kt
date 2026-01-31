package com.example.librarysystem.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.librarysystem.ui.viewmodel.LibraryViewModel

@Composable
fun BookListScreen(viewModel: LibraryViewModel, navController: NavController) {
    val books by viewModel.books.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val selectedCategory by viewModel.selectedCategoryId.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var isbn by remember { mutableStateOf("") }
    var categoryId by remember { mutableStateOf<Long?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Category Filter
        val selectedTabIndex = if (selectedCategory == null) 0 else categories.indexOfFirst { it.id == selectedCategory } + 1
        
        ScrollableTabRow(selectedTabIndex = selectedTabIndex) {
            Tab(selected = selectedCategory == null, onClick = { viewModel.selectCategory(null) }) {
                Text("Semua", modifier = Modifier.padding(16.dp))
            }
            categories.forEach { category ->
                Tab(selected = selectedCategory == category.id, onClick = { viewModel.selectCategory(category.id) }) {
                    Text(category.name, modifier = Modifier.padding(16.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { showAddDialog = true }) {
            Text("Tambah Buku")
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(books) { book ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        .clickable { navController.navigate("bookDetail/${book.id}") }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = book.title, style = MaterialTheme.typography.titleMedium)
                        Text(text = "ISBN: ${book.isbn}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }

    val authors by viewModel.authors.collectAsState()
    var selectedAuthorIds by remember { mutableStateOf(setOf<Long>()) }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Tambah Buku") },
            text = {
                Column {
                    TextField(value = title, onValueChange = { title = it }, label = { Text("Judul") })
                    TextField(value = isbn, onValueChange = { isbn = it }, label = { Text("ISBN") })
                    
                    var catExpanded by remember { mutableStateOf(false) }
                    Box {
                        OutlinedButton(onClick = { catExpanded = true }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                            Text(text = categories.find { it.id == categoryId }?.name ?: "Pilih Kategori")
                        }
                        DropdownMenu(expanded = catExpanded, onDismissRequest = { catExpanded = false }) {
                            categories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category.name) },
                                    onClick = { categoryId = category.id; catExpanded = false }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Pilih Penulis:", style = MaterialTheme.typography.titleSmall)
                    
                    if (authors.isEmpty()) {
                        Text(
                            "Belum ada penulis. Silakan tambah penulis di tab Penulis terlebih dahulu.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Box(modifier = Modifier.height(150.dp).fillMaxWidth()) {
                        LazyColumn {
                            items(authors) { author ->
                                val isSelected = selectedAuthorIds.contains(author.id)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedAuthorIds = if (isSelected) {
                                                selectedAuthorIds - author.id
                                            } else {
                                                selectedAuthorIds + author.id
                                            }
                                        }
                                        .padding(vertical = 4.dp)
                                ) {
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = { checked ->
                                            selectedAuthorIds = if (checked == true) {
                                                selectedAuthorIds + author.id
                                            } else {
                                                selectedAuthorIds - author.id
                                            }
                                        }
                                    )
                                    Text(author.name, modifier = Modifier.padding(start = 8.dp))
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (title.isNotBlank() && categoryId != null) {
                        viewModel.addBook(title, isbn, categoryId, selectedAuthorIds.toList())
                        showAddDialog = false
                        title = ""
                        isbn = ""
                        categoryId = null
                        selectedAuthorIds = emptySet()
                    }
                }) { Text("Tambah") }
            }
        )
    }
}
