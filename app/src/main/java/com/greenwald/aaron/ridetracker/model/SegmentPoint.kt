package com.greenwald.aaron.ridetracker.model

import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson

import java.util.Date

data class SegmentPoint(val latitude: Double,
                        val longitude: Double,
                        val accuracy: Double,
                        val dateTime: Date,
                        val altitude: Meters,
                        val altitudeChange: Meters = Meters(0.0),
                        val elapsedTime: Milliseconds = Milliseconds(0),
                        val distance: Meters = Meters(0.0)) {

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
