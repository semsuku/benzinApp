package com.example.benzinapp

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
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
import com.example.benzinapp.ui.MainViewModel
import com.example.benzinapp.ui.screens.AddRefuelingScreen
import com.example.benzinapp.ui.screens.ChartsScreen
import com.example.benzinapp.ui.screens.HomeScreen
import com.example.benzinapp.ui.theme.BenzinAppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    // Action per scattare la foto
    private val takePicturePreview = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
        if (bitmap != null) {
            processImageWithGemini(bitmap)
        } else {
            Toast.makeText(this, "Nessuna foto scattata", Toast.LENGTH_SHORT).show()
        }
    }

    // Action per scegliere dalla galleria
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
            }
        } else {
            Toast.makeText(this, "Nessuna foto selezionata", Toast.LENGTH_SHORT).show()
        }
    }

    private var onGeminiSuccess: ((Double?, Double?) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BenzinAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val viewModel: MainViewModel = viewModel()

                    // Variabili di stato per la navigazione dopo lo scatto
                    var initialLiters by remember { mutableStateOf<Double?>(null) }
                    var initialTotal by remember { mutableStateOf<Double?>(null) }

                    onGeminiSuccess = { liters, total ->
                        initialLiters = liters
                        initialTotal = total
                        navController.navigate("add")
                    }

                    NavHost(navController = navController, startDestination = "home") {
                        composable("home") {
                            HomeScreen(
                                viewModel = viewModel,
                                onNavigateToAdd = {
                                    initialLiters = null
                                    initialTotal = null
                                    navController.navigate("add")
                                },
                                onNavigateToCamera = {
                                    takePicturePreview.launch(null)
                                },
                                onNavigateToGallery = {
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
                                onNavigateBack = { navController.popBackStack() }
                            )
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
            val result = GeminiHelper.extractDataFromImage(bitmap)
            withContext(Dispatchers.Main) {
                if (result != null) {
                    Toast.makeText(this@MainActivity, "Dati estratti con successo!", Toast.LENGTH_SHORT).show()
                    onGeminiSuccess?.invoke(result.liters, result.totalPrice)
                } else {
                    Toast.makeText(this@MainActivity, "Errore nell'estrazione dei dati. Reinserisci manualmente.", Toast.LENGTH_LONG).show()
                    onGeminiSuccess?.invoke(null, null)
                }
            }
        }
    }
}