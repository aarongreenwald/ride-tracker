package com.greenwald.aaron.ridetracker.model

import com.google.android.gms.maps.model.LatLng

import java.io.Serializable
import java.util.ArrayList

class Trip : Serializable {
    var name: String? = null
        private set
    var id: Long = 0
        private set
    private var segmentList = ArrayList<Segment>()

    val allLocations: ArrayList<LatLng>
        get() {
            val result = ArrayList<LatLng>()
            for (segment in segmentList)
                for (segmentPoint in segment.segmentPoints)
                    result.add(segmentPoint.latLng)

            return result
        }

    val startingPoint: SegmentPoint?
        get() = if (!segmentList.isEmpty() && !segmentList[0].segmentPoints.isEmpty()) {
            segmentList[0].segmentPoints[0]
        } else null

    constructor(name: String) {
        this.name = name
    }

    constructor(id: Long, name: String) {
        this.id = id
        this.name = name
    }

    fun setId(id: Long?) {
        this.id = id!!
    }

    fun setSegments(segments: ArrayList<Segment>) {
        this.segmentList = segments
    }
}
