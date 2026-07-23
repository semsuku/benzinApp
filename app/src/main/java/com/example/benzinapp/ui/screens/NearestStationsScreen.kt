package com.example.benzinapp.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.webkit.WebView
import android.webkit.WebViewClient
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
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
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
    
    var hasLocationPermission by remember { mutableStateOf(false) }
    var isMapViewSelected by remember { mutableStateOf(false) }

    // Launcher per richiedere i permessi di localizzazione
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineGranted || coarseGranted) {
            hasLocationPermission = true
        } else {
            Toast.makeText(context, "Permesso di posizione negato. Uso posizione predefinita.", Toast.LENGTH_LONG).show()
        }
    }

    // Carica dati al primo avvio
    LaunchedEffect(Unit) {
        val finePermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarsePermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        
        if (finePermission == PackageManager.PERMISSION_GRANTED || coarsePermission == PackageManager.PERMISSION_GRANTED) {
            hasLocationPermission = true
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // Gestione degli aggiornamenti GPS continui (real-time in macchina)
    DisposableEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val locationListener = object : android.location.LocationListener {
                override fun onLocationChanged(location: Location) {
                    latitude = location.latitude
                    longitude = location.longitude
                    isLocationDetected = true
                }
                @Deprecated("Deprecated in Java")
                override fun onStatusChanged(provider: String?, status: Int, extras: android.os.Bundle?) {}
                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {}
            }

            try {
                // Primo rilevamento immediato con Last Known Location
                val providers = locationManager.getProviders(true)
                var bestLocation: Location? = null
                for (provider in providers) {
                    val l = locationManager.getLastKnownLocation(provider) ?: continue
                    if (bestLocation == null || l.accuracy < bestLocation.accuracy) {
                        bestLocation = l
                    }
                }
                bestLocation?.let {
                    latitude = it.latitude
                    longitude = it.longitude
                    isLocationDetected = true
                }

                // Richiedi aggiornamenti continui dal GPS (e Network come fallback)
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        3000L, // 3 secondi
                        5f,    // 5 metri
                        locationListener
                    )
                }
                if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        3000L,
                        5f,
                        locationListener
                    )
                }
            } catch (e: SecurityException) {
                e.printStackTrace()
            }

            onDispose {
                try {
                    locationManager.removeUpdates(locationListener)
                } catch (e: SecurityException) {
                    e.printStackTrace()
                }
            }
        } else {
            onDispose {}
        }
    }

    // Ricarica quando cambia il carburante o il raggio o la posizione
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
                        if (hasLocationPermission) {
                            Toast.makeText(context, "Ricerca posizione GPS...", Toast.LENGTH_SHORT).show()
                            forceLocationRefresh(context) { lat, lon ->
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
                    .clickable { openMapIntent(context, latitude, longitude, "La mia posizione") }
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
                            text = if (isLocationDetected) "Posizione GPS Rilevata (Clicca per Mappa)" else "Posizione Predefinita (Bientina)",
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

            // Toggle Visualizzazione LISTA / MAPPA
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { isMapViewSelected = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!isMapViewSelected) ComicYellow else Color.White
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .border(3.dp, ComicBlack, RoundedCornerShape(8.dp)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("LISTA", color = ComicBlack, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { isMapViewSelected = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isMapViewSelected) ComicYellow else Color.White
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .border(3.dp, ComicBlack, RoundedCornerShape(8.dp)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("MAPPA", color = ComicBlack, fontWeight = FontWeight.Bold)
                }
            }

            // Contenuto: Lista distributori o Mappa o caricamento/errore
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
                if (isMapViewSelected) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .border(3.dp, ComicBlack, RoundedCornerShape(12.dp))
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .padding(3.dp)
                    ) {
                        MapWebView(
                            userLat = latitude,
                            userLon = longitude,
                            stations = stationsList
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
}

@Composable
fun StationCard(station: Station) {
    val context = LocalContext.current
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(3.dp, ComicBlack, RoundedCornerShape(12.dp))
            .clickable { openMapIntent(context, station.latitude, station.longitude, station.brand) }
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

            // Distanza & Bottone Navigazione
            Column(horizontalAlignment = Alignment.End) {
                IconButton(
                    onClick = { openNavigationIntent(context, station.latitude, station.longitude, station.brand) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Navigation,
                        contentDescription = "Naviga",
                        tint = ComicBlue,
                        modifier = Modifier.size(24.dp)
                    )
                }
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

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun MapWebView(
    userLat: Double,
    userLon: Double,
    stations: List<Station>
) {
    val context = LocalContext.current
    
    // Formatta coordinate con punto decimale per evitare problemi di localizzazione in Javascript
    val latStr = remember(userLat) { String.format(java.util.Locale.US, "%.6f", userLat) }
    val lonStr = remember(userLon) { String.format(java.util.Locale.US, "%.6f", userLon) }

    // Serializza la lista delle stazioni in JSON (Gson rispetta @SerializedName)
    val stationsJson = remember(stations) {
        com.google.gson.Gson().toJson(stations)
    }

    // IMPORTANTE: remember SOLO su stationsJson, NON su latStr/lonStr.
    // Gli aggiornamenti GPS ogni 3 secondi causerebbero il reload della pagina,
    // interrompendo il download di Leaflet.js dal CDN prima che finisca.
    // La mappa si ricarica solo quando cambiano le stazioni (tipo carburante, raggio, ecc.)
    val htmlContent = remember(stationsJson) {
        """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
            <link rel="stylesheet" href="file:///android_asset/leaflet/leaflet.css" />
            <style>
                html, body {
                    width: 100vw;
                    height: 100vh;
                    margin: 0;
                    padding: 0;
                    overflow: hidden;
                    background: #E3F2FD;
                }
                #map {
                    width: 100vw;
                    height: 100vh;
                    position: absolute;
                    top: 0; left: 0;
                    z-index: 1;
                }

                .custom-popup .leaflet-popup-content-wrapper {
                    background: #FFFFFF;
                    color: #212121;
                    border: 3px solid #212121;
                    border-radius: 8px;
                    font-family: sans-serif;
                    box-shadow: 3px 3px 0px #212121;
                }
                .custom-popup .leaflet-popup-tip {
                    border: 3px solid #212121;
                    background: #FFFFFF;
                }
                .popup-title {
                    font-weight: 800;
                    font-size: 13px;
                    margin: 0 0 4px 0;
                    text-transform: uppercase;
                }
                .popup-price {
                    font-weight: 800;
                    font-size: 15px;
                    color: #E28413;
                    margin: 4px 0;
                }
                .popup-button {
                    display: block;
                    background: #81D4FA;
                    color: #212121 !important;
                    text-decoration: none;
                    padding: 6px;
                    font-weight: bold;
                    font-size: 10px;
                    border: 2px solid #212121;
                    border-radius: 4px;
                    box-shadow: 2px 2px 0px #212121;
                    margin-top: 6px;
                    text-align: center;
                    text-transform: uppercase;
                }
            </style>
        </head>
        <body>
            <div id="map"></div>
            <script src="file:///android_asset/leaflet/leaflet.js"
                    onerror="console.error('ERROR: FALLITO CARICAMENTO LEAFLET LOCALE'); alert('Leaflet locale fallito');"></script>
            <script>
                window.onload = function() {
                    if (typeof L === 'undefined') {
                        console.error("ERROR: Leaflet 'L' undefined");
                        alert("Errore L undefined");
                        return;
                    }
                    try {
                        var map = L.map('map', { zoomControl: false }).setView([$latStr, $lonStr], 13);
                        
                        L.control.zoom({ position: 'topright' }).addTo(map);

                        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                            maxZoom: 19,
                            attribution: '&copy; OSM'
                        }).addTo(map);

                        // Marker Posizione Utente
                        var userIcon = L.divIcon({
                            className: 'user-marker',
                            html: '<div style="background: #00E676; width: 14px; height: 14px; border-radius: 50%; border: 3px solid white; box-shadow: 0 0 8px rgba(0,230,118,0.8);"></div>',
                            iconSize: [20, 20],
                            iconAnchor: [10, 10]
                        });
                        L.marker([$latStr, $lonStr], {icon: userIcon}).addTo(map)
                            .bindPopup("<div class='popup-title'>La tua posizione</div>");

                        // Marker Distributori e auto-zoom
                        var bounds = L.latLngBounds([ [$latStr, $lonStr] ]);
                        var stations = $stationsJson;
                        stations.forEach(function(station) {
                            var iconColor = station.is_self ? '#00E676' : '#81D4FA';
                            var markerIcon = L.divIcon({
                                className: 'station-marker',
                                html: '<div style="background: ' + iconColor + '; width: 12px; height: 12px; border-radius: 50%; border: 2.5px solid #212121; box-shadow: 2px 2px 0px #212121;"></div>',
                                iconSize: [16, 16],
                                iconAnchor: [8, 8]
                            });

                            var priceStr = (station.price != null) ? station.price.toFixed(3) : 'N/D';
                            var distStr = (station.distance_km != null) ? station.distance_km.toFixed(1) : 'N/D';
                            var brandStr = station.brand || 'Sconosciuto';
                            var addrStr = station.address || '';

                            var popupContent = "<div class='popup-title'>" + brandStr + "</div>" +
                                               "<div style='font-size: 10px; color: #4B5563;'>" + addrStr + "</div>" +
                                               "<div class='popup-price'>" + priceStr + " \u20ac/L (" + (station.is_self ? 'Self' : 'Servito') + ")</div>" +
                                               "<div style='font-size: 9px; color: #6B7280; margin-bottom: 5px;'>" + distStr + " km</div>" +
                                               "<a href='naviga://" + station.latitude + "," + station.longitude + "," + encodeURIComponent(brandStr) + "' class='popup-button'>Naviga</a>";

                            L.marker([station.latitude, station.longitude], {icon: markerIcon})
                                .addTo(map)
                                .bindPopup(popupContent, {className: 'custom-popup'});
                                
                            bounds.extend([station.latitude, station.longitude]);
                        });

                        // Adatta lo zoom in base ai marker
                        if (stations.length > 0) {
                            map.fitBounds(bounds, {padding: [40, 40]});
                        }

                        setTimeout(function() {
                            map.invalidateSize();
                        }, 300);

                        // Ascolta eventuali ridimensionamenti della WebView causati da Compose
                        window.addEventListener('resize', function() {
                            map.invalidateSize();
                        });

                    } catch (e) {
                        console.error("CATCH ERROR: " + e.message);
                        alert('Eccezione JS: ' + e.message);
                    }
                };
            </script>
        </body>
        </html>
        """.trimIndent()
    }

    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                layoutParams = android.view.ViewGroup.LayoutParams(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT
                )
                
                // Tag per tracciare l'hash dell'ultimo HTML caricato ed evitare reload inutili
                tag = ""

                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.allowContentAccess = true
                settings.allowFileAccess = true
                settings.allowFileAccessFromFileURLs = true
                settings.allowUniversalAccessFromFileURLs = true
                settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                
                // Cattura i messaggi della console JS per diagnosi facilitata in logcat e mostra alert nativi
                webChromeClient = object : android.webkit.WebChromeClient() {
                    override fun onConsoleMessage(consoleMessage: android.webkit.ConsoleMessage?): Boolean {
                        consoleMessage?.let {
                            android.util.Log.d("MapWebView", "${it.message()} -- line ${it.lineNumber()} of ${it.sourceId()}")
                        }
                        return true
                    }
                    
                    override fun onJsAlert(
                        view: WebView?,
                        url: String?,
                        message: String?,
                        result: android.webkit.JsResult?
                    ): Boolean {
                        android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_LONG).show()
                        result?.confirm()
                        return true
                    }
                }

                webViewClient = object : WebViewClient() {
                    @Deprecated("Deprecated in Java")
                    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                        if (url != null && url.startsWith("naviga://")) {
                            val data = url.substring("naviga://".length)
                            val parts = data.split(",")
                            if (parts.size >= 3) {
                                val lat = parts[0].toDoubleOrNull() ?: 0.0
                                val lon = parts[1].toDoubleOrNull() ?: 0.0
                                val brand = Uri.decode(parts[2])
                                openNavigationIntent(context, lat, lon, brand)
                            }
                            return true
                        }
                        return false
                    }
                }
            }
        },
        update = { webView ->
            // Ricarica solo se il contenuto HTML è effettivamente cambiato.
            // Usa l'hashCode del contenuto come fingerprint per evitare reload
            // continui causati dalle ricomposizioni GPS ogni 3 secondi.
            val contentHash = htmlContent.hashCode().toString()
            val lastHash = webView.tag as? String ?: ""
            if (lastHash != contentHash) {
                webView.tag = contentHash
                webView.loadDataWithBaseURL("file:///android_asset/", htmlContent, "text/html", "UTF-8", null)
            }
        },
        modifier = Modifier.fillMaxSize()
    )
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

private fun forceLocationRefresh(context: Context, onLocationDetected: (Double, Double) -> Unit) {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        return
    }
    
    try {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            locationManager.getCurrentLocation(
                LocationManager.GPS_PROVIDER,
                null,
                context.mainExecutor
            ) { location ->
                if (location != null) {
                    onLocationDetected(location.latitude, location.longitude)
                } else {
                    detectLocation(context, onLocationDetected)
                }
            }
        } else {
            val providers = locationManager.getProviders(true)
            for (provider in providers) {
                locationManager.requestLocationUpdates(
                    provider,
                    0L,
                    0f,
                    object : android.location.LocationListener {
                        override fun onLocationChanged(loc: Location) {
                            onLocationDetected(loc.latitude, loc.longitude)
                            locationManager.removeUpdates(this)
                        }
                        @Deprecated("Deprecated in Java")
                        override fun onStatusChanged(p: String?, s: Int, e: android.os.Bundle?) {}
                        override fun onProviderEnabled(p: String) {}
                        override fun onProviderDisabled(p: String) {}
                    },
                    context.mainLooper
                )
            }
        }
    } catch (e: SecurityException) {
        e.printStackTrace()
    }
}

private fun openMapIntent(context: Context, lat: Double, lon: Double, label: String) {
    try {
        val uri = Uri.parse("geo:0,0?q=$lat,$lon(${Uri.encode(label)})")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.google.android.apps.maps")
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$lat,$lon")
        val webIntent = Intent(Intent.ACTION_VIEW, webUri)
        context.startActivity(webIntent)
    }
}

private fun openNavigationIntent(context: Context, lat: Double, lon: Double, label: String) {
    try {
        val uri = Uri.parse("google.navigation:q=$lat,$lon")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.google.android.apps.maps")
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        val webUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$lat,$lon")
        val webIntent = Intent(Intent.ACTION_VIEW, webUri)
        context.startActivity(webIntent)
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

