package com.greenwald.aaron.ridetracker.model

data class KilometersPerHour(val value: Long) {
    override fun toString() = "$value kph"

    companion object {
        fun from(distance: Meters, time: Milliseconds): KilometersPerHour {
            val km = distance / 1000
            val hours = time.value / MILLISECONDS_PER_HOUR
            return KilometersPerHour(km / hours)
        }
    }
}

data class Kilometers(val value: Long) {
    override fun toString() = "$value km"
}

data class Milliseconds(val value: Long) {
    private fun pad(value: Long) = value.toString().padStart(2, '0')

    override fun toString(): String {
        val hours = value / (1000 * 60 * 60)
        val minutes = (value - hours) / (1000 * 60)
        val seconds = (value - hours - minutes) / 1000
        return "${pad(hours)}:${pad(minutes)}:${pad(seconds)}"
    }
}

data class Degrees(val value: Long) {
    companion object {
        private fun radiansToDegrees(radians: Double) =
                Degrees((radians / (Math.PI / 180)).toLong())

        fun from(altitudeChange: Meters, distance: Meters): Degrees {
            val slope = altitudeChange / distance
            val radians = Math.atan(slope.toDouble())
            return radiansToDegrees(radians)
        }
    }
}

typealias Meters = Long
typealias TripId = Long
typealias SegmentId = Long
typealias SegmentPointId = Long

private const val MILLISECONDS_PER_HOUR = 1000 * 60 * 60


