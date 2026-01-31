package com.example.librarysystem.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.librarysystem.ui.viewmodel.LibraryViewModel

@Composable
fun AuthorListScreen(viewModel: LibraryViewModel) {
    val authors by viewModel.authors.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Button(onClick = { showAddDialog = true }) {
            Text("Tambah Penulis")
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(authors) { author ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = author.name, style = MaterialTheme.typography.titleMedium)
                        Text(text = author.biography, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Tambah Penulis") },
            text = {
                Column {
                    TextField(value = name, onValueChange = { name = it }, label = { Text("Nama") })
                    TextField(value = bio, onValueChange = { bio = it }, label = { Text("Biografi") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.addAuthor(name, bio)
                    showAddDialog = false
                    name = ""
                    bio = ""
                }) { Text("Tambah") }
            }
        )
    }
}
