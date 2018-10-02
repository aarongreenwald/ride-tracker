package com.greenwald.aaron.ridetracker.model

typealias KilometersPerHour = Long
typealias Kilometers = Long
typealias Meters = Long
typealias Milliseconds = Long
typealias Degrees = Long
typealias TripId = Long
typealias SegmentId = Long
typealias SegmentPointId = Long

private val MILLISECONDS_PER_HOUR = 1000 * 60 * 60

fun slopeAngle(altitudeChange: Meters, distance: Meters): Degrees {
    val slope = altitudeChange / distance
    val radians = Math.atan(slope.toDouble())
    return radiansToDegrees(radians)
}

fun kph(distance: Meters, time: Milliseconds): KilometersPerHour {
    val km = distance / 1000
    val hours = time / MILLISECONDS_PER_HOUR
    return km / hours
}

private fun radiansToDegrees(radians: Double): Degrees = (radians / (Math.PI / 180)).toLong()