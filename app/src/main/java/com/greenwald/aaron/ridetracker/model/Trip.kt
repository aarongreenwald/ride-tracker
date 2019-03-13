package com.greenwald.aaron.ridetracker.model

import com.google.android.gms.maps.model.LatLng

import java.io.Serializable

data class Trip (val name: String,
                 val id: Long = 0,
                 val distance: Kilometers = Kilometers(0.0),
                 val ridingTime: Milliseconds = Milliseconds(0),
                 val elapsedTime: Milliseconds = Milliseconds(0),
                 val maxSpeed: KilometersPerHour = KilometersPerHour(0.0),
                 val maxAltitude: Meters = Meters(0.0),
                 val minAltitude: Meters = Meters(0.0),
                 val segments: List<Segment> = emptyList()) : Serializable {

    val averageRidingSpeed: KilometersPerHour get() = KilometersPerHour.from(distance, ridingTime)
    val altitudeRange: Meters get() = Meters(maxAltitude.value - minAltitude.value)
    val stoppedTime: Milliseconds get() = Milliseconds(elapsedTime.value - ridingTime.value)

    fun getAllPoints(): List<LatLng> =
            segments.flatMap { segment -> segment.segmentPoints.map(SegmentPoint::latLng) }
}
