package com.example.benzinapp.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.benzinapp.ui.MainViewModel
import com.example.benzinapp.ui.theme.ComicBlack
import com.example.benzinapp.ui.theme.ComicYellow
import com.example.benzinapp.ui.theme.LightBlueSky
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
    val sortedByDate = refuelings.sortedBy { it.dateMillis }
    
    val priceEntries = sortedByDate.mapIndexed { index, ref ->
        FloatEntry(x = index.toFloat(), y = ref.pricePerLiter.toFloat())
    }

    val expenseEntries = sortedByDate.mapIndexed { index, ref ->
        FloatEntry(x = index.toFloat(), y = ref.totalPrice.toFloat())
    }

    Scaffold(
        containerColor = LightBlueSky,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LightBlueSky,
                    titleContentColor = ComicBlack
                ),
                title = { 
                    Text(
                        "STATISTICHE!", 
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Indietro", tint = ComicBlack)
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
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            if (sortedByDate.isEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(3.dp, ComicBlack),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        "NESSUN DATO! CORRI A FARE BENZINA!",
                        modifier = Modifier.padding(16.dp),
                        fontWeight = FontWeight.Bold,
                        color = ComicBlack
                    )
                }
            } else {
                // Grafico Prezzo
                ComicChartCard(title = "ANDAMENTO PREZZO (€/L)") {
                    if (priceEntries.isNotEmpty()) {
                        Chart(
                            chart = lineChart(),
                            model = entryModelOf(priceEntries),
                            startAxis = rememberStartAxis(),
                            bottomAxis = rememberBottomAxis()
                        )
                    }
                }

                // Grafico Spese
                ComicChartCard(title = "SPESE TOTALI (€)") {
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

@Composable
fun ComicChartCard(title: String, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .offset(x = 6.dp, y = 6.dp)
            .background(ComicBlack)
            .offset(x = (-6).dp, y = (-6).dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = ComicYellow),
            border = BorderStroke(4.dp, ComicBlack),
            shape = RoundedCornerShape(0.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = ComicBlack
                )
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    thickness = 3.dp,
                    color = ComicBlack
                )
                Box(modifier = Modifier.height(200.dp)) {
                    content()
                }
            }
        }
    }
}
