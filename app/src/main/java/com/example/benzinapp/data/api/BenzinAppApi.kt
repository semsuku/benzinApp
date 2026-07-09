package com.example.benzinapp.data.api

import com.example.benzinapp.data.model.Station
import okhttp3.MultipartBody
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

data class GeminiExtractionResponse(
    val liters: Double? = null,
    val totalPrice: Double? = null,
    val km: Int? = null
)

interface BenzinAppApi {
    @GET("api/v1/stations/nearest")
    suspend fun getNearestStations(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("fuel_type") fuelType: String,
        @Query("radius_km") radiusKm: Int
    ): List<Station>

    @Multipart
    @POST("api/v1/gemini/extract")
    suspend fun extractData(
        @Query("task_type") taskType: String,
        @Part file: MultipartBody.Part
    ): GeminiExtractionResponse
}
