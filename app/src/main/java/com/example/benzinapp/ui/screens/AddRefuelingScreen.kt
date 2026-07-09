package com.example.benzinapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.benzinapp.data.Refueling
import com.example.benzinapp.ui.MainViewModel
import androidx.compose.ui.res.stringResource
import com.example.benzinapp.R
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
    var litersStr by remember { mutableStateOf(refuelingToEdit?.liters?.toString() ?: initialLiters?.toString() ?: "") }
    var totalPriceStr by remember { mutableStateOf(refuelingToEdit?.totalPrice?.toString() ?: initialTotalPrice?.toString() ?: "") }
    var currentKmStr by remember { mutableStateOf(refuelingToEdit?.currentKm?.toString() ?: initialKm?.toString() ?: "") }
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

            OutlinedTextField(
                value = litersStr,
                onValueChange = { litersStr = it },
                label = { Text(stringResource(R.string.liters_hint)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

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

            OutlinedTextField(
                value = pricePerLiterStr,
                onValueChange = { /* Non editabile */ },
                label = { Text(stringResource(R.string.price_per_liter_hint)) },
                readOnly = true,
                enabled = false,
                modifier = Modifier.fillMaxWidth()
            )

            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = currentKmStr,
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

            Button(
                onClick = {
                    val liters = litersStr.replace(",", ".").toDoubleOrNull() ?: 0.0
                    val total = totalPriceStr.replace(",", ".").toDoubleOrNull() ?: 0.0
                    val price = pricePerLiterStr.toDoubleOrNull() ?: 0.0
                    val km = currentKmStr.toIntOrNull() ?: 0
                    val parsedDate = parseDate(dateStr) ?: System.currentTimeMillis()
 
                    if (liters > 0 && total > 0) {
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
                enabled = litersStr.isNotBlank() && totalPriceStr.isNotBlank() && pricePerLiterStr.isNotBlank()
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
                    initialValue = currentKmStr.toIntOrNull() ?: 0,
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
