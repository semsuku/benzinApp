package com.example.benzinapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profiles")
data class Profile(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val vehicleName: String = "",
    val iconName: String = "car",
    val colorHex: String = "#1E88E5"
)
