package com.greenwald.aaron.ridetracker.model

import com.google.android.gms.maps.model.LatLng

import java.io.Serializable
import java.util.ArrayList

data class Trip (val name: String,
                 val id: Long = 0,
                 val distance: Kilometers = 0,
                 val segments: List<Segment> = emptyList()) : Serializable {

    fun getAllLocations(): List<LatLng> =
            segments.flatMap({segment -> segment.segmentPoints.map(SegmentPoint::latLng) })

    val startingPoint: SegmentPoint?
        get() = segments.getOrNull(0)?.segmentPoints?.getOrNull(0)

}
