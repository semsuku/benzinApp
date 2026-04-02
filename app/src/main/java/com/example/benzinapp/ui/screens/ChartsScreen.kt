package com.example.benzinapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.benzinapp.ui.MainViewModel
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.entry.entryModelOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartsScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val refuelings by viewModel.refuelings.collectAsState()

    // Preparazione dati per i grafici
    // 1. Andamento Prezzo/L (ordiniamo per data dal più vecchio al più recente)
    val sortedByDate = refuelings.sortedBy { it.dateMillis }
    
    val priceEntries = sortedByDate.mapIndexed { index, ref ->
        FloatEntry(x = index.toFloat(), y = ref.pricePerLiter.toFloat())
    }

    // 2. Spesa totale per inserimento (simulando andamento per spesa)
    val expenseEntries = sortedByDate.mapIndexed { index, ref ->
        FloatEntry(x = index.toFloat(), y = ref.totalPrice.toFloat())
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Grafici e Statistiche") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Indietro")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            if (sortedByDate.isEmpty()) {
                Text("Nessun dato sufficiente per visualizzare i grafici.")
            } else {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Andamento Prezzo Carburante (€/L)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        if (priceEntries.isNotEmpty()) {
                            Chart(
                                chart = lineChart(),
                                model = entryModelOf(priceEntries),
                                startAxis = rememberStartAxis(),
                                bottomAxis = rememberBottomAxis()
                            )
                        }
                    }
                }

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Andamento Spese Totali per Rifornimento (€)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        if (expenseEntries.isNotEmpty()) {
                            Chart(
                                chart = columnChart(),
                                model = entryModelOf(expenseEntries),
                                startAxis = rememberStartAxis(),
                                bottomAxis = rememberBottomAxis()
                            )
                        }
                    }
                }
            }
        }
    }
}
