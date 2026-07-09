package com.example.benzinapp.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.benzinapp.data.model.Station
import com.example.benzinapp.data.repository.FuelRepository
import com.example.benzinapp.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NearestStationsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { FuelRepository() }
    val scope = rememberCoroutineScope()

    var fuelType by remember { mutableStateOf("Benzina") }
    var radiusKm by remember { mutableStateOf(20) }
    var isLoading by remember { mutableStateOf(false) }
    var stationsList by remember { mutableStateOf<List<Station>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Coordinate di default (Bientina, PI)
    var latitude by remember { mutableStateOf(43.7117) }
    var longitude by remember { mutableStateOf(10.6309) }
    var isLocationDetected by remember { mutableStateOf(false) }

    // Launcher per richiedere i permessi di localizzazione
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineGranted || coarseGranted) {
            detectLocation(context) { lat, lon ->
                latitude = lat
                longitude = lon
                isLocationDetected = true
                loadStations(repository, lat, lon, fuelType, radiusKm, scope, { stations ->
                    stationsList = stations
                    errorMessage = null
                    isLoading = false
                }, { err ->
                    errorMessage = err
                    isLoading = false
                }) { isLoading = it }
            }
        } else {
            Toast.makeText(context, "Permesso di posizione negato. Uso posizione predefinita.", Toast.LENGTH_LONG).show()
        }
    }

    // Carica dati al primo avvio
    LaunchedEffect(Unit) {
        val finePermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarsePermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        
        if (finePermission == PackageManager.PERMISSION_GRANTED || coarsePermission == PackageManager.PERMISSION_GRANTED) {
            detectLocation(context) { lat, lon ->
                latitude = lat
                longitude = lon
                isLocationDetected = true
            }
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // Ricarica quando cambia il carburante o il raggio
    LaunchedEffect(fuelType, radiusKm, latitude, longitude) {
        loadStations(repository, latitude, longitude, fuelType, radiusKm, scope, { stations ->
            stationsList = stations
            errorMessage = null
        }, { err ->
            errorMessage = err
        }) { isLoading = it }
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
                        "DISTRIBUTORI VICINI",
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Indietro", tint = ComicBlack)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val finePermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                        if (finePermission == PackageManager.PERMISSION_GRANTED) {
                            detectLocation(context) { lat, lon ->
                                latitude = lat
                                longitude = lon
                                isLocationDetected = true
                                Toast.makeText(context, "Posizione aggiornata!", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            locationPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                    }) {
                        Icon(Icons.Default.MyLocation, contentDescription = "Aggiorna GPS", tint = ComicBlack)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // Stato Posizione GPS
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .border(3.dp, ComicBlack, RoundedCornerShape(12.dp))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = null,
                        tint = if (isLocationDetected) ComicGreen else ComicRed,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (isLocationDetected) "Posizione GPS Rilevata" else "Posizione Predefinita (Bientina)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = ComicBlack
                        )
                        Text(
                            text = String.format("Lat: %.4f | Lon: %.4f", latitude, longitude),
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            // Selezione Carburante
            Text(
                "TIPO CARBURANTE",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 12.sp,
                color = ComicBlack,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            val fuels = listOf("Benzina", "Gasolio", "GPL", "Metano")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                fuels.forEach { fuel ->
                    val isSelected = fuelType == fuel
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 2.dp)
                            .height(40.dp)
                            .background(
                                color = if (isSelected) ComicYellow else Color.White,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .border(3.dp, ComicBlack, RoundedCornerShape(8.dp))
                            .clickable { fuelType = fuel }
                    ) {
                        Text(
                            text = fuel.uppercase(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = ComicBlack
                        )
                    }
                }
            }

            // Selezione Raggio (Slider)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "DISTANZA MASSIMA: ${radiusKm} KM",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp,
                    color = ComicBlack
                )
            }
            Slider(
                value = radiusKm.toFloat(),
                onValueChange = { radiusKm = it.toInt() },
                valueRange = 5f..50f,
                steps = 9,
                colors = SliderDefaults.colors(
                    thumbColor = ComicBlue,
                    activeTrackColor = ComicBlack,
                    inactiveTrackColor = Color.LightGray
                ),
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .border(3.dp, ComicBlack, RoundedCornerShape(8.dp))
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp)
            )

            // Lista distributori o stato di caricamento/errore
            if (isLoading && stationsList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = ComicBlack)
                }
            } else if (errorMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = errorMessage ?: "Errore imprevisto",
                            fontWeight = FontWeight.Bold,
                            color = ComicRed,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Button(
                            onClick = {
                                loadStations(repository, latitude, longitude, fuelType, radiusKm, scope, { stations ->
                                    stationsList = stations
                                    errorMessage = null
                                }, { err ->
                                    errorMessage = err
                                }) { isLoading = it }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ComicBlue),
                            modifier = Modifier.border(3.dp, ComicBlack, RoundedCornerShape(8.dp))
                        ) {
                            Text("RIPROVA", color = ComicBlack, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else if (stationsList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nessun distributore trovato nel raggio selezionato.",
                        fontWeight = FontWeight.Bold,
                        color = ComicBlack
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(stationsList) { station ->
                        StationCard(station = station)
                    }
                }
            }
        }
    }
}

