package com.example.benzinapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.benzinapp.ui.MainViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRefuelingScreen(
    viewModel: MainViewModel,
    initialLiters: Double? = null,
    initialTotalPrice: Double? = null,
    onNavigateBack: () -> Unit
) {
    var litersStr by remember { mutableStateOf(initialLiters?.toString() ?: "") }
    var totalPriceStr by remember { mutableStateOf(initialTotalPrice?.toString() ?: "") }
    var currentKmStr by remember { mutableStateOf("") }

    // Calcolo automatico del prezzo al litro
    val pricePerLiterStr = remember(litersStr, totalPriceStr) {
        val liters = litersStr.replace(",", ".").toDoubleOrNull() ?: 0.0
        val total = totalPriceStr.replace(",", ".").toDoubleOrNull() ?: 0.0
        if (liters > 0 && total > 0) {
            String.format(Locale.US, "%.3f", total / liters)
        } else {
            ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Aggiungi Rifornimento") },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (initialLiters != null || initialTotalPrice != null) {
                Text(
                    text = "Dati estratti automaticamente da Gemini AI ✨",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            OutlinedTextField(
                value = litersStr,
                onValueChange = { litersStr = it },
                label = { Text("Litri (es: 20.5)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = totalPriceStr,
                onValueChange = { totalPriceStr = it },
                label = { Text("Costo Totale (€)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = pricePerLiterStr,
                onValueChange = { /* Non editabile */ },
                label = { Text("Prezzo al Litro (€/L)") },
                readOnly = true,
                enabled = false,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = currentKmStr,
                onValueChange = { currentKmStr = it },
                label = { Text("Km Attuali (opzionale per tracciare consumi)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    val liters = litersStr.replace(",", ".").toDoubleOrNull() ?: 0.0
                    val total = totalPriceStr.replace(",", ".").toDoubleOrNull() ?: 0.0
                    val price = pricePerLiterStr.toDoubleOrNull() ?: 0.0
                    val km = currentKmStr.toIntOrNull() ?: 0

                    if (liters > 0 && total > 0) {
                        viewModel.addRefueling(
                            dateMillis = System.currentTimeMillis(),
                            pricePerLiter = price,
                            liters = liters,
                            totalPrice = total,
                            currentKm = km
                        )
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = litersStr.isNotBlank() && totalPriceStr.isNotBlank() && pricePerLiterStr.isNotBlank()
            ) {
                Text("Salva Spesa")
            }
        }
    }
}
