package com.example.benzinapp.data.repository

import com.example.benzinapp.data.api.RetrofitClient
import com.example.benzinapp.data.model.Station
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class FuelRepository {
    private val apiService = RetrofitClient.apiService

    /**
     * Fetches nearest and cheapest fuel stations from the backend as a Flow.
     */
    fun getNearestStations(
        lat: Double,
        lon: Double,
        fuelType: String,
        radiusKm: Int
    ): Flow<Result<List<Station>>> = flow {
        try {
            val stations = apiService.getNearestStations(lat, lon, fuelType, radiusKm)
            emit(Result.success(stations))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)
}
