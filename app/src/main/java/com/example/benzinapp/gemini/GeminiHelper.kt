package com.example.benzinapp.gemini

import android.graphics.Bitmap
import com.example.benzinapp.data.api.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream

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

    /**
     * Extracts refueling liters and total price from an image by sending it to the FastAPI backend proxy.
     */
    suspend fun extractDataFromImage(bitmap: Bitmap): GeminiResult<ExtractedRefuelingInfo> = withContext(Dispatchers.IO) {
        try {
            val bos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bos)
            val bytes = bos.toByteArray()
            val requestFile = bytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", "receipt.jpg", requestFile)

            val response = RetrofitClient.apiService.extractData(taskType = "refueling", file = body)
            return@withContext GeminiResult.Success(ExtractedRefuelingInfo(response.liters, response.totalPrice))
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext GeminiResult.ApiError(e.localizedMessage)
        }
    }

    /**
     * Extracts total odometer mileage from an image by sending it to the FastAPI backend proxy.
     */
    suspend fun extractOdometerFromImage(bitmap: Bitmap): GeminiResult<Int?> = withContext(Dispatchers.IO) {
        try {
            val bos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bos)
            val bytes = bos.toByteArray()
            val requestFile = bytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", "odometer.jpg", requestFile)

            val response = RetrofitClient.apiService.extractData(taskType = "odometer", file = body)
            return@withContext GeminiResult.Success(response.km)
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext GeminiResult.ApiError(e.localizedMessage)
        }
    }
}
