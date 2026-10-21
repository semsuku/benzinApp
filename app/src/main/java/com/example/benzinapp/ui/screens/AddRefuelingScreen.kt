package com.example.benzinapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.benzinapp.R
import com.example.benzinapp.data.Refueling
import com.example.benzinapp.ui.MainViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRefuelingScreen(
    viewModel: MainViewModel,
    initialLiters: Double? = null,
    initialTotalPrice: Double? = null,
    initialKm: Int? = null,
    refuelingToEdit: Refueling? = null,
    onNavigateBack: () -> Unit
) {
    val refuelings by viewModel.refuelings.collectAsState()

    // Calcolo km di riferimento dal rifornimento precedente
    val previousKm = remember(refuelings, refuelingToEdit) {
        if (refuelingToEdit != null) {
            refuelings.filter { it.id != refuelingToEdit.id && it.dateMillis <= refuelingToEdit.dateMillis }
                .maxByOrNull { it.dateMillis }?.currentKm ?: 0
        } else {
            refuelings.firstOrNull()?.currentKm ?: 0
        }
    }

    var totalPriceStr by remember { 
        mutableStateOf(
            refuelingToEdit?.totalPrice?.let { String.format(Locale.US, "%.2f", it) } 
                ?: initialTotalPrice?.let { String.format(Locale.US, "%.2f", it) } 
                ?: ""
        ) 
    }

    var pricePerLiterStr by remember { 
        mutableStateOf(
            refuelingToEdit?.pricePerLiter?.let { String.format(Locale.US, "%.3f", it) }
                ?: if (initialTotalPrice != null && initialLiters != null && initialLiters > 0) {
                    String.format(Locale.US, "%.3f", initialTotalPrice / initialLiters)
                } else {
                    ""
                }
        ) 
    }

    // Calcolo automatico dei litri da Costo Totale e Prezzo al Litro
    val litersStr = remember(totalPriceStr, pricePerLiterStr) {
        val total = totalPriceStr.replace(",", ".").toDoubleOrNull() ?: 0.0
        val price = pricePerLiterStr.replace(",", ".").toDoubleOrNull() ?: 0.0
        if (total > 0.0 && price > 0.0) {
            String.format(Locale.US, "%.2f", total / price)
        } else {
            ""
        }
    }

    var currentKmStr by remember { 
        mutableStateOf(
            refuelingToEdit?.currentKm?.toString() 
                ?: initialKm?.toString() 
                ?: if (previousKm > 0) previousKm.toString() else ""
        ) 
    }

    // Imposta previousKm se initialKm o refuelingToEdit non sono presenti e previousKm viene caricato successivamente
    LaunchedEffect(previousKm) {
        if (currentKmStr.isBlank() && previousKm > 0 && refuelingToEdit == null && initialKm == null) {
            currentKmStr = previousKm.toString()
        }
    }

    var dateStr by remember { 
        mutableStateOf(
            if (refuelingToEdit != null) {
                formatMillis(refuelingToEdit.dateMillis)
            } else {
                formatMillis(System.currentTimeMillis())
            }
        ) 
    }
    var showDatePicker by remember { mutableStateOf(false) }
    var showOdometerPicker by remember { mutableStateOf(false) }
    var showMoneyPicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (refuelingToEdit != null) stringResource(R.string.edit_refueling) else stringResource(R.string.add_refueling)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
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
            if (initialLiters != null || initialTotalPrice != null || initialKm != null) {
                Text(
                    text = stringResource(R.string.gemini_extracted_data),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Data Rifornimento
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = dateStr,
                    onValueChange = {},
                    label = { Text("Data Rifornimento") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { showDatePicker = true }
                )
            }

            // Costo Totale (€)
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = totalPriceStr,
                    onValueChange = {},
                    label = { Text(stringResource(R.string.total_cost_hint)) },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { showMoneyPicker = true }
                )
            }

            // Prezzo al Litro (€/L) - EDITABILE
            OutlinedTextField(
                value = pricePerLiterStr,
                onValueChange = { input ->
                    pricePerLiterStr = input.replace(',', '.')
                },
                label = { Text(stringResource(R.string.price_per_liter_hint)) },
                placeholder = { Text("es. 1.789") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Litri - CALCOLATO IN AUTOMATICO
            OutlinedTextField(
                value = litersStr,
                onValueChange = { /* Calcolato automaticamente */ },
                label = { Text("${stringResource(R.string.liters_hint)} (calcolo automatico)") },
                readOnly = true,
                enabled = false,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Km Attuali - Default precedente se vuoto
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = if (currentKmStr.isNotBlank()) currentKmStr else if (previousKm > 0) "$previousKm (default prec.)" else "",
                    onValueChange = {},
                    label = { Text(stringResource(R.string.current_km)) },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { showOdometerPicker = true }
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            val total = totalPriceStr.replace(",", ".").toDoubleOrNull() ?: 0.0
            val price = pricePerLiterStr.replace(",", ".").toDoubleOrNull() ?: 0.0
            val liters = litersStr.replace(",", ".").toDoubleOrNull() ?: (if (price > 0) total / price else 0.0)
            val isFormValid = total > 0.0 && price > 0.0 && liters > 0.0

            Button(
                onClick = {
                    val kmInput = currentKmStr.toIntOrNull()
                    val km = if (kmInput != null && kmInput > 0) {
                        kmInput
                    } else if (previousKm > 0) {
                        previousKm
                    } else {
                        0
                    }
                    val parsedDate = parseDate(dateStr) ?: System.currentTimeMillis()

                    if (isFormValid) {
                        if (refuelingToEdit != null) {
                            viewModel.updateRefueling(
                                id = refuelingToEdit.id,
                                dateMillis = parsedDate,
                                pricePerLiter = price,
                                liters = liters,
                                totalPrice = total,
                                currentKm = km
                            )
                        } else {
                            viewModel.addRefueling(
                                dateMillis = parsedDate,
                                pricePerLiter = price,
                                liters = liters,
                                totalPrice = total,
                                currentKm = km
                            )
                        }
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = isFormValid
            ) {
                Text(if (refuelingToEdit != null) stringResource(R.string.save_changes) else stringResource(R.string.save_expense))
            }

            if (showDatePicker) {
                DatePickerDialogPicker(
                    initialSelectedDateMillis = parseDate(dateStr) ?: System.currentTimeMillis(),
                    onDateSelected = { millis ->
                        dateStr = formatMillis(millis)
                    },
                    onDismiss = { showDatePicker = false }
                )
            }
            if (showOdometerPicker) {
                OdometerPickerDialog(
                    initialValue = currentKmStr.toIntOrNull() ?: if (previousKm > 0) previousKm else 0,
                    title = stringResource(R.string.current_km),
                    onValueSelected = { valVal ->
                        currentKmStr = valVal.toString()
                    },
                    onDismiss = { showOdometerPicker = false }
                )
            }
            if (showMoneyPicker) {
                MoneyPickerDialog(
                    initialValue = totalPriceStr.replace(",", ".").toDoubleOrNull() ?: 0.0,
                    title = stringResource(R.string.total_cost_hint),
                    onValueSelected = { valVal ->
                        totalPriceStr = String.format(Locale.US, "%.2f", valVal)
                    },
                    onDismiss = { showMoneyPicker = false }
                )
            }
        }
    }
}
