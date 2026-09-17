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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.benzinapp.ui.MainViewModel
import androidx.compose.ui.res.stringResource
import com.example.benzinapp.R
import com.example.benzinapp.ui.theme.ComicBlack
import com.example.benzinapp.ui.theme.ComicYellow
import com.example.benzinapp.ui.theme.LightBlueSky
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.component.lineComponent
import com.patrykandpatrick.vico.core.chart.line.LineChart
import com.patrykandpatrick.vico.core.component.shape.shader.DynamicShaders
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.entry.entryModelOf
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartsScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val refuelings by viewModel.refuelings.collectAsState()

    // Preparazione dati per i grafici
    val sortedByDate = refuelings.sortedBy { it.dateMillis }

    // 1. Andamento Prezzo (per ogni rifornimento)
    val priceEntries = sortedByDate.mapIndexed { index, ref ->
        FloatEntry(x = index.toFloat(), y = ref.pricePerLiter.toFloat())
    }

    // Raggruppamento per mese per i grafici a colonne
    val monthlyGroups = sortedByDate.groupBy { ref ->
        val cal = Calendar.getInstance().apply { timeInMillis = ref.dateMillis }
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        cal.timeInMillis
    }.toSortedMap()

    // 2. Spesa Totale Mensile
    val monthlyExpenseData = monthlyGroups.entries.map { (timeMillis, list) ->
        val label = SimpleDateFormat("MMM\nyyyy", Locale.getDefault()).format(Date(timeMillis)).uppercase()
        val value = list.sumOf { it.totalPrice }
        Pair(label, value)
    }

    // 3. KM Totali Mensili
    val monthlyKmData = monthlyGroups.entries.map { (timeMillis, list) ->
        val label = SimpleDateFormat("MMM\nyyyy", Locale.getDefault()).format(Date(timeMillis)).uppercase()
        val value = list.sumOf { it.kmDrivenSinceLast }.toDouble()
        Pair(label, value)
    }

    // 4. Calcolo Consumo Medio (km/l)
    val validConsumptionRefuelings = sortedByDate.filter { it.kmDrivenSinceLast > 0 && it.liters > 0 }
    val consumptionEntries = validConsumptionRefuelings.mapIndexed { index, ref ->
        FloatEntry(x = index.toFloat(), y = (ref.kmDrivenSinceLast.toDouble() / ref.liters).toFloat())
    }
    val totalKmTracked = validConsumptionRefuelings.sumOf { it.kmDrivenSinceLast }
    val totalLitersTracked = validConsumptionRefuelings.sumOf { it.liters }
    val overallAvgConsumption = if (totalLitersTracked > 0) totalKmTracked.toDouble() / totalLitersTracked else 0.0

    val monthlyConsumptionData = monthlyGroups.entries.mapNotNull { (timeMillis, list) ->
        val monthKm = list.sumOf { it.kmDrivenSinceLast }
        val monthLiters = list.sumOf { it.liters }
        if (monthKm > 0 && monthLiters > 0) {
            val label = SimpleDateFormat("MMM\nyyyy", Locale.getDefault()).format(Date(timeMillis)).uppercase()
            Pair(label, monthKm.toDouble() / monthLiters)
        } else {
            null
        }
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
                        stringResource(R.string.statistics),
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back), tint = ComicBlack)
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
                        stringResource(R.string.no_data_charts),
                        modifier = Modifier.padding(16.dp),
                        fontWeight = FontWeight.Bold,
                        color = ComicBlack
                    )
                }
            } else {
                // Grafico Consumo Medio (km/l)
                ComicChartCard(title = stringResource(R.string.consumption_trend)) {
                    if (validConsumptionRefuelings.isEmpty()) {
                        Text(
                            text = stringResource(R.string.insufficient_data_consumption),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = ComicBlack
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Badge Media Globale
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White,
                                border = BorderStroke(2.dp, ComicBlack),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = stringResource(R.string.overall_avg_consumption),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ComicBlack
                                    )
                                    Text(
                                        text = "${String.format(Locale.US, "%.1f", overallAvgConsumption)} km/l",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFFE65100)
                                    )
                                }
                            }

                            // Grafico ad andamento continuo tra i rifornimenti
                            if (consumptionEntries.isNotEmpty()) {
                                Text(
                                    text = "ANDAMENTO PER RIFORNIMENTO",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = ComicBlack
                                )
                                Box(modifier = Modifier.height(190.dp)) {
                                    Chart(
                                        chart = lineChart(
                                            lines = listOf(
                                                LineChart.LineSpec(
                                                    lineColor = android.graphics.Color.rgb(230, 81, 0),
                                                    lineThicknessDp = 3f
                                                )
                                            )
                                        ),
                                        model = entryModelOf(consumptionEntries),
                                        startAxis = rememberStartAxis(),
                                        bottomAxis = rememberBottomAxis()
                                    )
                                }
                            }

                            // Grafico a barre per il consumo medio mensile
                            if (monthlyConsumptionData.isNotEmpty()) {
                                Text(
                                    text = stringResource(R.string.monthly_avg_consumption),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = ComicBlack,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                                MonthlyBarChart(
                                    data = monthlyConsumptionData,
                                    labelFormatter = { value -> "${String.format(Locale.US, "%.1f", value)} km/l" },
                                    barColor = Color(0xFFFF9800)
                                )
                            }
                        }
                    }
                }

                // Grafico Prezzo (Vico line chart)
                ComicChartCard(title = stringResource(R.string.price_trend)) {
                    if (priceEntries.isNotEmpty()) {
                        Box(modifier = Modifier.height(200.dp)) {
                            Chart(
                                chart = lineChart(
                                    lines = listOf(
                                        LineChart.LineSpec(
                                            lineColor = android.graphics.Color.RED,
                                            lineThicknessDp = 3f
                                        )
                                    )
                                ),
                                model = entryModelOf(priceEntries),
                                startAxis = rememberStartAxis(),
                                bottomAxis = rememberBottomAxis()
                            )
                        }
                    }
                }

                // Grafico Spese Mensili (custom bar chart con etichette dentro)
                ComicChartCard(title = stringResource(R.string.monthly_total_expense)) {
                    if (monthlyExpenseData.isNotEmpty()) {
                        MonthlyBarChart(
                            data = monthlyExpenseData,
                            labelFormatter = { value -> "€ ${String.format(Locale.US, "%.0f", value)}" },
                            barColor = Color(0xFF4A90D9)
                        )
                    }
                }

                // Grafico KM Mensili (custom bar chart con etichette dentro)
                ComicChartCard(title = stringResource(R.string.monthly_km)) {
                    if (monthlyKmData.isNotEmpty()) {
                        MonthlyBarChart(
                            data = monthlyKmData,
                            labelFormatter = { value -> "${value.toInt()} km" },
                            barColor = Color(0xFF6DBF67)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Grafico a barre orizzontali custom con etichetta del valore visibile dentro la barra grigia.
 * Ogni barra occupa una riga: [etichetta mese] [████ valore ████]
 */
@Composable
fun MonthlyBarChart(
    data: List<Pair<String, Double>>,
    labelFormatter: (Double) -> String,
    barColor: Color
) {
    val maxValue = data.maxOfOrNull { it.second }?.takeIf { it > 0 } ?: 1.0

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        data.forEach { (label, value) ->
            val fraction = (value / maxValue).toFloat().coerceIn(0f, 1f)
            val valueText = labelFormatter(value)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Etichetta mese/anno a sinistra
                Text(
                    text = label,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = ComicBlack,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(38.dp),
                    lineHeight = 12.sp
                )

                Spacer(modifier = Modifier.width(6.dp))

                // Barra con valore scritto dentro
                BoxWithConstraints(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(Color(0xFFDDDDDD), RoundedCornerShape(4.dp))
                        .padding(2.dp)
                ) {
                    val totalWidth = maxWidth

                    // Barra colorata proporzionale al valore
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(totalWidth * fraction)
                            .background(barColor, RoundedCornerShape(3.dp))
                    )

                    // Testo del valore centrato verticalmente, sempre visibile sopra la barra
                    Text(
                        text = valueText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = ComicBlack,
                        maxLines = 1,
                        overflow = TextOverflow.Clip,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 6.dp)
                    )
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
                content()
            }
        }
    }
}
