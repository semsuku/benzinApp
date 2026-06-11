package com.example.benzinapp.gemini

import android.graphics.Bitmap
import com.example.benzinapp.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

data class ExtractedRefuelingInfo(
    val liters: Double?,
    val totalPrice: Double?
)

object GeminiHelper {
    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    suspend fun extractDataFromImage(bitmap: Bitmap): ExtractedRefuelingInfo? = withContext(Dispatchers.IO) {
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
                return@withContext ExtractedRefuelingInfo(liters, totalPrice)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext null
    }

    suspend fun extractOdometerFromImage(bitmap: Bitmap): Int? = withContext(Dispatchers.IO) {
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
                if (jsonObject.has("km") && !jsonObject.isNull("km")) {
                    return@withContext jsonObject.getInt("km")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext null
    }
}

