package com.example.benzinapp.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.benzinapp.data.Refueling
import com.example.benzinapp.ui.MainViewModel
import com.example.benzinapp.ui.theme.ComicBlack
import com.example.benzinapp.ui.theme.ComicBlue
import com.example.benzinapp.ui.theme.ComicYellow
import com.example.benzinapp.ui.theme.LightBlueSky
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
        containerColor = LightBlueSky,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LightBlueSky,
                    titleContentColor = ComicBlack
                ),
                title = { 
                    Text(
                        "BENZIN APP!", 
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.headlineMedium,
                        letterSpacing = 2.sp
                    ) 
                },
                actions = {
                    IconButton(onClick = onNavigateToCharts) {
                        Icon(Icons.Default.BarChart, contentDescription = "Grafici", tint = ComicBlack)
                    }
                }
            )
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                // Bottone Camera - Semplificato per evitare l'effetto "tutto nero"
                SmallFloatingActionButton(
                    onClick = onNavigateToCamera,
                    containerColor = ComicBlue,
                    contentColor = ComicBlack,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .border(3.dp, ComicBlack, RoundedCornerShape(8.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt, 
                        contentDescription = "Usa Foto",
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Bottone Add stile fumetto
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .border(4.dp, ComicBlack, RoundedCornerShape(12.dp))
                ) {
                    IconButton(
                        onClick = onNavigateToAdd,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Aggiungi", tint = ComicBlack, modifier = Modifier.size(32.dp))
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (refuelings.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(3.dp, ComicBlack),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            "NESSUNA SPESA! AGGIUNGI QUALCOSA!",
                            modifier = Modifier.padding(16.dp),
                            fontWeight = FontWeight.Bold,
                            color = ComicBlack
                        )
                    }
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
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val dateString = sdf.format(Date(refueling.dateMillis))

    // Card con bordo spesso nero e angoli vivi per stile comic
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .offset(x = 4.dp, y = 4.dp) // Ombra "hard"
            .background(ComicBlack, RoundedCornerShape(0.dp))
            .offset(x = (-4).dp, y = (-4).dp),
        colors = CardDefaults.cardColors(containerColor = ComicYellow),
        border = BorderStroke(4.dp, ComicBlack),
        shape = RoundedCornerShape(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "DATA: $dateString", 
                fontWeight = FontWeight.ExtraBold,
                style = MaterialTheme.typography.labelLarge,
                color = ComicBlack
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "TOTALE: € ${String.format(Locale.US, "%.2f", refueling.totalPrice)}",
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.titleLarge,
                    color = ComicBlack
                )
                Text(
                    text = "${String.format(Locale.US, "%.2f", refueling.liters)} L",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = ComicBlack
                )
            }
            HorizontalDivider(color = ComicBlack, thickness = 3.dp, modifier = Modifier.padding(vertical = 8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "PREZZO/L: € ${String.format(Locale.US, "%.3f", refueling.pricePerLiter)}", 
                    fontWeight = FontWeight.Bold,
                    color = ComicBlack
                )
            }
            if (refueling.currentKm > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = ComicBlack,
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = " KM: ${refueling.currentKm} ${if (refueling.kmDrivenSinceLast > 0) "(+${refueling.kmDrivenSinceLast}) " else " "}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
