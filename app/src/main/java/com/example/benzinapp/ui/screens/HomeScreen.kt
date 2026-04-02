package com.example.benzinapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.benzinapp.data.Refueling
import com.example.benzinapp.ui.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToAdd: () -> Unit,
    onNavigateToCamera: () -> Unit,
    onNavigateToCharts: () -> Unit
) {
    val refuelings by viewModel.refuelings.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("BenzinApp", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onNavigateToCharts) {
                        Icon(Icons.Default.BarChart, contentDescription = "Grafici")
                    }
                }
            )
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                SmallFloatingActionButton(
                    onClick = onNavigateToCamera,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Usa Foto / Gemini")
                }
                FloatingActionButton(
                    onClick = onNavigateToAdd,
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Aggiungi Spesa")
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (refuelings.isEmpty()) {
                item {
                    Text(
                        "Nessuna spesa registrata. Inizia aggiungendo un rifornimento!",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            items(refuelings) { refueling ->
                RefuelingCard(refueling = refueling)
            }
        }
    }
}

@Composable
fun RefuelingCard(refueling: Refueling) {
    val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    val dateString = sdf.format(Date(refueling.dateMillis))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Data: $dateString", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Totale: € ${String.format(Locale.US, "%.2f", refueling.totalPrice)}",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "${String.format(Locale.US, "%.2f", refueling.liters)} L",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Prezzo/L: € ${String.format(Locale.US, "%.3f", refueling.pricePerLiter)}", style = MaterialTheme.typography.bodyMedium)
            }
            if (refueling.currentKm > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Km attuali: ${refueling.currentKm}", style = MaterialTheme.typography.bodySmall)
                    if (refueling.kmDrivenSinceLast > 0) {
                        Text(
                            text = "+${refueling.kmDrivenSinceLast} km dall'ultimo",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
