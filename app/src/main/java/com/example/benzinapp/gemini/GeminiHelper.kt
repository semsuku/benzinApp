package com.example.benzinapp.gemini

import android.graphics.Bitmap
import com.example.benzinapp.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * Data class representing the result of refueling data extraction.
 */
data class ExtractedRefuelingInfo(
    val liters: Double?,
    val totalPrice: Double?
)

/**
 * Sealed interface representing the outcome of Gemini API operations.
 */
sealed interface GeminiResult<out T> {
    data class Success<out T>(val data: T) : GeminiResult<T>
    object ParsingError : GeminiResult<Nothing>
    data class ApiError(val message: String?) : GeminiResult<Nothing>
}

object GeminiHelper {
    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    /**
     * Extracts refueling liters and total price from an image.
     */
    suspend fun extractDataFromImage(bitmap: Bitmap): GeminiResult<ExtractedRefuelingInfo> = withContext(Dispatchers.IO) {
        try {
            val prompt = """
                Analizza questa immagine di un display di una pompa di benzina o scontrino di rifornimento.
                Estrai e restituisci il risultato **esclusivamente** in formato JSON valido, senza testo aggiuntivo (niente markdown, niente backticks), con le seguenti chiavi numeriche:
                - "liters" (float, litri erogati, es. 20.50)
                - "totalPrice" (float, costo totale in valuta locale, es. 40.00)
                
                Se un valore non è leggibile, metti null. Assicurati che i decimali usino il punto.
            """.trimIndent()

            val response = generativeModel.generateContent(
                content {
                    image(bitmap)
                    text(prompt)
                }
            )
            
            val responseText = response.text?.trim()
                ?.removePrefix("```json")
                ?.removeSuffix("```")
                ?.trim()
            
            if (!responseText.isNullOrEmpty()) {
                val jsonObject = JSONObject(responseText)
                val liters = if (jsonObject.has("liters") && !jsonObject.isNull("liters")) jsonObject.getDouble("liters") else null
                val totalPrice = if (jsonObject.has("totalPrice") && !jsonObject.isNull("totalPrice")) jsonObject.getDouble("totalPrice") else null
                return@withContext GeminiResult.Success(ExtractedRefuelingInfo(liters, totalPrice))
            }
            return@withContext GeminiResult.ParsingError
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext GeminiResult.ApiError(e.localizedMessage)
        }
    }

    /**
     * Extracts total odometer mileage from an image.
     */
    suspend fun extractOdometerFromImage(bitmap: Bitmap): GeminiResult<Int?> = withContext(Dispatchers.IO) {
        try {
            val prompt = """
                Analizza questa immagine del contachilometri di una macchina.
                Estrai e restituisci il numero totale di chilometri percorsi (odometro/chilometraggio totale) **esclusivamente** in formato JSON valido, senza testo aggiuntivo (niente markdown, niente backticks), con la seguente chiave:
                - "km" (integer, chilometri totali, es. 124500)
                
                Se il valore non è leggibile o non è presente, metti null.
            """.trimIndent()

            val response = generativeModel.generateContent(
                content {
                    image(bitmap)
                    text(prompt)
                }
            )
            
            val responseText = response.text?.trim()
                ?.removePrefix("```json")
                ?.removeSuffix("```")
                ?.trim()
            
            if (!responseText.isNullOrEmpty()) {
                val jsonObject = JSONObject(responseText)
                val km = if (jsonObject.has("km") && !jsonObject.isNull("km")) jsonObject.getInt("km") else null
                return@withContext GeminiResult.Success(km)
            }
            return@withContext GeminiResult.ParsingError
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext GeminiResult.ApiError(e.localizedMessage)
        }
    }
}
