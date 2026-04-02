package com.example.benzinapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "refuelings")
data class Refueling(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateMillis: Long,          // Timestamp del rifornimento
    val pricePerLiter: Double,     // Prezzo al litro
    val liters: Double,            // Litri di carburante erogati
    val totalPrice: Double,        // Costo totale
    val currentKm: Int,            // Chilometraggio attuale del veicolo
    val kmDrivenSinceLast: Int     // Chilometri percorsi dall'ultimo rifornimento
)
