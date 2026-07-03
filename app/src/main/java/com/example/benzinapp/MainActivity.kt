package com.example.benzinapp

import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.benzinapp.gemini.GeminiHelper
import com.example.benzinapp.gemini.GeminiResult
import com.example.benzinapp.ui.MainViewModel
import com.example.benzinapp.ui.screens.AddRefuelingScreen
import com.example.benzinapp.ui.screens.ChartsScreen
import com.example.benzinapp.ui.screens.HomeScreen
import com.example.benzinapp.ui.theme.BenzinAppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {

    enum class ImageProcessMode {
        FUEL_PUMP,
        ODOMETER
    }

    private var currentMode = ImageProcessMode.FUEL_PUMP
    private var tempLiters: Double? = null
    private var tempTotalPrice: Double? = null
    private var showQuotaDialog by mutableStateOf(false)

    private var onFuelPumpExtracted: (() -> Unit)? = null
    private var onOdometerExtracted: ((Int?) -> Unit)? = null

    private val takePicturePreview = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
        if (bitmap != null) {
            processImageWithGemini(bitmap)
        } else {
            if (currentMode == ImageProcessMode.FUEL_PUMP) {
                Toast.makeText(this, "Nessuna foto scattata", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Rilevamento contachilometri annullato", Toast.LENGTH_SHORT).show()
                onOdometerExtracted?.invoke(null)
            }
        }
    }

    private val pickImageFromGallery = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            try {
                val bitmap = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    val source = android.graphics.ImageDecoder.createSource(contentResolver, uri)
                    android.graphics.ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                        decoder.allocator = android.graphics.ImageDecoder.ALLOCATOR_SOFTWARE
                        decoder.isMutableRequired = true
                    }
                } else {
                    @Suppress("DEPRECATION")
                    android.provider.MediaStore.Images.Media.getBitmap(contentResolver, uri)
                }
                processImageWithGemini(bitmap)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Errore nel caricamento dell'immagine", Toast.LENGTH_SHORT).show()
                if (currentMode == ImageProcessMode.ODOMETER) {
                    onOdometerExtracted?.invoke(null)
                }
            }
        } else {
            if (currentMode == ImageProcessMode.FUEL_PUMP) {
                Toast.makeText(this, "Nessuna foto selezionata", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Rilevamento contachilometri annullato", Toast.LENGTH_SHORT).show()
                onOdometerExtracted?.invoke(null)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize AdMob Mobile Ads SDK
        // com.google.android.gms.ads.MobileAds.initialize(this) {}

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            val requestPermissionLauncher = registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (!isGranted) {
                    Toast.makeText(this, "Permesso notifiche negato", Toast.LENGTH_SHORT).show()
                }
            }
            requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            BenzinAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (showQuotaDialog) {
                        AlertDialog(
                            onDismissRequest = { showQuotaDialog = false },
                            title = { Text("Servizio non disponibile") },
                            text = { Text("Al momento il servizio di riconoscimento automatico non è disponibile a causa del raggiungimento del limite di utilizzi gratuiti. Riprova più tardi o inserisci i dati manualmente.") },
                            confirmButton = {
                                TextButton(onClick = { showQuotaDialog = false }) {
                                    Text("OK")
                                }
                            }
                        )
                    }

                    val navController = rememberNavController()
                    val viewModel: MainViewModel = viewModel()

                    var initialLiters by remember { mutableStateOf<Double?>(null) }
                    var initialTotal by remember { mutableStateOf<Double?>(null) }
                    var initialKm by remember { mutableStateOf<Int?>(null) }

                    var showOdometerPrompt by remember { mutableStateOf(false) }

                    onFuelPumpExtracted = {
                        initialLiters = tempLiters
                        initialTotal = tempTotalPrice
                        initialKm = null
                        showOdometerPrompt = true
                    }

                    onOdometerExtracted = { km ->
                        initialKm = km
                        navController.navigate("add")
                    }

                    if (showOdometerPrompt) {
                        AlertDialog(
                            onDismissRequest = {
                                showOdometerPrompt = false
                                navController.navigate("add")
                            },
                            title = { Text("Aggiungi Contachilometri") },
                            text = { Text("Vuoi scattare o selezionare anche una foto del contachilometri per impostare automaticamente i km?") },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        showOdometerPrompt = false
                                        currentMode = ImageProcessMode.ODOMETER
                                        takePicturePreview.launch(null)
                                    }
                                ) {
                                    Text("Scatta Foto")
                                }
                            },
                            dismissButton = {
                                Row {
                                    TextButton(
                                        onClick = {
                                            showOdometerPrompt = false
                                            currentMode = ImageProcessMode.ODOMETER
                                            pickImageFromGallery.launch("image/*")
                                        }
                                    ) {
                                        Text("Scegli da Galleria")
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    TextButton(
                                        onClick = {
                                            showOdometerPrompt = false
                                            navController.navigate("add")
                                        }
                                    ) {
                                        Text("No, grazie")
                                    }
                                }
                            }
                        )
                    }

                    NavHost(navController = navController, startDestination = "home") {
                        composable("home") {
                            HomeScreen(
                                viewModel = viewModel,
                                onNavigateToAdd = {
                                    initialLiters = null
                                    initialTotal = null
                                    initialKm = null
                                    navController.navigate("add")
                                },
                                onNavigateToEdit = { refueling ->
                                    navController.navigate("edit/${refueling.id}")
                                },
                                onNavigateToCamera = {
                                    currentMode = ImageProcessMode.FUEL_PUMP
                                    takePicturePreview.launch(null)
                                },
                                onNavigateToGallery = {
                                    currentMode = ImageProcessMode.FUEL_PUMP
                                    pickImageFromGallery.launch("image/*")
                                },
                                onNavigateToCharts = {
                                    navController.navigate("charts")
                                }
                            )
                        }
                        composable("add") {
                            AddRefuelingScreen(
                                viewModel = viewModel,
                                initialLiters = initialLiters,
                                initialTotalPrice = initialTotal,
                                initialKm = initialKm,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable(
                            route = "edit/{refuelingId}",
                            arguments = listOf(navArgument("refuelingId") { type = NavType.LongType })
                        ) { backStackEntry ->
                            val refuelingId = backStackEntry.arguments?.getLong("refuelingId") ?: -1L
                            val refueling = viewModel.refuelings.collectAsState().value.find { it.id == refuelingId }
                            
                            if (refueling != null) {
                                AddRefuelingScreen(
                                    viewModel = viewModel,
                                    refuelingToEdit = refueling,
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }
                        }
                        composable("charts") {
                            ChartsScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun processImageWithGemini(bitmap: Bitmap) {
        Toast.makeText(this, "Analisi immagine in corso...", Toast.LENGTH_LONG).show()
        CoroutineScope(Dispatchers.IO).launch {
            if (currentMode == ImageProcessMode.FUEL_PUMP) {
                val result = GeminiHelper.extractDataFromImage(bitmap)
                withContext(Dispatchers.Main) {
                    when (result) {
                        is GeminiResult.Success -> {
                            Toast.makeText(this@MainActivity, "Dati pompa estratti!", Toast.LENGTH_SHORT).show()
                            tempLiters = result.data.liters
                            tempTotalPrice = result.data.totalPrice
                        }
                        is GeminiResult.ApiError -> {
                            showQuotaDialog = true
                            tempLiters = null
                            tempTotalPrice = null
                        }
                        is GeminiResult.ParsingError -> {
                            Toast.makeText(this@MainActivity, "Errore nell'estrazione dati pompa. Inserisci manualmente.", Toast.LENGTH_LONG).show()
                            tempLiters = null
                            tempTotalPrice = null
                        }
                    }
                    onFuelPumpExtracted?.invoke()
                }
            } else {
                val result = GeminiHelper.extractOdometerFromImage(bitmap)
                withContext(Dispatchers.Main) {
                    when (result) {
                        is GeminiResult.Success -> {
                            val km = result.data
                            if (km != null) {
                                Toast.makeText(this@MainActivity, "Contachilometri rilevato: $km km", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this@MainActivity, "Errore nella lettura dei km. Inserisci manualmente.", Toast.LENGTH_LONG).show()
                            }
                            onOdometerExtracted?.invoke(km)
                        }
                        is GeminiResult.ApiError -> {
                            showQuotaDialog = true
                            onOdometerExtracted?.invoke(null)
                        }
                        is GeminiResult.ParsingError -> {
                            Toast.makeText(this@MainActivity, "Errore nella lettura dei km. Inserisci manualmente.", Toast.LENGTH_LONG).show()
                            onOdometerExtracted?.invoke(null)
                        }
                    }
                }
            }
        }
    }
}