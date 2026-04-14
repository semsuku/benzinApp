package com.example.benzinapp.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.benzinapp.data.AppDatabase
import com.example.benzinapp.data.Refueling
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).refuelingDao()

    val refuelings: StateFlow<List<Refueling>> = dao.getAllRefuelings()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addRefueling(
        dateMillis: Long,
        pricePerLiter: Double,
        liters: Double,
        totalPrice: Double,
        currentKm: Int
    ) {
        viewModelScope.launch {
            val lastRefueling = dao.getLastRefueling()
            val kmDriven = if (lastRefueling != null && currentKm >= lastRefueling.currentKm) {
                currentKm - lastRefueling.currentKm
            } else {
                0
            }

            val newRefueling = Refueling(
                dateMillis = dateMillis,
                pricePerLiter = pricePerLiter,
                liters = liters,
                totalPrice = totalPrice,
                currentKm = currentKm,
                kmDrivenSinceLast = kmDriven
            )
            dao.insertRefueling(newRefueling)
        }
    }

    fun deleteRefueling(refueling: Refueling) {
        viewModelScope.launch {
            dao.deleteRefueling(refueling)
        }
    }
}
