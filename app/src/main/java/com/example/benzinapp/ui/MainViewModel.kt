package com.example.benzinapp.ui

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.benzinapp.data.AppDatabase
import com.example.benzinapp.data.Profile
import com.example.benzinapp.data.Refueling
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val refuelingDao = database.refuelingDao()
    private val profileDao = database.profileDao()

    private val sharedPrefs = application.getSharedPreferences("benzin_prefs", Context.MODE_PRIVATE)

    // --- PROFILES STATE ---
    val profiles: StateFlow<List<Profile>> = profileDao.getAllProfiles()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _activeProfileId = MutableStateFlow(sharedPrefs.getLong("active_profile_id", 1L))
    val activeProfileId: StateFlow<Long> = _activeProfileId.asStateFlow()

    val activeProfile: StateFlow<Profile?> = combine(profiles, activeProfileId) { profileList, activeId ->
        profileList.find { it.id == activeId } ?: profileList.firstOrNull()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    // --- REFUELINGS STATE (Filtered by Active Profile) ---
    val refuelings: StateFlow<List<Refueling>> = _activeProfileId.flatMapLatest { profileId ->
        refuelingDao.getRefuelingsForProfile(profileId)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // --- MAINTENANCE / DEADLINES STATE FOR ACTIVE PROFILE ---
    private val _tireRotationKm = MutableStateFlow<Int?>(null)
    val tireRotationKm: StateFlow<Int?> = _tireRotationKm.asStateFlow()

    private val _tireChangeDate = MutableStateFlow<Long?>(null)
    val tireChangeDate: StateFlow<Long?> = _tireChangeDate.asStateFlow()

    private val _tireChangeKm = MutableStateFlow<Int?>(null)
    val tireChangeKm: StateFlow<Int?> = _tireChangeKm.asStateFlow()

    private val _oilChangeKm = MutableStateFlow<Int?>(null)
    val oilChangeKm: StateFlow<Int?> = _oilChangeKm.asStateFlow()

    private val _oilLastChangeDate = MutableStateFlow<Long?>(null)
    val oilLastChangeDate: StateFlow<Long?> = _oilLastChangeDate.asStateFlow()

    private val _oilLastChangeKm = MutableStateFlow<Int?>(null)
    val oilLastChangeKm: StateFlow<Int?> = _oilLastChangeKm.asStateFlow()

    private val _oilIntervalMonths = MutableStateFlow(12)
    val oilIntervalMonths: StateFlow<Int> = _oilIntervalMonths.asStateFlow()

    private val _oilIntervalKm = MutableStateFlow(30000)
    val oilIntervalKm: StateFlow<Int> = _oilIntervalKm.asStateFlow()

    private val _oilNextDate = MutableStateFlow<Long?>(null)
    val oilNextDate: StateFlow<Long?> = _oilNextDate.asStateFlow()

    private val _insuranceExpiryDate = MutableStateFlow<Long?>(null)
    val insuranceExpiryDate: StateFlow<Long?> = _insuranceExpiryDate.asStateFlow()

    private val _insuranceAmount = MutableStateFlow(0.0)
    val insuranceAmount: StateFlow<Double> = _insuranceAmount.asStateFlow()

    private val _insuranceCompany = MutableStateFlow("")
    val insuranceCompany: StateFlow<String> = _insuranceCompany.asStateFlow()

    private val _bolloExpiryDate = MutableStateFlow<Long?>(null)
    val bolloExpiryDate: StateFlow<Long?> = _bolloExpiryDate.asStateFlow()

    private val _bolloAmount = MutableStateFlow(0.0)
    val bolloAmount: StateFlow<Double> = _bolloAmount.asStateFlow()

    private val _revisioneExpiryDate = MutableStateFlow<Long?>(null)
    val revisioneExpiryDate: StateFlow<Long?> = _revisioneExpiryDate.asStateFlow()

    private val _revisioneLastDate = MutableStateFlow<Long?>(null)
    val revisioneLastDate: StateFlow<Long?> = _revisioneLastDate.asStateFlow()

    private val _revisioneIsNew = MutableStateFlow(false)
    val revisioneIsNew: StateFlow<Boolean> = _revisioneIsNew.asStateFlow()

    private val _revisioneAmount = MutableStateFlow(0.0)
    val revisioneAmount: StateFlow<Double> = _revisioneAmount.asStateFlow()

    init {
        createNotificationChannel(application)
        
        viewModelScope.launch {
            // Ensure default profile exists
            if (profileDao.getProfileCount() == 0) {
                profileDao.insertProfile(Profile(id = 1L, name = "Io", vehicleName = "Auto Principale", iconName = "car", colorHex = "#1E88E5"))
            }
            
            // Migrate legacy single-profile maintenance prefs if present
            migrateLegacyPrefsIfNeeded()

            // Observe activeProfileId changes to update maintenance state
            _activeProfileId.collect { profileId ->
                loadProfileMaintenance(profileId)
            }
        }

        checkAllDeadlines()
    }

    private fun migrateLegacyPrefsIfNeeded() {
        val legacyKeys = listOf("tire_rotation_km", "insurance_expiry_date", "bollo_expiry_date", "oil_last_date")
        val hasLegacy = legacyKeys.any { sharedPrefs.contains(it) }
        if (hasLegacy) {
            val editor = sharedPrefs.edit()
            fun copyInt(key: String) { if (sharedPrefs.contains(key)) editor.putInt("p_1_$key", sharedPrefs.getInt(key, 0)) }
            fun copyLong(key: String) { if (sharedPrefs.contains(key)) editor.putLong("p_1_$key", sharedPrefs.getLong(key, 0)) }
            fun copyFloat(key: String) { if (sharedPrefs.contains(key)) editor.putFloat("p_1_$key", sharedPrefs.getFloat(key, 0f)) }
            fun copyString(key: String) { if (sharedPrefs.contains(key)) editor.putString("p_1_$key", sharedPrefs.getString(key, "")) }
            fun copyBool(key: String) { if (sharedPrefs.contains(key)) editor.putBoolean("p_1_$key", sharedPrefs.getBoolean(key, false)) }

            copyInt("tire_rotation_km"); copyLong("tire_change_date"); copyInt("tire_change_km")
            copyInt("oil_change_km"); copyLong("oil_last_date"); copyInt("oil_last_km")
            copyInt("oil_interval_months"); copyInt("oil_interval_km"); copyLong("oil_next_date")
            copyLong("insurance_expiry_date"); copyFloat("insurance_amount"); copyString("insurance_company")
            copyLong("bollo_expiry_date"); copyFloat("bollo_amount")
            copyLong("revisione_expiry_date"); copyLong("revisione_last_date"); copyBool("revisione_is_new"); copyFloat("revisione_amount")

            legacyKeys.forEach { editor.remove(it) }
            editor.apply()
        }
    }

    private fun prefKey(profileId: Long, key: String): String = "p_${profileId}_$key"

    private fun loadProfileMaintenance(profileId: Long) {
        _tireRotationKm.value = if (sharedPrefs.contains(prefKey(profileId, "tire_rotation_km"))) sharedPrefs.getInt(prefKey(profileId, "tire_rotation_km"), 0) else null
        _tireChangeDate.value = if (sharedPrefs.contains(prefKey(profileId, "tire_change_date"))) sharedPrefs.getLong(prefKey(profileId, "tire_change_date"), 0) else null
        _tireChangeKm.value = if (sharedPrefs.contains(prefKey(profileId, "tire_change_km"))) sharedPrefs.getInt(prefKey(profileId, "tire_change_km"), 0) else null
        
        _oilChangeKm.value = if (sharedPrefs.contains(prefKey(profileId, "oil_change_km"))) sharedPrefs.getInt(prefKey(profileId, "oil_change_km"), 0) else null
        _oilLastChangeDate.value = if (sharedPrefs.contains(prefKey(profileId, "oil_last_date"))) sharedPrefs.getLong(prefKey(profileId, "oil_last_date"), 0) else null
        _oilLastChangeKm.value = if (sharedPrefs.contains(prefKey(profileId, "oil_last_km"))) sharedPrefs.getInt(prefKey(profileId, "oil_last_km"), 0) else null
        _oilIntervalMonths.value = sharedPrefs.getInt(prefKey(profileId, "oil_interval_months"), 12)
        _oilIntervalKm.value = sharedPrefs.getInt(prefKey(profileId, "oil_interval_km"), 30000)
        _oilNextDate.value = if (sharedPrefs.contains(prefKey(profileId, "oil_next_date"))) sharedPrefs.getLong(prefKey(profileId, "oil_next_date"), 0) else null

        _insuranceExpiryDate.value = if (sharedPrefs.contains(prefKey(profileId, "insurance_expiry_date"))) sharedPrefs.getLong(prefKey(profileId, "insurance_expiry_date"), 0) else null
        _insuranceAmount.value = sharedPrefs.getFloat(prefKey(profileId, "insurance_amount"), 0f).toDouble()
        _insuranceCompany.value = sharedPrefs.getString(prefKey(profileId, "insurance_company"), "") ?: ""

        _bolloExpiryDate.value = if (sharedPrefs.contains(prefKey(profileId, "bollo_expiry_date"))) sharedPrefs.getLong(prefKey(profileId, "bollo_expiry_date"), 0) else null
        _bolloAmount.value = sharedPrefs.getFloat(prefKey(profileId, "bollo_amount"), 0f).toDouble()

        _revisioneExpiryDate.value = if (sharedPrefs.contains(prefKey(profileId, "revisione_expiry_date"))) sharedPrefs.getLong(prefKey(profileId, "revisione_expiry_date"), 0) else null
        _revisioneLastDate.value = if (sharedPrefs.contains(prefKey(profileId, "revisione_last_date"))) sharedPrefs.getLong(prefKey(profileId, "revisione_last_date"), 0) else null
        _revisioneIsNew.value = sharedPrefs.getBoolean(prefKey(profileId, "revisione_is_new"), false)
        _revisioneAmount.value = sharedPrefs.getFloat(prefKey(profileId, "revisione_amount"), 0f).toDouble()
    }

    // --- PROFILE OPERATIONS ---
    fun setActiveProfile(profileId: Long) {
        _activeProfileId.value = profileId
        sharedPrefs.edit().putLong("active_profile_id", profileId).apply()
    }

    fun addProfile(name: String, vehicleName: String, iconName: String, colorHex: String) {
        viewModelScope.launch {
            val count = profileDao.getProfileCount()
            if (count < 4) {
                val newId = profileDao.insertProfile(
                    Profile(name = name, vehicleName = vehicleName, iconName = iconName, colorHex = colorHex)
                )
                setActiveProfile(newId)
            }
        }
    }

    fun updateProfile(profile: Profile) {
        viewModelScope.launch {
            profileDao.updateProfile(profile)
        }
    }

    fun deleteProfile(profile: Profile) {
        viewModelScope.launch {
            if (profiles.value.size > 1) {
                refuelingDao.deleteRefuelingsForProfile(profile.id)
                profileDao.deleteProfile(profile)

                if (_activeProfileId.value == profile.id) {
                    val remaining = profiles.value.filter { it.id != profile.id }
                    remaining.firstOrNull()?.let { setActiveProfile(it.id) }
                }
            }
        }
    }

    // --- MAINTENANCE UPDATES FOR ACTIVE PROFILE ---
    fun updateTireRotation(changeDate: Long?, changeKm: Int?, nextRotationKm: Int?) {
        val pId = activeProfileId.value
        val editor = sharedPrefs.edit()
        
        if (changeDate != null) {
            editor.putLong(prefKey(pId, "tire_change_date"), changeDate)
            _tireChangeDate.value = changeDate
        } else {
            editor.remove(prefKey(pId, "tire_change_date"))
            _tireChangeDate.value = null
        }

        if (changeKm != null) {
            editor.putInt(prefKey(pId, "tire_change_km"), changeKm)
            _tireChangeKm.value = changeKm
        } else {
            editor.remove(prefKey(pId, "tire_change_km"))
            _tireChangeKm.value = null
        }

        if (nextRotationKm != null) {
            editor.putInt(prefKey(pId, "tire_rotation_km"), nextRotationKm)
            _tireRotationKm.value = nextRotationKm
        } else {
            editor.remove(prefKey(pId, "tire_rotation_km"))
            _tireRotationKm.value = null
        }
        
        editor.apply()
    }

    fun updateOilChange(lastDate: Long?, lastKm: Int?, intervalMonths: Int, intervalKm: Int) {
        val pId = activeProfileId.value
        val editor = sharedPrefs.edit()
        
        if (lastDate != null) {
            editor.putLong(prefKey(pId, "oil_last_date"), lastDate)
            _oilLastChangeDate.value = lastDate
            
            val calendar = java.util.Calendar.getInstance().apply {
                timeInMillis = lastDate
            }
            calendar.add(java.util.Calendar.MONTH, intervalMonths)
            val nextDate = calendar.timeInMillis
            editor.putLong(prefKey(pId, "oil_next_date"), nextDate)
            _oilNextDate.value = nextDate
        } else {
            editor.remove(prefKey(pId, "oil_last_date"))
            editor.remove(prefKey(pId, "oil_next_date"))
            _oilLastChangeDate.value = null
            _oilNextDate.value = null
        }
        
        if (lastKm != null) {
            editor.putInt(prefKey(pId, "oil_last_km"), lastKm)
            _oilLastChangeKm.value = lastKm
            
            val nextKm = lastKm + intervalKm
            editor.putInt(prefKey(pId, "oil_change_km"), nextKm)
            _oilChangeKm.value = nextKm
        } else {
            editor.remove(prefKey(pId, "oil_last_km"))
            editor.remove(prefKey(pId, "oil_change_km"))
            _oilLastChangeKm.value = null
            _oilChangeKm.value = null
        }
        
        editor.putInt(prefKey(pId, "oil_interval_months"), intervalMonths)
        _oilIntervalMonths.value = intervalMonths
        
        editor.putInt(prefKey(pId, "oil_interval_km"), intervalKm)
        _oilIntervalKm.value = intervalKm
        
        editor.apply()
        checkOilDeadline()
    }

    fun updateInsurance(expiry: Long?, amount: Double, company: String) {
        val pId = activeProfileId.value
        val editor = sharedPrefs.edit()
        if (expiry != null) editor.putLong(prefKey(pId, "insurance_expiry_date"), expiry)
        else editor.remove(prefKey(pId, "insurance_expiry_date"))
        editor.putFloat(prefKey(pId, "insurance_amount"), amount.toFloat())
        editor.putString(prefKey(pId, "insurance_company"), company)
        editor.apply()
        _insuranceExpiryDate.value = expiry
        _insuranceAmount.value = amount
        _insuranceCompany.value = company
        checkInsuranceDeadline()
    }

    fun updateBollo(expiry: Long?, amount: Double) {
        val pId = activeProfileId.value
        val editor = sharedPrefs.edit()
        if (expiry != null) editor.putLong(prefKey(pId, "bollo_expiry_date"), expiry)
        else editor.remove(prefKey(pId, "bollo_expiry_date"))
        editor.putFloat(prefKey(pId, "bollo_amount"), amount.toFloat())
        editor.apply()
        _bolloExpiryDate.value = expiry
        _bolloAmount.value = amount
        checkBolloDeadline()
    }

    fun updateRevisione(lastDate: Long?, isNew: Boolean, amount: Double) {
        val pId = activeProfileId.value
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

        if (lastDate != null) editor.putLong(prefKey(pId, "revisione_last_date"), lastDate)
        else editor.remove(prefKey(pId, "revisione_last_date"))

        if (expiry != null) editor.putLong(prefKey(pId, "revisione_expiry_date"), expiry)
        else editor.remove(prefKey(pId, "revisione_expiry_date"))

        editor.putBoolean(prefKey(pId, "revisione_is_new"), isNew)
        editor.putFloat(prefKey(pId, "revisione_amount"), amount.toFloat())
        editor.apply()

        _revisioneLastDate.value = lastDate
        _revisioneExpiryDate.value = expiry
        _revisioneIsNew.value = isNew
        _revisioneAmount.value = amount

        checkRevisioneDeadline()
    }

    // --- REFUELING OPERATIONS ---
    fun addRefueling(
        dateMillis: Long,
        pricePerLiter: Double,
        liters: Double,
        totalPrice: Double,
        currentKm: Int,
        targetProfileId: Long = activeProfileId.value
    ) {
        viewModelScope.launch {
            val lastRefueling = refuelingDao.getLastRefuelingForProfile(targetProfileId)
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
                kmDrivenSinceLast = kmDriven,
                profileId = targetProfileId
            )
            refuelingDao.insertRefueling(newRefueling)

            val rotationThreshold = if (sharedPrefs.contains(prefKey(targetProfileId, "tire_rotation_km"))) sharedPrefs.getInt(prefKey(targetProfileId, "tire_rotation_km"), 0) else null
            if (rotationThreshold != null && currentKm >= rotationThreshold) {
                sendTireRotationNotification()
            }

            val oilThreshold = if (sharedPrefs.contains(prefKey(targetProfileId, "oil_change_km"))) sharedPrefs.getInt(prefKey(targetProfileId, "oil_change_km"), 0) else null
            if (oilThreshold != null && currentKm >= oilThreshold) {
                sendOilChangeNotification()
            }
        }
    }

    fun updateRefueling(
        id: Long,
        dateMillis: Long,
        pricePerLiter: Double,
        liters: Double,
        totalPrice: Double,
        currentKm: Int,
        targetProfileId: Long = activeProfileId.value
    ) {
        viewModelScope.launch {
            val prevRefueling = refuelingDao.getPreviousRefuelingForProfile(targetProfileId, dateMillis)
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
                kmDrivenSinceLast = kmDriven,
                profileId = targetProfileId
            )
            refuelingDao.updateRefueling(updatedRefueling)
        }
    }

    fun deleteRefueling(refueling: Refueling) {
        viewModelScope.launch {
            refuelingDao.deleteRefueling(refueling)
        }
    }

    // --- NOTIFICATIONS & DEADLINES (Iterates all profiles) ---
    private fun getProfileDisplayName(profileId: Long): String {
        val prof = profiles.value.find { it.id == profileId } ?: return "Auto"
        return if (prof.vehicleName.isNotBlank()) "${prof.name} (${prof.vehicleName})" else prof.name
    }

    fun checkAllDeadlines() {
        val allProfilesList = profiles.value
        val profileIdsToCheck = if (allProfilesList.isNotEmpty()) allProfilesList.map { it.id } else listOf(1L)

        for (pId in profileIdsToCheck) {
            val profInfo = getProfileDisplayName(pId)
            
            // Check Insurance
            if (sharedPrefs.contains(prefKey(pId, "insurance_expiry_date"))) {
                val expiry = sharedPrefs.getLong(prefKey(pId, "insurance_expiry_date"), 0)
                if (expiry > 0) {
                    val daysToExpiry = (expiry - System.currentTimeMillis()) / (24 * 60 * 60 * 1000)
                    if (daysToExpiry <= 30) {
                        val dateStr = formatDate(expiry)
                        val title = if (daysToExpiry < 0) "🚨 Assicurazione Scaduta: $profInfo" else "⚠️ Assicurazione in Scadenza: $profInfo"
                        val text = if (daysToExpiry < 0) "L'assicurazione per $profInfo è scaduta il $dateStr!" else "L'assicurazione per $profInfo scade il $dateStr."
                        sendNotification((1000 + pId * 10 + 1).toInt(), title, text)
                    }
                }
            }

            // Check Bollo
            if (sharedPrefs.contains(prefKey(pId, "bollo_expiry_date"))) {
                val expiry = sharedPrefs.getLong(prefKey(pId, "bollo_expiry_date"), 0)
                if (expiry > 0) {
                    val daysToExpiry = (expiry - System.currentTimeMillis()) / (24 * 60 * 60 * 1000)
                    if (daysToExpiry <= 30) {
                        val dateStr = formatDate(expiry)
                        val title = if (daysToExpiry < 0) "🚨 Bollo Scaduto: $profInfo" else "⚠️ Bollo in Scadenza: $profInfo"
                        val text = if (daysToExpiry < 0) "Il bollo per $profInfo è scaduto il $dateStr!" else "Il bollo per $profInfo scade il $dateStr."
                        sendNotification((1000 + pId * 10 + 2).toInt(), title, text)
                    }
                }
            }

            // Check Revisione
            if (sharedPrefs.contains(prefKey(pId, "revisione_expiry_date"))) {
                val expiry = sharedPrefs.getLong(prefKey(pId, "revisione_expiry_date"), 0)
                if (expiry > 0) {
                    val daysToExpiry = (expiry - System.currentTimeMillis()) / (24 * 60 * 60 * 1000)
                    if (daysToExpiry <= 30) {
                        val dateStr = formatDate(expiry)
                        val title = if (daysToExpiry < 0) "🚨 Revisione Scaduta: $profInfo" else "⚠️ Revisione in Scadenza: $profInfo"
                        val text = if (daysToExpiry < 0) "La revisione per $profInfo è scaduta il $dateStr!" else "La revisione per $profInfo scade il $dateStr."
                        sendNotification((1000 + pId * 10 + 3).toInt(), title, text)
                    }
                }
            }

            // Check Oil
            if (sharedPrefs.contains(prefKey(pId, "oil_next_date"))) {
                val nextDate = sharedPrefs.getLong(prefKey(pId, "oil_next_date"), 0)
                if (nextDate > 0) {
                    val daysToExpiry = (nextDate - System.currentTimeMillis()) / (24 * 60 * 60 * 1000)
                    if (daysToExpiry <= 30) {
                        val dateStr = formatDate(nextDate)
                        val title = if (daysToExpiry < 0) "🚨 Tagliando Scaduto: $profInfo" else "⚠️ Tagliando in Scadenza: $profInfo"
                        val text = if (daysToExpiry < 0) "Il tagliando per $profInfo è scaduto il $dateStr!" else "Il tagliando per $profInfo scade il $dateStr."
                        sendNotification((1000 + pId * 10 + 4).toInt(), title, text)
                    }
                }
            }
        }
    }

    private fun checkOilDeadline() {
        val pId = activeProfileId.value
        val nextDate = _oilNextDate.value ?: return
        val profInfo = getProfileDisplayName(pId)
        val daysToExpiry = (nextDate - System.currentTimeMillis()) / (24 * 60 * 60 * 1000)
        if (daysToExpiry <= 30) {
            val dateStr = formatDate(nextDate)
            val title = if (daysToExpiry < 0) "🚨 Tagliando Scaduto: $profInfo" else "⚠️ Tagliando in Scadenza: $profInfo"
            val text = if (daysToExpiry < 0) "Il tagliando auto per $profInfo è scaduto il $dateStr!" else "Il tagliando auto per $profInfo scade il $dateStr."
            sendNotification((1000 + pId * 10 + 4).toInt(), title, text)
        }
    }

    private fun checkInsuranceDeadline() {
        val pId = activeProfileId.value
        val expiry = _insuranceExpiryDate.value ?: return
        val profInfo = getProfileDisplayName(pId)
        val daysToExpiry = (expiry - System.currentTimeMillis()) / (24 * 60 * 60 * 1000)
        if (daysToExpiry <= 30) {
            val dateStr = formatDate(expiry)
            val title = if (daysToExpiry < 0) "🚨 Assicurazione Scaduta: $profInfo" else "⚠️ Assicurazione in Scadenza: $profInfo"
            val text = if (daysToExpiry < 0) "L'assicurazione per $profInfo è scaduta il $dateStr!" else "L'assicurazione per $profInfo scade il $dateStr."
            sendNotification((1000 + pId * 10 + 1).toInt(), title, text)
        }
    }

    private fun checkBolloDeadline() {
        val pId = activeProfileId.value
        val expiry = _bolloExpiryDate.value ?: return
        val profInfo = getProfileDisplayName(pId)
        val daysToExpiry = (expiry - System.currentTimeMillis()) / (24 * 60 * 60 * 1000)
        if (daysToExpiry <= 30) {
            val dateStr = formatDate(expiry)
            val title = if (daysToExpiry < 0) "🚨 Bollo Scaduto: $profInfo" else "⚠️ Bollo in Scadenza: $profInfo"
            val text = if (daysToExpiry < 0) "Il bollo auto per $profInfo è scaduto il $dateStr!" else "Il bollo auto per $profInfo scade il $dateStr."
            sendNotification((1000 + pId * 10 + 2).toInt(), title, text)
        }
    }

    private fun checkRevisioneDeadline() {
        val pId = activeProfileId.value
        val expiry = _revisioneExpiryDate.value ?: return
        val profInfo = getProfileDisplayName(pId)
        val daysToExpiry = (expiry - System.currentTimeMillis()) / (24 * 60 * 60 * 1000)
        if (daysToExpiry <= 30) {
            val dateStr = formatDate(expiry)
            val title = if (daysToExpiry < 0) "🚨 Revisione Scaduta: $profInfo" else "⚠️ Revisione in Scadenza: $profInfo"
            val text = if (daysToExpiry < 0) "La revisione dell'auto per $profInfo è scaduta il $dateStr!" else "La revisione dell'auto per $profInfo scade il $dateStr."
            sendNotification((1000 + pId * 10 + 3).toInt(), title, text)
        }
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

    private fun sendTireRotationNotification() {
        val pId = activeProfileId.value
        val profInfo = getProfileDisplayName(pId)
        sendNotification((1000 + pId * 10 + 5).toInt(), "🔧 Inversione Gomme: $profInfo", "Hai raggiunto i km impostati per l'inversione gomme ($profInfo).")
    }

    private fun sendOilChangeNotification() {
        val pId = activeProfileId.value
        val profInfo = getProfileDisplayName(pId)
        sendNotification((1000 + pId * 10 + 4).toInt(), "⚠️ Cambio Olio: $profInfo", "Hai raggiunto i km impostati per il cambio dell'olio ($profInfo).")
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
