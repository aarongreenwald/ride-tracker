package com.greenwald.aaron.ridetracker.model

data class KilometersPerHour(val value: Double) {
    override fun toString() = "${value.toInt()} km/h"

    companion object {
        fun from(distance: Kilometers, time: Milliseconds): KilometersPerHour {
            return from(distance.asMeters(), time)
        }

        fun from(distance: Meters, time: Milliseconds): KilometersPerHour {
            val km = distance.value / 1000
            val hours = time.asHours()

            if (hours == 0.0) return KilometersPerHour(0.0)

            return KilometersPerHour(km / hours)
        }
    }
}

data class Kilometers(val value: Double) {
    override fun toString(): String {
        return "${"%.1f".format(value)} km"
    }

    fun asMeters() = Meters(value * 1000)
}

data class Milliseconds(val value: Long) {
    private fun pad(value: Long) = value.toString().padStart(2, '0')

    fun asHours() = value.toDouble() / MILLISECONDS_PER_HOUR

    override fun toString(): String {
        val hours = value / MILLISECONDS_PER_HOUR
        val remainder = value % MILLISECONDS_PER_HOUR
        val minutes = remainder / MILLISECONDS_PER_MINUTE
        val milliseconds = remainder % MILLISECONDS_PER_MINUTE
        val seconds = milliseconds / 1000
        return "${pad(hours)}:${pad(minutes)}:${pad(seconds)}"
    }
}

data class Degrees(val value: Double) {
    companion object {
        private fun radiansToDegrees(radians: Double) =
                Degrees((radians / (Math.PI / 180)))

        fun from(altitudeChange: Meters, distance: Meters): Degrees {
            if (distance.value == 0.0) return Degrees(0.0)

            val slope = altitudeChange.value / distance.value
            val radians = Math.atan(slope.toDouble())
            return radiansToDegrees(radians)
        }
    }
}

data class Meters(val value: Double) {
    override fun toString() = "${value.toInt()}m"
}

typealias TripId = Long
typealias SegmentId = Long
typealias SegmentPointId = Long

private const val MILLISECONDS_PER_MINUTE = 1000 * 60
private const val MILLISECONDS_PER_HOUR = MILLISECONDS_PER_MINUTE * 60


