package com.greenwald.aaron.ridetracker.model

import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson

import java.util.Date

data class SegmentPoint(val latitude: Double,
                        val longitude: Double,
                        val accuracy: Double,
                        val dateTime: Date,
                        val altitude: Double) {

    val latLng: LatLng
        get() = LatLng(this.latitude, this.longitude)

    override fun toString(): String {
        val gson = Gson()
        return gson.toJson(this)
    }

    companion object {

        fun fromString(string: String): SegmentPoint {
            val gson = Gson()
            return gson.fromJson(string, SegmentPoint::class.java)
        }
    }
}
