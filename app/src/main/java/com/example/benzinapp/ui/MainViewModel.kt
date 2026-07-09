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

    private val _tireChangeDate = MutableStateFlow<Long?>(
        if (sharedPrefs.contains("tire_change_date")) sharedPrefs.getLong("tire_change_date", 0) else null
    )
    val tireChangeDate: StateFlow<Long?> = _tireChangeDate.asStateFlow()

    private val _tireChangeKm = MutableStateFlow<Int?>(
        if (sharedPrefs.contains("tire_change_km")) sharedPrefs.getInt("tire_change_km", 0) else null
    )
    val tireChangeKm: StateFlow<Int?> = _tireChangeKm.asStateFlow()

    private val _oilChangeKm = MutableStateFlow<Int?>(
        if (sharedPrefs.contains("oil_change_km")) sharedPrefs.getInt("oil_change_km", 0) else null
    )
    val oilChangeKm: StateFlow<Int?> = _oilChangeKm.asStateFlow()

    private val _oilLastChangeDate = MutableStateFlow<Long?>(
        if (sharedPrefs.contains("oil_last_date")) sharedPrefs.getLong("oil_last_date", 0) else null
    )
    val oilLastChangeDate: StateFlow<Long?> = _oilLastChangeDate.asStateFlow()

    private val _oilLastChangeKm = MutableStateFlow<Int?>(
        if (sharedPrefs.contains("oil_last_km")) sharedPrefs.getInt("oil_last_km", 0) else null
    )
    val oilLastChangeKm: StateFlow<Int?> = _oilLastChangeKm.asStateFlow()

    private val _oilIntervalMonths = MutableStateFlow(
        sharedPrefs.getInt("oil_interval_months", 12)
    )
    val oilIntervalMonths: StateFlow<Int> = _oilIntervalMonths.asStateFlow()

    private val _oilIntervalKm = MutableStateFlow(
        sharedPrefs.getInt("oil_interval_km", 30000)
    )
    val oilIntervalKm: StateFlow<Int> = _oilIntervalKm.asStateFlow()

    private val _oilNextDate = MutableStateFlow<Long?>(
        if (sharedPrefs.contains("oil_next_date")) sharedPrefs.getLong("oil_next_date", 0) else null
    )
    val oilNextDate: StateFlow<Long?> = _oilNextDate.asStateFlow()

    private val _insuranceExpiryDate = MutableStateFlow<Long?>(
        if (sharedPrefs.contains("insurance_expiry_date")) sharedPrefs.getLong("insurance_expiry_date", 0) else null
    )
    val insuranceExpiryDate: StateFlow<Long?> = _insuranceExpiryDate.asStateFlow()

    private val _insuranceAmount = MutableStateFlow(sharedPrefs.getFloat("insurance_amount", 0f).toDouble())
    val insuranceAmount: StateFlow<Double> = _insuranceAmount.asStateFlow()

    private val _insuranceCompany = MutableStateFlow(
        sharedPrefs.getString("insurance_company", "") ?: ""
    )
    val insuranceCompany: StateFlow<String> = _insuranceCompany.asStateFlow()

    private val _bolloExpiryDate = MutableStateFlow<Long?>(
        if (sharedPrefs.contains("bollo_expiry_date")) sharedPrefs.getLong("bollo_expiry_date", 0) else null
    )
    val bolloExpiryDate: StateFlow<Long?> = _bolloExpiryDate.asStateFlow()

    private val _bolloAmount = MutableStateFlow(sharedPrefs.getFloat("bollo_amount", 0f).toDouble())
    val bolloAmount: StateFlow<Double> = _bolloAmount.asStateFlow()

    private val _revisioneExpiryDate = MutableStateFlow<Long?>(
        if (sharedPrefs.contains("revisione_expiry_date")) sharedPrefs.getLong("revisione_expiry_date", 0) else null
    )
    val revisioneExpiryDate: StateFlow<Long?> = _revisioneExpiryDate.asStateFlow()

    private val _revisioneLastDate = MutableStateFlow<Long?>(
        if (sharedPrefs.contains("revisione_last_date")) sharedPrefs.getLong("revisione_last_date", 0) else null
    )
    val revisioneLastDate: StateFlow<Long?> = _revisioneLastDate.asStateFlow()

    private val _revisioneIsNew = MutableStateFlow(sharedPrefs.getBoolean("revisione_is_new", false))
    val revisioneIsNew: StateFlow<Boolean> = _revisioneIsNew.asStateFlow()

    private val _revisioneAmount = MutableStateFlow(sharedPrefs.getFloat("revisione_amount", 0f).toDouble())
    val revisioneAmount: StateFlow<Double> = _revisioneAmount.asStateFlow()

    init {
        createNotificationChannel(application)
        checkAllDeadlines()
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

    fun updateTireRotation(changeDate: Long?, changeKm: Int?, nextRotationKm: Int?) {
        val editor = sharedPrefs.edit()
        
        if (changeDate != null) {
            editor.putLong("tire_change_date", changeDate)
            _tireChangeDate.value = changeDate
        } else {
            editor.remove("tire_change_date")
            _tireChangeDate.value = null
        }

        if (changeKm != null) {
            editor.putInt("tire_change_km", changeKm)
            _tireChangeKm.value = changeKm
        } else {
            editor.remove("tire_change_km")
            _tireChangeKm.value = null
        }

        if (nextRotationKm != null) {
            editor.putInt("tire_rotation_km", nextRotationKm)
            _tireRotationKm.value = nextRotationKm
        } else {
            editor.remove("tire_rotation_km")
            _tireRotationKm.value = null
        }
        
        editor.apply()
    }

    fun updateOilChange(lastDate: Long?, lastKm: Int?, intervalMonths: Int, intervalKm: Int) {
        val editor = sharedPrefs.edit()
        
        if (lastDate != null) {
            editor.putLong("oil_last_date", lastDate)
            _oilLastChangeDate.value = lastDate
            
            val calendar = java.util.Calendar.getInstance().apply {
                timeInMillis = lastDate
            }
            calendar.add(java.util.Calendar.MONTH, intervalMonths)
            val nextDate = calendar.timeInMillis
            editor.putLong("oil_next_date", nextDate)
            _oilNextDate.value = nextDate
        } else {
            editor.remove("oil_last_date")
            editor.remove("oil_next_date")
            _oilLastChangeDate.value = null
            _oilNextDate.value = null
        }
        
        if (lastKm != null) {
            editor.putInt("oil_last_km", lastKm)
            _oilLastChangeKm.value = lastKm
            
            val nextKm = lastKm + intervalKm
            editor.putInt("oil_change_km", nextKm)
            _oilChangeKm.value = nextKm
        } else {
            editor.remove("oil_last_km")
            editor.remove("oil_change_km")
            _oilLastChangeKm.value = null
            _oilChangeKm.value = null
        }
        
        editor.putInt("oil_interval_months", intervalMonths)
        _oilIntervalMonths.value = intervalMonths
        
        editor.putInt("oil_interval_km", intervalKm)
        _oilIntervalKm.value = intervalKm
        
        editor.apply()
        checkOilDeadline()
    }

    fun updateInsurance(expiry: Long?, amount: Double, company: String) {
        val editor = sharedPrefs.edit()
        if (expiry != null) editor.putLong("insurance_expiry_date", expiry)
        else editor.remove("insurance_expiry_date")
        editor.putFloat("insurance_amount", amount.toFloat())
        editor.putString("insurance_company", company)
        editor.apply()
        _insuranceExpiryDate.value = expiry
        _insuranceAmount.value = amount
        _insuranceCompany.value = company
        checkInsuranceDeadline()
    }

    fun updateBollo(expiry: Long?, amount: Double) {
        val editor = sharedPrefs.edit()
        if (expiry != null) editor.putLong("bollo_expiry_date", expiry)
        else editor.remove("bollo_expiry_date")
        editor.putFloat("bollo_amount", amount.toFloat())
        editor.apply()
        _bolloExpiryDate.value = expiry
        _bolloAmount.value = amount
        checkBolloDeadline()
    }

    fun updateRevisione(lastDate: Long?, isNew: Boolean, amount: Double) {
        val editor = sharedPrefs.edit()
        val expiry = if (lastDate != null) {
            val calendar = java.util.Calendar.getInstance().apply {
                timeInMillis = lastDate
            }
            if (isNew) {
                calendar.add(java.util.Calendar.YEAR, 4)
            } else {
                calendar.add(java.util.Calendar.YEAR, 2)
            }
            calendar.timeInMillis
        } else {
            null
        }

        if (lastDate != null) editor.putLong("revisione_last_date", lastDate)
        else editor.remove("revisione_last_date")

        if (expiry != null) editor.putLong("revisione_expiry_date", expiry)
        else editor.remove("revisione_expiry_date")

        editor.putBoolean("revisione_is_new", isNew)
        editor.putFloat("revisione_amount", amount.toFloat())
        editor.apply()

        _revisioneLastDate.value = lastDate
        _revisioneExpiryDate.value = expiry
        _revisioneIsNew.value = isNew
        _revisioneAmount.value = amount

        checkRevisioneDeadline()
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

            val oilThreshold = _oilChangeKm.value
            if (oilThreshold != null && currentKm >= oilThreshold) {
                sendOilChangeNotification()
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

    private fun sendOilChangeNotification() {
        val context = getApplication<Application>()
        try {
            val builder = NotificationCompat.Builder(context, "maintenance_channel")
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle("Cambio Olio Necessario!")
                .setContentText("Hai raggiunto i km impostati per il cambio dell'olio.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)

            with(NotificationManagerCompat.from(context)) {
                notify(1002, builder.build())
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

    fun checkAllDeadlines() {
        checkInsuranceDeadline()
        checkBolloDeadline()
        checkRevisioneDeadline()
        checkOilDeadline()
    }

    private fun checkOilDeadline() {
        val nextDate = _oilNextDate.value ?: return
        val daysToExpiry = (nextDate - System.currentTimeMillis()) / (24 * 60 * 60 * 1000)
        if (daysToExpiry <= 30) {
            val dateStr = formatDate(nextDate)
            val title = if (daysToExpiry < 0) "Tagliando Scaduto!" else "Tagliando in Scadenza!"
            val text = if (daysToExpiry < 0) "Il tagliando auto è scaduto il $dateStr (limite tempo)!" else "Il tagliando auto scade il $dateStr (limite tempo)."
            sendNotification(1002, title, text)
        }
    }

    private fun checkInsuranceDeadline() {
        val expiry = _insuranceExpiryDate.value ?: return
        val daysToExpiry = (expiry - System.currentTimeMillis()) / (24 * 60 * 60 * 1000)
        if (daysToExpiry <= 30) {
            val dateStr = formatDate(expiry)
            val title = if (daysToExpiry < 0) "Assicurazione Scaduta!" else "Assicurazione in Scadenza!"
            val text = if (daysToExpiry < 0) "La tua assicurazione è scaduta il $dateStr!" else "La tua assicurazione scade il $dateStr."
            sendNotification(1003, title, text)
        }
    }

    private fun checkBolloDeadline() {
        val expiry = _bolloExpiryDate.value ?: return
        val daysToExpiry = (expiry - System.currentTimeMillis()) / (24 * 60 * 60 * 1000)
        if (daysToExpiry <= 30) {
            val dateStr = formatDate(expiry)
            val title = if (daysToExpiry < 0) "Bollo Scaduto!" else "Bollo in Scadenza!"
            val text = if (daysToExpiry < 0) "Il tuo bollo auto è scaduto il $dateStr!" else "Il tuo bollo auto scade il $dateStr."
            sendNotification(1004, title, text)
        }
    }

    private fun checkRevisioneDeadline() {
        val expiry = _revisioneExpiryDate.value ?: return
        val daysToExpiry = (expiry - System.currentTimeMillis()) / (24 * 60 * 60 * 1000)
        if (daysToExpiry <= 30) {
            val dateStr = formatDate(expiry)
            val title = if (daysToExpiry < 0) "Revisione Scaduta!" else "Revisione in Scadenza!"
            val text = if (daysToExpiry < 0) "La revisione dell'auto è scaduta il $dateStr!" else "La revisione dell'auto scade il $dateStr."
            sendNotification(1005, title, text)
        }
    }

    private fun sendNotification(id: Int, title: String, text: String) {
        val context = getApplication<Application>()
        try {
            val builder = NotificationCompat.Builder(context, "maintenance_channel")
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)

            with(NotificationManagerCompat.from(context)) {
                notify(id, builder.build())
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun formatDate(millis: Long): String {
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(millis))
    }
}
