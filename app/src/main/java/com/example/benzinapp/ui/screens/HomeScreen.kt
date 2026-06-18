package com.example.benzinapp.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.TireRepair
import androidx.compose.material.icons.filled.OilBarrel
import androidx.compose.material3.*
import com.example.benzinapp.ui.components.AdMobBanner
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.benzinapp.R
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
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
import com.example.benzinapp.ui.theme.ComicGreen
import com.example.benzinapp.ui.theme.ComicRed
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
    onNavigateToEdit: (Refueling) -> Unit,
    onNavigateToCamera: () -> Unit,
    onNavigateToGallery: () -> Unit,
    onNavigateToCharts: () -> Unit
) {
    val refuelings by viewModel.refuelings.collectAsState()
    val tireRotationKm by viewModel.tireRotationKm.collectAsState()
    val oilChangeKm by viewModel.oilChangeKm.collectAsState()
    
    val insuranceExpiryDate by viewModel.insuranceExpiryDate.collectAsState()
    val insuranceAmount by viewModel.insuranceAmount.collectAsState()
    val bolloExpiryDate by viewModel.bolloExpiryDate.collectAsState()
    val bolloAmount by viewModel.bolloAmount.collectAsState()

    var showTireDialog by remember { mutableStateOf(false) }
    var tireInput by remember { mutableStateOf("") }
    
    var showOilDialog by remember { mutableStateOf(false) }
    var oilInput by remember { mutableStateOf("") }

    var showInsuranceDialog by remember { mutableStateOf(false) }
    var insuranceDateInput by remember { mutableStateOf("") }
    var insuranceAmountInput by remember { mutableStateOf("") }

    var showBolloDialog by remember { mutableStateOf(false) }
    var bolloDateInput by remember { mutableStateOf("") }
    var bolloAmountInput by remember { mutableStateOf("") }

    val groupedRefuelings = remember(refuelings) {
        refuelings.groupBy { refueling ->
            val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
            sdf.format(Date(refueling.dateMillis)).uppercase()
        }
    }
    val expandedMonths = remember { mutableStateMapOf<String, Boolean>() }

    if (showTireDialog) {
        AlertDialog(
            onDismissRequest = { showTireDialog = false },
            titleContentColor = ComicBlack,
            textContentColor = ComicBlack,
            title = { Text(stringResource(R.string.tire_rotation), fontWeight = FontWeight.Bold, color = ComicBlack) },
            text = {
                OutlinedTextField(
                    value = tireInput,
                    onValueChange = { tireInput = it },
                    label = { Text(stringResource(R.string.insert_km_for_tire_rotation)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = ComicBlack,
                        unfocusedTextColor = ComicBlack,
                        focusedLabelColor = ComicBlack,
                        unfocusedLabelColor = ComicBlack,
                        cursorColor = ComicBlack,
                        focusedBorderColor = ComicBlack,
                        unfocusedBorderColor = ComicBlack
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        tireInput.toIntOrNull()?.let {
                            viewModel.updateTireRotationKm(it)
                        }
                        showTireDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ComicBlue)
                ) {
                    Text(stringResource(R.string.save), color = ComicBlack, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTireDialog = false }) {
                    Text(stringResource(R.string.cancel), color = ComicBlack)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(12.dp)
        )
    }

    if (showOilDialog) {
        AlertDialog(
            onDismissRequest = { showOilDialog = false },
            titleContentColor = ComicBlack,
            textContentColor = ComicBlack,
            title = { Text(stringResource(R.string.oil_change), fontWeight = FontWeight.Bold, color = ComicBlack) },
            text = {
                OutlinedTextField(
                    value = oilInput,
                    onValueChange = { oilInput = it },
                    label = { Text(stringResource(R.string.insert_km_for_oil_change)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = ComicBlack,
                        unfocusedTextColor = ComicBlack,
                        focusedLabelColor = ComicBlack,
                        unfocusedLabelColor = ComicBlack,
                        cursorColor = ComicBlack,
                        focusedBorderColor = ComicBlack,
                        unfocusedBorderColor = ComicBlack
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        oilInput.toIntOrNull()?.let {
                            viewModel.updateOilChangeKm(it)
                        }
                        showOilDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ComicBlue)
                ) {
                    Text(stringResource(R.string.save), color = ComicBlack, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showOilDialog = false }) {
                    Text(stringResource(R.string.cancel), color = ComicBlack)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(12.dp)
        )
    }

    if (showInsuranceDialog) {
        MaintenanceDialog(
            title = stringResource(R.string.insurance),
            dateValue = insuranceDateInput,
            onDateChange = { insuranceDateInput = it },
            amountValue = insuranceAmountInput,
            onAmountChange = { insuranceAmountInput = it },
            onSave = {
                val date = parseDate(insuranceDateInput)
                val amount = insuranceAmountInput.toDoubleOrNull() ?: 0.0
                viewModel.updateInsurance(date, amount)
                showInsuranceDialog = false
            },
            onDismiss = { showInsuranceDialog = false }
        )
    }

    if (showBolloDialog) {
        MaintenanceDialog(
            title = stringResource(R.string.car_tax),
            dateValue = bolloDateInput,
            onDateChange = { bolloDateInput = it },
            amountValue = bolloAmountInput,
            onAmountChange = { bolloAmountInput = it },
            onSave = {
                val date = parseDate(bolloDateInput)
                val amount = bolloAmountInput.toDoubleOrNull() ?: 0.0
                viewModel.updateBollo(date, amount)
                showBolloDialog = false
            },
            onDismiss = { showBolloDialog = false }
        )
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
                        stringResource(R.string.app_name).uppercase(), 
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.headlineMedium,
                        letterSpacing = 2.sp
                    ) 
                },
                actions = {
                    IconButton(onClick = onNavigateToCharts) {
                        Icon(Icons.Default.BarChart, contentDescription = stringResource(R.string.charts_desc), tint = ComicBlack)
                    }
                }
            )
        },
        bottomBar = {
            AdMobBanner()
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                Row(
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .horizontalScroll(rememberScrollState())
                ) {
                    // Bottone Assicurazione
                    val insuranceColor = if (insuranceExpiryDate != null) {
                        val daysToExpiry = (insuranceExpiryDate!! - System.currentTimeMillis()) / (24 * 60 * 60 * 1000)
                        if (daysToExpiry > 30) ComicGreen else ComicRed
                    } else ComicGreen

                    SmallFloatingActionButton(
                        onClick = {
                            insuranceDateInput = formatMillis(insuranceExpiryDate)
                            if (insuranceDateInput == "N/D") insuranceDateInput = ""
                            insuranceAmountInput = String.format(Locale.US, "%.2f", insuranceAmount)
                            showInsuranceDialog = true
                        },
                        containerColor = insuranceColor,
                        contentColor = ComicBlack,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .border(3.dp, ComicBlack, RoundedCornerShape(8.dp))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${stringResource(R.string.insurance_abbr)}${formatMillis(insuranceExpiryDate)}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Bottone Bollo
                    val bolloColor = if (bolloExpiryDate != null) {
                        if (System.currentTimeMillis() < bolloExpiryDate!!) ComicGreen else ComicRed
                    } else ComicGreen

                    SmallFloatingActionButton(
                        onClick = {
                            bolloDateInput = formatMillis(bolloExpiryDate)
                            if (bolloDateInput == "N/D") bolloDateInput = ""
                            bolloAmountInput = String.format(Locale.US, "%.2f", bolloAmount)
                            showBolloDialog = true
                        },
                        containerColor = bolloColor,
                        contentColor = ComicBlack,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .border(3.dp, ComicBlack, RoundedCornerShape(8.dp))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${stringResource(R.string.car_tax_abbr)}${formatMillis(bolloExpiryDate)}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }

                    val latestKm = refuelings.firstOrNull()?.currentKm ?: 0
                    val isTireRotationNeeded = tireRotationKm != null && latestKm >= tireRotationKm!!
                    val tireColor = if (isTireRotationNeeded) ComicRed else ComicGreen
 
                    // Bottone Inversione Gomme
                    SmallFloatingActionButton(
                        onClick = {
                            tireInput = tireRotationKm?.toString() ?: ""
                            showTireDialog = true 
                        },
                        containerColor = tireColor,
                        contentColor = ComicBlack,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .border(3.dp, ComicBlack, RoundedCornerShape(8.dp))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.TireRepair,
                                contentDescription = stringResource(R.string.tire_rotation),
                                modifier = Modifier.size(20.dp)
                            )
                            if (tireRotationKm != null) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${tireRotationKm}${stringResource(R.string.km)}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = ComicBlack
                                )
                            }
                        }
                    }

                    // Bottone Cambio Olio
                    val isOilChangeNeeded = oilChangeKm != null && latestKm >= oilChangeKm!!
                    val oilColor = if (isOilChangeNeeded) ComicRed else ComicGreen

                    SmallFloatingActionButton(
                        onClick = {
                            oilInput = oilChangeKm?.toString() ?: ""
                            showOilDialog = true 
                        },
                        containerColor = oilColor,
                        contentColor = ComicBlack,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .border(3.dp, ComicBlack, RoundedCornerShape(8.dp))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.OilBarrel,
                                contentDescription = stringResource(R.string.oil_change),
                                modifier = Modifier.size(20.dp)
                            )
                            if (oilChangeKm != null) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${oilChangeKm}${stringResource(R.string.km)}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = ComicBlack
                                )
                            }
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    // Bottone Galleria
                    SmallFloatingActionButton(
                        onClick = onNavigateToGallery,
                        containerColor = ComicBlue,
                        contentColor = ComicBlack,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .border(3.dp, ComicBlack, RoundedCornerShape(8.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = stringResource(R.string.choose_from_gallery),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Bottone Camera
                    SmallFloatingActionButton(
                        onClick = onNavigateToCamera,
                        containerColor = ComicBlue,
                        contentColor = ComicBlack,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .border(3.dp, ComicBlack, RoundedCornerShape(8.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt, 
                            contentDescription = stringResource(R.string.use_camera),
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
                            Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add), tint = ComicBlack, modifier = Modifier.size(32.dp))
                        }
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
                            stringResource(R.string.no_expenses),
                            modifier = Modifier.padding(16.dp),
                            fontWeight = FontWeight.Bold,
                            color = ComicBlack
                        )
                    }
                }
            } else {
                groupedRefuelings.forEach { (month, monthRefuelings) ->
                    val isExpanded = expandedMonths[month] ?: false
                    item {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expandedMonths[month] = !isExpanded },
                            color = ComicBlue,
                            border = BorderStroke(3.dp, ComicBlack),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = month,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp,
                                    color = ComicBlack
                                )
                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = if (isExpanded) stringResource(R.string.collapse) else stringResource(R.string.expand),
                                    tint = ComicBlack
                                )
                            }
                        }
                    }
                    if (isExpanded) {
                        items(monthRefuelings) { refueling ->
                            RefuelingCard(
                                refueling = refueling,
                                onDelete = { viewModel.deleteRefueling(refueling) },
                                onEdit = { onNavigateToEdit(refueling) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RefuelingCard(refueling: Refueling, onDelete: () -> Unit, onEdit: () -> Unit) {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val dateString = sdf.format(Date(refueling.dateMillis))

    // Card con bordo spesso nero e angoli vivi per stile comic
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .offset(x = 4.dp, y = 4.dp) // Ombra "hard"
            .background(ComicBlack, RoundedCornerShape(0.dp))
            .offset(x = (-4).dp, y = (-4).dp)
            .clickable { onEdit() },
        colors = CardDefaults.cardColors(containerColor = ComicYellow),
        border = BorderStroke(4.dp, ComicBlack),
        shape = RoundedCornerShape(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${stringResource(R.string.date)}$dateString", 
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.labelLarge,
                    color = ComicBlack
                )
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.delete_expense),
                        tint = ComicBlack
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${stringResource(R.string.total_text)}${String.format(Locale.US, "%.2f", refueling.totalPrice)}",
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
                    text = "${stringResource(R.string.price_per_liter_text)}${String.format(Locale.US, "%.3f", refueling.pricePerLiter)}", 
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
                        text = "${stringResource(R.string.km_text)}${refueling.currentKm} ${if (refueling.kmDrivenSinceLast > 0) "(+${refueling.kmDrivenSinceLast}) " else " "}",
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

@Composable
fun MaintenanceDialog(
    title: String,
    dateValue: String,
    onDateChange: (String) -> Unit,
    amountValue: String,
    onAmountChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        titleContentColor = ComicBlack,
        textContentColor = ComicBlack,
        title = { Text(title, fontWeight = FontWeight.Bold, color = ComicBlack) },
        text = {
            Column {
                OutlinedTextField(
                    value = dateValue,
                    onValueChange = onDateChange,
                    label = { Text(stringResource(R.string.expiry_date)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = ComicBlack,
                        unfocusedTextColor = ComicBlack,
                        focusedLabelColor = ComicBlack,
                        unfocusedLabelColor = ComicBlack,
                        cursorColor = ComicBlack,
                        focusedBorderColor = ComicBlack,
                        unfocusedBorderColor = ComicBlack
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = amountValue,
                    onValueChange = onAmountChange,
                    label = { Text(stringResource(R.string.amount)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = ComicBlack,
                        unfocusedTextColor = ComicBlack,
                        focusedLabelColor = ComicBlack,
                        unfocusedLabelColor = ComicBlack,
                        cursorColor = ComicBlack,
                        focusedBorderColor = ComicBlack,
                        unfocusedBorderColor = ComicBlack
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onSave,
                colors = ButtonDefaults.buttonColors(containerColor = ComicBlue)
            ) {
                Text(stringResource(R.string.save), color = ComicBlack, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel), color = ComicBlack)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(12.dp)
    )
}

fun formatMillis(millis: Long?): String {
    if (millis == null || millis == 0L) return "N/D"
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return sdf.format(Date(millis))
}

fun parseDate(dateStr: String): Long? {
    return try {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        sdf.parse(dateStr)?.time
    } catch (e: Exception) {
        null
    }
}
