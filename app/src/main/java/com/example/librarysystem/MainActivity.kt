package com.example.librarysystem

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import com.example.librarysystem.data.LibraryDatabase
import com.example.librarysystem.data.repository.LibraryRepository
import com.example.librarysystem.ui.screens.*
import com.example.librarysystem.ui.viewmodel.LibraryViewModel
import com.example.librarysystem.ui.viewmodel.LibraryViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val database = LibraryDatabase.getDatabase(this)
        val repository = LibraryRepository(database.libraryDao())
        val factory = LibraryViewModelFactory(repository)

        setContent {
            LibraryApp(factory)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryApp(factory: LibraryViewModelFactory) {
    val navController = rememberNavController()
    val viewModel: LibraryViewModel = viewModel(factory = factory)
    val errorMessage by viewModel.errorMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Manajemen Perpustakaan") },
                navigationIcon = {
                    if (currentRoute != "books") {
                        IconButton(onClick = { navController.navigate("books") {
                            popUpTo("books") { inclusive = true }
                        } }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Kembali ke Buku")
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                val items = listOf(
                    "books" to "Buku",
                    "categories" to "Kategori",
                    "authors" to "Penulis",
                    "logs" to "Log"
                )
                items.forEach { (route, label) ->
                    NavigationBarItem(
                        icon = { Text(label) },
                        selected = currentRoute == route,
                        onClick = { 
                            navController.navigate(route) {
                                popUpTo("books") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(navController, startDestination = "books", modifier = Modifier.padding(padding)) {
            composable("books") { BookListScreen(viewModel, navController) }
            composable("categories") { CategoryListScreen(viewModel) }
            composable("authors") { AuthorListScreen(viewModel) }
            composable("logs") { AuditLogScreen(viewModel) }
            composable("bookDetail/{bookId}") { backStackEntry ->
                val bookId = backStackEntry.arguments?.getString("bookId")?.toLongOrNull()
                bookId?.let { BookDetailScreen(it, viewModel) }
            }
        }
    }
}