@Composable
fun StationCard(station: Station) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(3.dp, ComicBlack, RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Box Prezzo (stile Neo-brutalism)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(70.dp)
                    .background(ComicYellow, RoundedCornerShape(8.dp))
                    .border(3.dp, ComicBlack, RoundedCornerShape(8.dp))
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = String.format(java.util.Locale.US, "%.3f", station.price),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = ComicBlack
                    )
                    Text(
                        text = "€/L",
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = ComicBlack
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Dettagli Distributore
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = station.brand.uppercase(),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    color = ComicBlack
                )
                Text(
                    text = station.address,
                    fontSize = 11.sp,
                    color = Color.DarkGray
                )
                Text(
                    text = "${station.city.uppercase()} (${station.province.uppercase()})",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Badge Modalità (Self/Servito)
                    val modeText = if (station.isSelf) "SELF" else "SERVITO"
                    val modeColor = if (station.isSelf) ComicGreen else ComicBlue
                    Box(
                        modifier = Modifier
                            .background(modeColor, RoundedCornerShape(4.dp))
                            .border(1.dp, ComicBlack, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = modeText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 8.sp,
                            color = ComicBlack
                        )
                    }
                    
                    // Comunicazione data
                    Text(
                        text = "Agg: ${station.dtComu.split(" ")[0]}",
                        fontSize = 9.sp,
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Distanza
            Column(horizontalAlignment = Alignment.End) {
                Icon(
                    imageVector = Icons.Default.LocalGasStation,
                    contentDescription = null,
                    tint = ComicBlack,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = String.format(java.util.Locale.US, "%.1f km", station.distanceKm),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = ComicBlack
                )
            }
        }
    }
}

private fun detectLocation(context: Context, onLocationResult: (Double, Double) -> Unit) {
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        return
    }
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    try {
        val providers = locationManager.getProviders(true)
        var bestLocation: Location? = null
        for (provider in providers) {
            val l = locationManager.getLastKnownLocation(provider) ?: continue
            if (bestLocation == null || l.accuracy < bestLocation.accuracy) {
                bestLocation = l
            }
        }
        bestLocation?.let {
            onLocationResult(it.latitude, it.longitude)
        }
    } catch (e: SecurityException) {
        e.printStackTrace()
    }
}

private fun loadStations(
    repository: FuelRepository,
    lat: Double,
    lon: Double,
    fuelType: String,
    radiusKm: Int,
    scope: kotlinx.coroutines.CoroutineScope,
    onSuccess: (List<Station>) -> Unit,
    onFailure: (String) -> Unit,
    onLoading: (Boolean) -> Unit
) {
    onLoading(true)
    scope.launch {
        repository.getNearestStations(lat, lon, fuelType, radiusKm).collect { result ->
            result.onSuccess { stations ->
                onSuccess(stations)
                onLoading(false)
            }.onFailure { exception ->
                onFailure("Impossibile connettersi al server del Raspberry Pi.")
                onLoading(false)
            }
        }
    }
}
