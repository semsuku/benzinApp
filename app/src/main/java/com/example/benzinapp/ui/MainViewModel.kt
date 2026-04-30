package com.example.benzinapp.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.benzinapp.data.AppDatabase
import com.example.benzinapp.data.Refueling
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).refuelingDao()

    private val sharedPrefs = application.getSharedPreferences("benzin_prefs", Context.MODE_PRIVATE)
    
    private val _tireRotationKm = MutableStateFlow<Int?>(
        if (sharedPrefs.contains("tire_rotation_km")) sharedPrefs.getInt("tire_rotation_km", 0) else null
    )
    val tireRotationKm: StateFlow<Int?> = _tireRotationKm.asStateFlow()

    private val _insuranceExpiryDate = MutableStateFlow<Long?>(
        if (sharedPrefs.contains("insurance_expiry_date")) sharedPrefs.getLong("insurance_expiry_date", 0) else null
    )
    val insuranceExpiryDate: StateFlow<Long?> = _insuranceExpiryDate.asStateFlow()

    private val _insuranceAmount = MutableStateFlow(sharedPrefs.getFloat("insurance_amount", 0f).toDouble())
    val insuranceAmount: StateFlow<Double> = _insuranceAmount.asStateFlow()

    private val _bolloExpiryDate = MutableStateFlow<Long?>(
        if (sharedPrefs.contains("bollo_expiry_date")) sharedPrefs.getLong("bollo_expiry_date", 0) else null
    )
    val bolloExpiryDate: StateFlow<Long?> = _bolloExpiryDate.asStateFlow()

    private val _bolloAmount = MutableStateFlow(sharedPrefs.getFloat("bollo_amount", 0f).toDouble())
    val bolloAmount: StateFlow<Double> = _bolloAmount.asStateFlow()

    init {
        createNotificationChannel(application)
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Avvisi Manutenzione"
            val descriptionText = "Notifiche per inversione gomme e manutenzione"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("maintenance_channel", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun updateTireRotationKm(km: Int) {
        sharedPrefs.edit().putInt("tire_rotation_km", km).apply()
        _tireRotationKm.value = km
    }

    fun updateInsurance(expiry: Long?, amount: Double) {
        val editor = sharedPrefs.edit()
        if (expiry != null) editor.putLong("insurance_expiry_date", expiry)
        else editor.remove("insurance_expiry_date")
        editor.putFloat("insurance_amount", amount.toFloat())
        editor.apply()
        _insuranceExpiryDate.value = expiry
        _insuranceAmount.value = amount
    }

    fun updateBollo(expiry: Long?, amount: Double) {
        val editor = sharedPrefs.edit()
        if (expiry != null) editor.putLong("bollo_expiry_date", expiry)
        else editor.remove("bollo_expiry_date")
        editor.putFloat("bollo_amount", amount.toFloat())
        editor.apply()
        _bolloExpiryDate.value = expiry
        _bolloAmount.value = amount
    }

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

            val rotationThreshold = _tireRotationKm.value
            if (rotationThreshold != null && currentKm >= rotationThreshold) {
                sendTireRotationNotification()
            }
        }
    }

    private fun sendTireRotationNotification() {
        val context = getApplication<Application>()
        try {
            val builder = NotificationCompat.Builder(context, "maintenance_channel")
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle("Inversione Gomme Necessaria!")
                .setContentText("Hai raggiunto i km impostati per l'inversione gomme.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)

            with(NotificationManagerCompat.from(context)) {
                notify(1001, builder.build())
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    fun updateRefueling(
        id: Long,
        dateMillis: Long,
        pricePerLiter: Double,
        liters: Double,
        totalPrice: Double,
        currentKm: Int
    ) {
        viewModelScope.launch {
            val prevRefueling = dao.getPreviousRefueling(dateMillis)
            val kmDriven = if (prevRefueling != null && currentKm >= prevRefueling.currentKm) {
                currentKm - prevRefueling.currentKm
            } else {
                0
            }

            val updatedRefueling = Refueling(
                id = id,
                dateMillis = dateMillis,
                pricePerLiter = pricePerLiter,
                liters = liters,
                totalPrice = totalPrice,
                currentKm = currentKm,
                kmDrivenSinceLast = kmDriven
            )
            dao.updateRefueling(updatedRefueling)
        }
    }

    fun deleteRefueling(refueling: Refueling) {
        viewModelScope.launch {
            dao.deleteRefueling(refueling)
        }
    }
}
