package com.example.librarysystem.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.librarysystem.ui.viewmodel.LibraryViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AuditLogScreen(viewModel: LibraryViewModel) {
    val logs by viewModel.auditLogs.collectAsState()
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Log Audit", style = MaterialTheme.typography.headlineMedium)
        
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(logs) { log ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "${log.action} pada ${log.tableName} (ID: ${log.entityId})", style = MaterialTheme.typography.titleSmall)
                        Text(text = "Waktu: ${dateFormat.format(Date(log.timestamp))}", style = MaterialTheme.typography.bodySmall)
                        if (log.preValue != null) Text(text = "Lama: ${log.preValue}", style = MaterialTheme.typography.bodySmall)
                        if (log.postValue != null) Text(text = "Baru: ${log.postValue}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
