package com.greenwald.aaron.ridetracker.model

import java.io.Serializable
import java.util.Date

data class Segment(
    val id: SegmentId = 0,
    val startedTimestamp: Date,
    val stoppedTimestamp: Date? = null,
    val distance: Kilometers = Kilometers(0.0),
    val elapsedTime: Milliseconds = Milliseconds(0),
    val maxSpeed: KilometersPerHour = KilometersPerHour(0.0),
    val maxAltitude: Meters = Meters(0.0),
    val minAltitude: Meters = Meters(0.0),
    val segmentPoints: List<SegmentPoint> = emptyList()
): Serializable {
    val averageSpeed: KilometersPerHour get() = KilometersPerHour.from(distance, elapsedTime)
    val altitudeRange: Meters get() = Meters(maxAltitude.value - minAltitude.value)
}