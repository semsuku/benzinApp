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
import androidx.compose.material.icons.filled.CarRepair
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material3.*
import com.example.benzinapp.ui.components.AdMobBanner
import com.example.benzinapp.ui.components.ProfileSelector
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
    onNavigateToCharts: () -> Unit,
    onNavigateToStations: () -> Unit
) {
    val profiles by viewModel.profiles.collectAsState()
    val activeProfileId by viewModel.activeProfileId.collectAsState()
    val refuelings by viewModel.refuelings.collectAsState()
    val tireRotationKm by viewModel.tireRotationKm.collectAsState()
    val tireChangeDate by viewModel.tireChangeDate.collectAsState()
    val tireChangeKm by viewModel.tireChangeKm.collectAsState()
    val oilChangeKm by viewModel.oilChangeKm.collectAsState()
    val oilLastChangeDate by viewModel.oilLastChangeDate.collectAsState()
    val oilLastChangeKm by viewModel.oilLastChangeKm.collectAsState()
    val oilIntervalMonths by viewModel.oilIntervalMonths.collectAsState()
    val oilIntervalKm by viewModel.oilIntervalKm.collectAsState()
    val oilNextDate by viewModel.oilNextDate.collectAsState()
    
    val insuranceExpiryDate by viewModel.insuranceExpiryDate.collectAsState()
    val insuranceAmount by viewModel.insuranceAmount.collectAsState()
    val insuranceCompany by viewModel.insuranceCompany.collectAsState()
    val bolloExpiryDate by viewModel.bolloExpiryDate.collectAsState()
    val bolloAmount by viewModel.bolloAmount.collectAsState()
    val revisioneExpiryDate by viewModel.revisioneExpiryDate.collectAsState()
    val revisioneLastDate by viewModel.revisioneLastDate.collectAsState()
    val revisioneIsNew by viewModel.revisioneIsNew.collectAsState()
    val revisioneAmount by viewModel.revisioneAmount.collectAsState()

    var showTireDialog by remember { mutableStateOf(false) }
    var tireDateInput by remember { mutableStateOf("") }
    var tireKmInput by remember { mutableStateOf("") }
    var tireNextKmInput by remember { mutableStateOf("") }
    
    var showOilDialog by remember { mutableStateOf(false) }
    var oilDateInput by remember { mutableStateOf("") }
    var oilKmInput by remember { mutableStateOf("") }
    var oilIntervalMonthsInput by remember { mutableStateOf("") }
    var oilIntervalKmInput by remember { mutableStateOf("") }

    var showInsuranceDialog by remember { mutableStateOf(false) }
    var insuranceDateInput by remember { mutableStateOf("") }
    var insuranceAmountInput by remember { mutableStateOf("") }
    var insuranceCompanyInput by remember { mutableStateOf("") }
    var showCompanyPicker by remember { mutableStateOf(false) }

    var showBolloDialog by remember { mutableStateOf(false) }
    var bolloDateInput by remember { mutableStateOf("") }
    var bolloAmountInput by remember { mutableStateOf("") }

    var showRevisioneDialog by remember { mutableStateOf(false) }
    var revisioneDateInput by remember { mutableStateOf("") }
    var revisioneIsNewInput by remember { mutableStateOf(false) }
    var revisioneAmountInput by remember { mutableStateOf("") }
    var datePickerTarget by remember { mutableStateOf<((Long) -> Unit)?>(null) }
    var odometerPickerTarget by remember { mutableStateOf<OdometerTarget?>(null) }
    var moneyPickerTarget by remember { mutableStateOf<MoneyTarget?>(null) }
    var refuelingToDelete by remember { mutableStateOf<Refueling?>(null) }
    var areButtonsVisible by remember { mutableStateOf(true) }

    val refuelingsByYearAndMonth = remember(refuelings) {
        val sdfYear = SimpleDateFormat("yyyy", Locale.getDefault())
        val sdfMonth = SimpleDateFormat("MMMM", Locale.getDefault())
        
        val byYear = refuelings.groupBy { sdfYear.format(Date(it.dateMillis)) }
            .toSortedMap(compareByDescending { it })
            
        byYear.mapValues { (_, yearList) ->
            yearList.groupBy { sdfMonth.format(Date(it.dateMillis)).uppercase() }
                .toList()
                .sortedByDescending { (_, list) -> list.firstOrNull()?.dateMillis ?: 0L }
                .toMap()
        }
    }
    val latestYear = remember(refuelingsByYearAndMonth) {
        refuelingsByYearAndMonth.keys.firstOrNull()
    }
    val expandedMonths = remember { mutableStateMapOf<String, Boolean>() }
    val expandedYears = remember { mutableStateMapOf<String, Boolean>() }

    if (showTireDialog) {
        AlertDialog(
            onDismissRequest = { showTireDialog = false },
            titleContentColor = ComicBlack,
            textContentColor = ComicBlack,
            title = { Text(stringResource(R.string.tire_rotation), fontWeight = FontWeight.Bold, color = ComicBlack) },
            text = {
                Column {
                    Box(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                        OutlinedTextField(
                            value = tireDateInput,
                            onValueChange = {},
                            label = { Text("Data Cambio/Inversione") },
                            readOnly = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = ComicBlack,
                                unfocusedTextColor = ComicBlack,
                                focusedLabelColor = ComicBlack,
                                unfocusedLabelColor = ComicBlack,
                                cursorColor = ComicBlack,
                                focusedBorderColor = ComicBlack,
                                unfocusedBorderColor = ComicBlack
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable {
                                    datePickerTarget = { millis ->
                                        tireDateInput = formatMillis(millis)
                                    }
                                }
                        )
                    }
                    Box(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                        OutlinedTextField(
                            value = tireKmInput,
                            onValueChange = {},
                            label = { Text("Km all'Inversione/Cambio") },
                            readOnly = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = ComicBlack,
                                unfocusedTextColor = ComicBlack,
                                focusedLabelColor = ComicBlack,
                                unfocusedLabelColor = ComicBlack,
                                cursorColor = ComicBlack,
                                focusedBorderColor = ComicBlack,
                                unfocusedBorderColor = ComicBlack
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable {
                                    odometerPickerTarget = OdometerTarget(
                                        title = "Km all'Inversione/Cambio",
                                        initialValue = tireKmInput.toIntOrNull() ?: 0
                                    ) { valVal ->
                                        tireKmInput = valVal.toString()
                                        tireNextKmInput = (valVal + 10000).toString()
                                    }
                                }
                        )
                    }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = tireNextKmInput,
                            onValueChange = {},
                            label = { Text("Km Prossima Inversione") },
                            readOnly = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = ComicBlack,
                                unfocusedTextColor = ComicBlack,
                                focusedLabelColor = ComicBlack,
                                unfocusedLabelColor = ComicBlack,
                                cursorColor = ComicBlack,
                                focusedBorderColor = ComicBlack,
                                unfocusedBorderColor = ComicBlack
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable {
                                    odometerPickerTarget = OdometerTarget(
                                        title = "Km Prossima Inversione",
                                        initialValue = tireNextKmInput.toIntOrNull() ?: 0
                                    ) { valVal ->
                                        tireNextKmInput = valVal.toString()
                                    }
                                }
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val date = parseDate(tireDateInput)
                        val km = tireKmInput.toIntOrNull()
                        val nextKm = tireNextKmInput.toIntOrNull()
                        viewModel.updateTireRotation(date, km, nextKm)
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
            title = { Text("Tagliando / Cambio Olio", fontWeight = FontWeight.Bold, color = ComicBlack) },
            text = {
                Column {
                    Box(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                        OutlinedTextField(
                            value = oilDateInput,
                            onValueChange = {},
                            label = { Text("Data Ultimo Tagliando") },
                            readOnly = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = ComicBlack,
                                unfocusedTextColor = ComicBlack,
                                focusedLabelColor = ComicBlack,
                                unfocusedLabelColor = ComicBlack,
                                cursorColor = ComicBlack,
                                focusedBorderColor = ComicBlack,
                                unfocusedBorderColor = ComicBlack
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable {
                                    datePickerTarget = { millis ->
                                        oilDateInput = formatMillis(millis)
                                    }
                                }
                        )
                    }
                    Box(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                        OutlinedTextField(
                            value = oilKmInput,
                            onValueChange = {},
                            label = { Text("Km Ultimo Tagliando") },
                            readOnly = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = ComicBlack,
                                unfocusedTextColor = ComicBlack,
                                focusedLabelColor = ComicBlack,
                                unfocusedLabelColor = ComicBlack,
                                cursorColor = ComicBlack,
                                focusedBorderColor = ComicBlack,
                                unfocusedBorderColor = ComicBlack
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable {
                                    odometerPickerTarget = OdometerTarget(
                                        title = "Km Ultimo Tagliando",
                                        initialValue = oilKmInput.toIntOrNull() ?: 0
                                    ) { valVal ->
                                        oilKmInput = valVal.toString()
                                    }
                                }
                        )
                    }
                    OutlinedTextField(
                        value = oilIntervalMonthsInput,
                        onValueChange = { oilIntervalMonthsInput = it },
                        label = { Text("Intervallo Tempo (Mesi)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = ComicBlack,
                            unfocusedTextColor = ComicBlack,
                            focusedLabelColor = ComicBlack,
                            unfocusedLabelColor = ComicBlack,
                            cursorColor = ComicBlack,
                            focusedBorderColor = ComicBlack,
                            unfocusedBorderColor = ComicBlack
                        ),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = oilIntervalKmInput,
                            onValueChange = {},
                            label = { Text("Intervallo Chilometrico (Km)") },
                            readOnly = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = ComicBlack,
                                unfocusedTextColor = ComicBlack,
                                focusedLabelColor = ComicBlack,
                                unfocusedLabelColor = ComicBlack,
                                cursorColor = ComicBlack,
                                focusedBorderColor = ComicBlack,
                                unfocusedBorderColor = ComicBlack
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable {
                                    odometerPickerTarget = OdometerTarget(
                                        title = "Intervallo Chilometrico (Km)",
                                        initialValue = oilIntervalKmInput.toIntOrNull() ?: 0
                                    ) { valVal ->
                                        oilIntervalKmInput = valVal.toString()
                                    }
                                }
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val date = parseDate(oilDateInput)
                        val km = oilKmInput.toIntOrNull()
                        val months = oilIntervalMonthsInput.toIntOrNull() ?: 12
                        val kmInt = oilIntervalKmInput.toIntOrNull() ?: 30000
                        viewModel.updateOilChange(date, km, months, kmInt)
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
            onDateClick = {
                datePickerTarget = { millis ->
                    insuranceDateInput = formatMillis(millis)
                }
            },
            amountValue = insuranceAmountInput,
            onAmountClick = {
                moneyPickerTarget = MoneyTarget("Importo Assicurazione", insuranceAmountInput.replace(",", ".").toDoubleOrNull() ?: 0.0) { valVal ->
                    insuranceAmountInput = String.format(Locale.US, "%.2f", valVal)
                }
            },
            companyValue = insuranceCompanyInput,
            onCompanyClick = { showCompanyPicker = true },
            onSave = {
                val date = parseDate(insuranceDateInput)
                val amount = insuranceAmountInput.replace(",", ".").toDoubleOrNull() ?: 0.0
                viewModel.updateInsurance(date, amount, insuranceCompanyInput)
                showInsuranceDialog = false
            },
            onDismiss = { showInsuranceDialog = false }
        )
    }

    if (showBolloDialog) {
        MaintenanceDialog(
            title = stringResource(R.string.car_tax),
            dateValue = bolloDateInput,
            onDateClick = {
                datePickerTarget = { millis ->
                    bolloDateInput = formatMillis(millis)
                }
            },
            amountValue = bolloAmountInput,
            onAmountClick = {
                moneyPickerTarget = MoneyTarget("Importo Bollo", bolloAmountInput.replace(",", ".").toDoubleOrNull() ?: 0.0) { valVal ->
                    bolloAmountInput = String.format(Locale.US, "%.2f", valVal)
                }
            },
            companyValue = null,
            onCompanyClick = null,
            onSave = {
                val date = parseDate(bolloDateInput)
                val amount = bolloAmountInput.replace(",", ".").toDoubleOrNull() ?: 0.0
                viewModel.updateBollo(date, amount)
                showBolloDialog = false
            },
            onDismiss = { showBolloDialog = false }
        )
    }

    if (showRevisioneDialog) {
        RevisioneDialog(
            title = stringResource(R.string.revisione),
            dateValue = revisioneDateInput,
            onDateClick = {
                datePickerTarget = { millis ->
                    revisioneDateInput = formatMillis(millis)
                }
            },
            isNewCar = revisioneIsNewInput,
            onIsNewCarChange = { revisioneIsNewInput = it },
            amountValue = revisioneAmountInput,
            onAmountClick = {
                moneyPickerTarget = MoneyTarget("Importo Revisione", revisioneAmountInput.replace(",", ".").toDoubleOrNull() ?: 0.0) { valVal ->
                    revisioneAmountInput = String.format(Locale.US, "%.2f", valVal)
                }
            },
            onSave = {
                val date = parseDate(revisioneDateInput)
                val amount = revisioneAmountInput.replace(",", ".").toDoubleOrNull() ?: 0.0
                viewModel.updateRevisione(date, revisioneIsNewInput, amount)
                showRevisioneDialog = false
            },
            onDismiss = { showRevisioneDialog = false }
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
                    val context = androidx.compose.ui.platform.LocalContext.current
                    IconButton(onClick = { exportToCsv(context, refuelings) }) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = "Esporta Excel", tint = ComicBlack)
                    }
                    IconButton(onClick = onNavigateToStations) {
                        Icon(Icons.Default.LocalGasStation, contentDescription = "Nearest Stations", tint = ComicBlack)
                    }
                    IconButton(onClick = onNavigateToCharts) {
                        Icon(Icons.Default.BarChart, contentDescription = stringResource(R.string.charts_desc), tint = ComicBlack)
                    }
                }
            )
        },
        // bottomBar = {
        //     AdMobBanner()
        // },
        floatingActionButton = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 32.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .size(width = 28.dp, height = 36.dp)
                        .background(ComicYellow, RoundedCornerShape(8.dp))
                        .border(3.dp, ComicBlack, RoundedCornerShape(8.dp))
                        .clickable { areButtonsVisible = !areButtonsVisible }
                ) {
                    Icon(
                        imageVector = if (areButtonsVisible) Icons.Default.ChevronLeft else Icons.Default.ChevronRight,
                        contentDescription = "Toggle Buttons",
                        modifier = Modifier.size(18.dp),
                        tint = ComicBlack
                    )
                }

                AnimatedVisibility(
                    visible = areButtonsVisible,
                    enter = slideInHorizontally(initialOffsetX = { -it }) + fadeIn(),
                    exit = slideOutHorizontally(targetOffsetX = { -it }) + fadeOut()
                ) {
                    Column(
                        horizontalAlignment = Alignment.Start
                    ) {
                        // Prima riga: Scadenze temporali (Assicurazione, Bollo, Revisione)
                        Row(
                            modifier = Modifier
                                .align(Alignment.Start)
                                .padding(bottom = 8.dp)
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
                                    insuranceCompanyInput = insuranceCompany
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
                                    if (insuranceCompany.isNotBlank()) {
                                        CompanyLogo(name = insuranceCompany, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                    } else {
                                        Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
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

                            // Bottone Revisione
                            val revisioneColor = if (revisioneExpiryDate != null) {
                                val daysToExpiry = (revisioneExpiryDate!! - System.currentTimeMillis()) / (24 * 60 * 60 * 1000)
                                if (daysToExpiry > 30) ComicGreen else ComicRed
                            } else ComicGreen

                            SmallFloatingActionButton(
                                onClick = {
                                    revisioneDateInput = formatMillis(revisioneLastDate)
                                    if (revisioneDateInput == "N/D") revisioneDateInput = ""
                                    revisioneIsNewInput = revisioneIsNew
                                    revisioneAmountInput = String.format(Locale.US, "%.2f", revisioneAmount)
                                    showRevisioneDialog = true
                                },
                                containerColor = revisioneColor,
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
                                    Icon(Icons.Default.CarRepair, contentDescription = null, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${stringResource(R.string.revisione_abbr)}${formatMillis(revisioneExpiryDate)}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        // Seconda riga: Manutenzione chilometrica (Inversione Gomme, Cambio Olio)
                        Row(
                            modifier = Modifier
                                .align(Alignment.Start)
                                .padding(bottom = 16.dp)
                                .horizontalScroll(rememberScrollState())
                        ) {
                            val latestKm = refuelings.firstOrNull()?.currentKm ?: 0
                            val isTireRotationNeeded = tireRotationKm != null && latestKm >= tireRotationKm!!
                            val tireColor = if (isTireRotationNeeded) ComicRed else ComicGreen

                            // Bottone Inversione Gomme
                            SmallFloatingActionButton(
                                onClick = {
                                    tireDateInput = formatMillis(tireChangeDate)
                                    if (tireDateInput == "N/D") tireDateInput = ""
                                    tireKmInput = tireChangeKm?.toString() ?: ""
                                    tireNextKmInput = tireRotationKm?.toString() ?: ""
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
                            val isOilChangeNeeded = (oilChangeKm != null && latestKm >= oilChangeKm!!) ||
                                                    (oilNextDate != null && System.currentTimeMillis() >= oilNextDate!!)
                            val oilColor = if (isOilChangeNeeded) ComicRed else ComicGreen

                            SmallFloatingActionButton(
                                onClick = {
                                    oilDateInput = formatMillis(oilLastChangeDate)
                                    if (oilDateInput == "N/D") oilDateInput = ""
                                    oilKmInput = oilLastChangeKm?.toString() ?: ""
                                    oilIntervalMonthsInput = oilIntervalMonths.toString()
                                    oilIntervalKmInput = oilIntervalKm.toString()
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
                                    val nextOilDateStr = formatMillis(oilNextDate)
                                    if (oilChangeKm != null || nextOilDateStr != "N/D") {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        val displayStr = if (oilChangeKm != null && nextOilDateStr != "N/D") {
                                            "${oilChangeKm}km o $nextOilDateStr"
                                        } else if (oilChangeKm != null) {
                                            "${oilChangeKm}km"
                                        } else {
                                            nextOilDateStr
                                        }
                                        Text(
                                            text = displayStr,
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
                            horizontalArrangement = Arrangement.Start
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
            item {
                ProfileSelector(
                    profiles = profiles,
                    activeProfileId = activeProfileId,
                    onSelectProfile = { id -> viewModel.setActiveProfile(id) },
                    onAddProfile = { name, vehicle, icon, color -> viewModel.addProfile(name, vehicle, icon, color) },
                    onEditProfile = { profile -> viewModel.updateProfile(profile) },
                    onDeleteProfile = { profile -> viewModel.deleteProfile(profile) }
                )
            }

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
                refuelingsByYearAndMonth.forEach { (year, monthsMap) ->
                    val isYearExpanded = expandedYears[year] ?: (year == latestYear)
                    
                    item {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expandedYears[year] = !isYearExpanded },
                            color = ComicYellow,
                            border = BorderStroke(3.dp, ComicBlack),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "ANNO $year",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 20.sp,
                                    color = ComicBlack
                                )
                                Icon(
                                    imageVector = if (isYearExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = if (isYearExpanded) "Collapse Year" else "Expand Year",
                                    tint = ComicBlack
                                )
                            }
                        }
                    }
                    
                    if (isYearExpanded) {
                        monthsMap.forEach { (month, monthRefuelings) ->
                            val isMonthExpanded = expandedMonths["$year-$month"] ?: false
                            
                            item {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 16.dp)
                                        .clickable { expandedMonths["$year-$month"] = !isMonthExpanded },
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
                                            fontSize = 16.sp,
                                            color = ComicBlack
                                        )
                                        Icon(
                                            imageVector = if (isMonthExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                            contentDescription = if (isMonthExpanded) stringResource(R.string.collapse) else stringResource(R.string.expand),
                                            tint = ComicBlack
                                        )
                                    }
                                }
                            }
                            
                            if (isMonthExpanded) {
                                items(monthRefuelings) { refueling ->
                                    Box(modifier = Modifier.padding(start = 32.dp)) {
                                        RefuelingCard(
                                            refueling = refueling,
                                            onDelete = { refuelingToDelete = refueling },
                                            onEdit = { onNavigateToEdit(refueling) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (datePickerTarget != null) {
            DatePickerDialogPicker(
                initialSelectedDateMillis = System.currentTimeMillis(),
                onDateSelected = { millis ->
                    datePickerTarget?.invoke(millis)
                },
                onDismiss = { datePickerTarget = null }
            )
        }
        if (odometerPickerTarget != null) {
            val target = odometerPickerTarget!!
            OdometerPickerDialog(
                initialValue = target.initialValue,
                title = target.title,
                onValueSelected = target.onSelected,
                onDismiss = { odometerPickerTarget = null }
            )
        }
        if (moneyPickerTarget != null) {
            val target = moneyPickerTarget!!
            MoneyPickerDialog(
                initialValue = target.initialValue,
                title = target.title,
                onValueSelected = target.onSelected,
                onDismiss = { moneyPickerTarget = null }
            )
        }
        if (showCompanyPicker) {
            CompanyPickerDialog(
                onCompanySelected = { company ->
                    insuranceCompanyInput = company
                    showCompanyPicker = false
                },
                onDismiss = { showCompanyPicker = false }
            )
        }
        if (refuelingToDelete != null) {
            val refueling = refuelingToDelete!!
            AlertDialog(
                onDismissRequest = { refuelingToDelete = null },
                titleContentColor = ComicBlack,
                textContentColor = ComicBlack,
                title = { Text("Elimina Rifornimento", fontWeight = FontWeight.Bold, color = ComicBlack) },
                text = { Text("Sei sicuro di voler eliminare questo rifornimento? Questa azione non può essere annullata.", color = ComicBlack) },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteRefueling(refueling)
                            refuelingToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ComicRed)
                    ) {
                        Text("Elimina", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { refuelingToDelete = null }) {
                        Text("Annulla", color = ComicBlack)
                    }
                },
                containerColor = Color.White,
                shape = RoundedCornerShape(12.dp)
            )
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
    onDateClick: () -> Unit,
    amountValue: String,
    onAmountClick: () -> Unit,
    companyValue: String? = null,
    onCompanyClick: (() -> Unit)? = null,
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
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = dateValue,
                        onValueChange = {},
                        label = { Text(stringResource(R.string.expiry_date)) },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
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
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { onDateClick() }
                    )
                }
                if (companyValue != null && onCompanyClick != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = companyValue,
                            onValueChange = {},
                            label = { Text("Compagnia Assicurativa") },
                            readOnly = true,
                            leadingIcon = {
                                if (companyValue.isNotBlank() && companyValue != "Altra Compagnia" && companyValue != "Altra / Personalizzata") {
                                    CompanyLogo(name = companyValue, modifier = Modifier.padding(start = 8.dp).size(24.dp))
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = ComicBlack,
                                unfocusedTextColor = ComicBlack,
                                focusedLabelColor = ComicBlack,
                                unfocusedLabelColor = ComicBlack,
                                cursorColor = ComicBlack,
                                focusedBorderColor = ComicBlack,
                                unfocusedBorderColor = ComicBlack
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { onCompanyClick() }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = amountValue,
                        onValueChange = {},
                        label = { Text(stringResource(R.string.amount)) },
                        readOnly = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = ComicBlack,
                            unfocusedTextColor = ComicBlack,
                            focusedLabelColor = ComicBlack,
                            unfocusedLabelColor = ComicBlack,
                            cursorColor = ComicBlack,
                            focusedBorderColor = ComicBlack,
                            unfocusedBorderColor = ComicBlack
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { onAmountClick() }
                    )
                }
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

@Composable
fun RevisioneDialog(
    title: String,
    dateValue: String,
    onDateClick: () -> Unit,
    isNewCar: Boolean,
    onIsNewCarChange: (Boolean) -> Unit,
    amountValue: String,
    onAmountClick: () -> Unit,
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
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = dateValue,
                        onValueChange = {},
                        label = { Text(stringResource(R.string.last_revision_date)) },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
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
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { onDateClick() }
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onIsNewCarChange(!isNewCar) }
                ) {
                    Checkbox(
                        checked = isNewCar,
                        onCheckedChange = onIsNewCarChange,
                        colors = CheckboxDefaults.colors(
                            checkedColor = ComicBlue,
                            uncheckedColor = ComicBlack,
                            checkmarkColor = ComicBlack
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.is_new_car),
                        color = ComicBlack,
                        fontSize = 14.sp
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = amountValue,
                        onValueChange = {},
                        label = { Text(stringResource(R.string.amount)) },
                        readOnly = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = ComicBlack,
                            unfocusedTextColor = ComicBlack,
                            focusedLabelColor = ComicBlack,
                            unfocusedLabelColor = ComicBlack,
                            cursorColor = ComicBlack,
                            focusedBorderColor = ComicBlack,
                            unfocusedBorderColor = ComicBlack
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { onAmountClick() }
                    )
                }
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
    val sdf = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
    return sdf.format(Date(millis))
}

fun parseDate(dateStr: String): Long? {
    return try {
        val sdf = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
        sdf.parse(dateStr)?.time
    } catch (e: Exception) {
        try {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            sdf.parse(dateStr)?.time
        } catch (e2: Exception) {
            null
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialogPicker(
    initialSelectedDateMillis: Long?,
    onDateSelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialSelectedDateMillis ?: System.currentTimeMillis()
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let {
                        onDateSelected(it)
                    }
                    onDismiss()
                }
            ) {
                Text("OK", color = Color(0xFF1E1E1E), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annulla", color = Color(0xFF1E1E1E))
            }
        },
        colors = DatePickerDefaults.colors(
            containerColor = Color.White
        )
    ) {
        DatePicker(
            state = datePickerState,
            colors = DatePickerDefaults.colors(
                containerColor = Color.White,
                titleContentColor = Color(0xFF1E1E1E),
                headlineContentColor = Color(0xFF1E1E1E),
                weekdayContentColor = Color(0xFF1E1E1E),
                subheadContentColor = Color(0xFF1E1E1E),
                navigationContentColor = Color(0xFF1E1E1E),
                yearContentColor = Color(0xFF1E1E1E),
                selectedYearContentColor = Color.White,
                selectedYearContainerColor = Color(0xFF3B82F6),
                dayContentColor = Color(0xFF1E1E1E),
                selectedDayContentColor = Color.White,
                selectedDayContainerColor = Color(0xFF3B82F6),
                todayContentColor = Color(0xFF3B82F6),
                todayDateBorderColor = Color(0xFF3B82F6)
            )
        )
    }
}

class OdometerTarget(
    val title: String,
    val initialValue: Int,
    val onSelected: (Int) -> Unit
)

@Composable
fun OdometerPickerDialog(
    initialValue: Int,
    title: String,
    onValueSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var tempValue by remember { mutableStateOf(initialValue) }
    AlertDialog(
        onDismissRequest = onDismiss,
        titleContentColor = ComicBlack,
        textContentColor = ComicBlack,
        title = { Text(title, fontWeight = FontWeight.Bold, color = ComicBlack) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                SpeedometerDial(value = tempValue)
                Spacer(modifier = Modifier.height(16.dp))
                ComicOdometerSelector(
                    value = tempValue,
                    onValueChange = { tempValue = it }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onValueSelected(tempValue)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = ComicBlue)
            ) {
                Text("Conferma", color = ComicBlack, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annulla", color = ComicBlack)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
fun SpeedometerDial(value: Int, maxVal: Int = 300000) {
    val progress = (value.toFloat() / maxVal.toFloat()).coerceIn(0f, 1f)
    val angle = 180f + progress * 180f
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.size(160.dp, 80.dp)) {
            // Draw arc border (thick comic style)
            drawArc(
                color = ComicBlack,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 6.dp.toPx())
            )
            
            // Draw filled arc progress
            drawArc(
                color = ComicBlue,
                startAngle = 180f,
                sweepAngle = progress * 180f,
                useCenter = false,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4.dp.toPx())
            )
            
            // Draw needle center pin
            val center = androidx.compose.ui.geometry.Offset(size.width / 2, size.height)
            drawCircle(
                color = ComicBlack,
                radius = 8.dp.toPx(),
                center = center
            )
            
            // Draw needle pointer
            val needleLength = 60.dp.toPx()
            val rad = Math.toRadians(angle.toDouble())
            val needleEnd = androidx.compose.ui.geometry.Offset(
                (center.x + needleLength * Math.cos(rad)).toFloat(),
                (center.y + needleLength * Math.sin(rad)).toFloat()
            )
            
            drawLine(
                color = ComicRed,
                start = center,
                end = needleEnd,
                strokeWidth = 4.dp.toPx(),
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        }
        
        Text(
            text = "$value km",
            fontWeight = FontWeight.Black,
            fontSize = 14.sp,
            color = ComicBlack,
            modifier = Modifier.padding(bottom = 4.dp)
        )
    }
}

@Composable
fun ComicOdometerSelector(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val digits = String.format(Locale.US, "%06d", value).map { it - '0' }
    
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until 6) {
            val weight = when (i) {
                0 -> 100000
                1 -> 10000
                2 -> 1000
                3 -> 100
                4 -> 10
                else -> 1
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 2.dp)
            ) {
                // Increment Button
                IconButton(
                    onClick = {
                        val currentDigit = digits[i]
                        val newVal = if (currentDigit < 9) value + weight else value - 9 * weight
                        onValueChange(newVal.coerceIn(0, 999999))
                    },
                    modifier = Modifier
                        .size(24.dp)
                        .border(2.dp, ComicBlack, RoundedCornerShape(4.dp))
                        .background(ComicYellow, RoundedCornerShape(4.dp))
                ) {
                    Text("+", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ComicBlack)
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Digit Box
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(36.dp)
                        .border(3.dp, ComicBlack, RoundedCornerShape(4.dp))
                        .background(Color.White, RoundedCornerShape(4.dp))
                ) {
                    Text(
                        text = digits[i].toString(),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = ComicBlack
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Decrement Button
                IconButton(
                    onClick = {
                        val currentDigit = digits[i]
                        val newVal = if (currentDigit > 0) value - weight else value + 9 * weight
                        onValueChange(newVal.coerceIn(0, 999999))
                    },
                    modifier = Modifier
                        .size(24.dp)
                        .border(2.dp, ComicBlack, RoundedCornerShape(4.dp))
                        .background(ComicYellow, RoundedCornerShape(4.dp))
                ) {
                    Text("-", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ComicBlack)
                }
            }
        }
    }
}

class MoneyTarget(
    val title: String,
    val initialValue: Double,
    val onSelected: (Double) -> Unit
)

@Composable
fun MoneyPickerDialog(
    initialValue: Double,
    title: String,
    onValueSelected: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    var tempValue by remember { mutableStateOf(initialValue) }
    var textInput by remember { mutableStateOf(String.format(Locale.US, "%.2f", initialValue)) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        titleContentColor = ComicBlack,
        textContentColor = ComicBlack,
        title = { Text(title, fontWeight = FontWeight.Bold, color = ComicBlack) },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Display Value
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .border(3.dp, ComicBlack, RoundedCornerShape(8.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(16.dp).fillMaxWidth()
                    ) {
                        Text(
                            text = String.format(Locale.US, "%.2f €", tempValue),
                            fontWeight = FontWeight.Black,
                            fontSize = 32.sp,
                            color = ComicBlack
                        )
                    }
                }
                
                // Banknotes Grid
                Text("Banconote:", fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start).padding(bottom = 4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    BanknoteButton(value = 5, color = Color(0xFFA5B4FC)) {
                        tempValue += 5.0
                        textInput = String.format(Locale.US, "%.2f", tempValue)
                    }
                    BanknoteButton(value = 10, color = Color(0xFFFCA5A5)) {
                        tempValue += 10.0
                        textInput = String.format(Locale.US, "%.2f", tempValue)
                    }
                    BanknoteButton(value = 20, color = Color(0xFF93C5FD)) {
                        tempValue += 20.0
                        textInput = String.format(Locale.US, "%.2f", tempValue)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    BanknoteButton(value = 50, color = Color(0xFFFDBA74)) {
                        tempValue += 50.0
                        textInput = String.format(Locale.US, "%.2f", tempValue)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    BanknoteButton(value = 100, color = Color(0xFF86EFAC)) {
                        tempValue += 100.0
                        textInput = String.format(Locale.US, "%.2f", tempValue)
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Coins Row
                Text("Monete:", fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start).padding(bottom = 4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CoinButton(label = "2€", value = 2.0) {
                        tempValue += 2.0
                        textInput = String.format(Locale.US, "%.2f", tempValue)
                    }
                    CoinButton(label = "1€", value = 1.0) {
                        tempValue += 1.0
                        textInput = String.format(Locale.US, "%.2f", tempValue)
                    }
                    CoinButton(label = "50c", value = 0.50) {
                        tempValue += 0.50
                        textInput = String.format(Locale.US, "%.2f", tempValue)
                    }
                    CoinButton(label = "10c", value = 0.10) {
                        tempValue += 0.10
                        textInput = String.format(Locale.US, "%.2f", tempValue)
                    }
                    CoinButton(label = "5c", value = 0.05) {
                        tempValue += 0.05
                        textInput = String.format(Locale.US, "%.2f", tempValue)
                    }
                    CoinButton(label = "1c", value = 0.01) {
                        tempValue += 0.01
                        textInput = String.format(Locale.US, "%.2f", tempValue)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Manual input override and Clear button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { newVal ->
                            textInput = newVal
                            newVal.replace(",", ".").toDoubleOrNull()?.let {
                                tempValue = it
                            }
                        },
                        label = { Text("Valore Manuale") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
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
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            tempValue = 0.0
                            textInput = "0.00"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ComicRed),
                        modifier = Modifier
                            .height(56.dp)
                            .border(3.dp, ComicBlack, RoundedCornerShape(8.dp)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Azzera", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onValueSelected(tempValue)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = ComicBlue)
            ) {
                Text("Conferma", color = ComicBlack, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annulla", color = ComicBlack)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
fun BanknoteButton(
    value: Int,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(90.dp)
            .height(55.dp)
            .padding(4.dp)
            .border(3.dp, ComicBlack, RoundedCornerShape(8.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = color),
        shape = RoundedCornerShape(8.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = "$value €",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp,
                color = ComicBlack
            )
        }
    }
}

@Composable
fun CoinButton(
    label: String,
    value: Double,
    onClick: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(45.dp)
            .padding(2.dp)
            .border(2.dp, ComicBlack, RoundedCornerShape(22.dp))
            .background(ComicYellow, RoundedCornerShape(22.dp))
            .clickable { onClick() }
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.Black,
            fontSize = 11.sp,
            color = ComicBlack
        )
    }
}

@Composable
fun CompanyLogo(name: String, modifier: Modifier = Modifier) {
    val (color, initials) = when (name) {
        "Generali" -> Color(0xFFC00000) to "G"
        "UnipolSai" -> Color(0xFF003366) to "U"
        "Allianz" -> Color(0xFF00377C) to "A"
        "AXA" -> Color(0xFF00008F) to "AXA"
        "Zurich" -> Color(0xFF003399) to "Z"
        "Prima" -> Color(0xFF7C3AED) to "P"
        "Linear" -> Color(0xFF008080) to "L"
        "Genertel" -> Color(0xFFF97316) to "G"
        "ConTe.it" -> Color(0xFF00B2A9) to "ConTe"
        else -> Color(0xFF6B7280) to "?"
    }
    
    val finalModifier = if (initials.length > 2) {
        modifier.width(42.dp)
    } else if (initials.length > 1) {
        modifier.width(30.dp)
    } else {
        modifier
    }
    
    Box(
        contentAlignment = Alignment.Center,
        modifier = finalModifier
            .background(color, RoundedCornerShape(4.dp))
            .border(2.dp, ComicBlack, RoundedCornerShape(4.dp))
            .padding(horizontal = 2.dp)
    ) {
        Text(
            text = initials,
            fontWeight = FontWeight.ExtraBold,
            fontSize = if (initials.length > 4) 8.sp else if (initials.length > 2) 9.sp else if (initials.length > 1) 10.sp else 12.sp,
            color = Color.White
        )
    }
}

@Composable
fun CompanyPickerDialog(
    onCompanySelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val companies = listOf(
        "Generali",
        "UnipolSai",
        "Allianz",
        "AXA",
        "Zurich",
        "Prima",
        "Linear",
        "Genertel",
        "ConTe.it"
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        titleContentColor = ComicBlack,
        textContentColor = ComicBlack,
        title = { Text("Seleziona Compagnia", fontWeight = FontWeight.Bold, color = ComicBlack) },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(companies) { company ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(2.dp, ComicBlack, RoundedCornerShape(8.dp))
                            .clickable {
                                onCompanySelected(company)
                                onDismiss()
                            },
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            CompanyLogo(name = company, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = company,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = ComicBlack
                            )
                        }
                    }
                }
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(2.dp, ComicBlack, RoundedCornerShape(8.dp))
                            .clickable {
                                onCompanySelected("Altra Compagnia")
                                onDismiss()
                            },
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(Color.LightGray, RoundedCornerShape(4.dp))
                                    .border(2.dp, ComicBlack, RoundedCornerShape(4.dp))
                            ) {
                                Text("?", fontWeight = FontWeight.Bold, color = ComicBlack)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Altra Compagnia",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = ComicBlack
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annulla", color = ComicBlack)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(12.dp)
    )
}

fun exportToCsv(context: android.content.Context, refuelings: List<Refueling>) {
    try {
        val sdfDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val sdfMonth = SimpleDateFormat("MMMM", Locale.getDefault())
        val sdfYear = SimpleDateFormat("yyyy", Locale.getDefault())
        
        // Grouping by year and month
        val grouped = refuelings.groupBy { refueling ->
            val date = Date(refueling.dateMillis)
            sdfYear.format(date) to sdfMonth.format(date)
        }
        
        val csvBuilder = java.lang.StringBuilder()
        // Prepend UTF-8 BOM to open correctly in Excel on Windows
        csvBuilder.append("\uFEFF")
        
        // Write Summary Section
        csvBuilder.append("SINTESI COSTI RAGGRUPPATI PER MESE E ANNO\n")
        csvBuilder.append("Anno;Mese;Spesa Totale (EUR);Litri Totali (L);Prezzo Medio (EUR/L);Numero Rifornimenti\n")
        
        grouped.forEach { (key, list) ->
            val (year, month) = key
            val totalSpent = list.sumOf { it.totalPrice }
            val totalLiters = list.sumOf { it.liters }
            val avgPrice = if (totalLiters > 0) totalSpent / totalLiters else 0.0
            val count = list.size
            csvBuilder.append(String.format(Locale.US, "%s;%s;%.2f;%.2f;%.3f;%d\n", year, month, totalSpent, totalLiters, avgPrice, count))
        }
        
        csvBuilder.append("\n\n")
        
        // Write Details Section
        csvBuilder.append("DETTAGLIO SINGOLI RIFORNIMENTI\n")
        csvBuilder.append("Data;Chilometri (km);Litri (L);Prezzo/Litro (EUR);Costo Totale (EUR)\n")
        
        refuelings.sortedByDescending { it.dateMillis }.forEach { refueling ->
            val dateStr = sdfDate.format(Date(refueling.dateMillis))
            csvBuilder.append(String.format(Locale.US, "%s;%d;%.2f;%.3f;%.2f\n", 
                dateStr, refueling.currentKm, refueling.liters, refueling.pricePerLiter, refueling.totalPrice))
        }
        
        val filename = "BenzinApp_Report_Costi.csv"
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            val contentResolver = context.contentResolver
            val contentValues = android.content.ContentValues().apply {
                put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "text/csv")
                put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_DOWNLOADS)
            }
            
            val uri = contentResolver.insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            if (uri != null) {
                contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(csvBuilder.toString().toByteArray(Charsets.UTF_8))
                }
                android.widget.Toast.makeText(context, "Report salvato in Download!", android.widget.Toast.LENGTH_LONG).show()
            } else {
                android.widget.Toast.makeText(context, "Errore durante il salvataggio", android.widget.Toast.LENGTH_LONG).show()
            }
        } else {
            val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) downloadsDir.mkdirs()
            val file = java.io.File(downloadsDir, filename)
            file.writeText(csvBuilder.toString(), Charsets.UTF_8)
            android.widget.Toast.makeText(context, "Report salvato in Download!", android.widget.Toast.LENGTH_LONG).show()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        android.widget.Toast.makeText(context, "Errore: ${e.localizedMessage}", android.widget.Toast.LENGTH_LONG).show()
    }
}
