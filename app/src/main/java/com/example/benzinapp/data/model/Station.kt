package com.example.benzinapp.data.model

import com.google.gson.annotations.SerializedName

data class Station(
    @SerializedName("id") val id: Int,
    @SerializedName("operator") val operator: String,
    @SerializedName("brand") val brand: String,
    @SerializedName("name") val name: String,
    @SerializedName("type") val type: String,
    @SerializedName("address") val address: String,
    @SerializedName("city") val city: String,
    @SerializedName("province") val province: String,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("distance_km") val distanceKm: Double,
    @SerializedName("price") val price: Double,
    @SerializedName("is_self") val isSelf: Boolean,
    @SerializedName("dt_comu") val dtComu: String
)
