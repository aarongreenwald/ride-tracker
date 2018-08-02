package com.greenwald.aaron.ridetracker.model

import java.util.ArrayList
import java.util.Date

class Segment {
    var startedTimestamp: Date? = null
        private set
    var id: Long = 0
        private set
    var segmentPoints = ArrayList<SegmentPoint>()

    constructor(startedTimestamp: Date) {
        this.startedTimestamp = startedTimestamp
    }

    constructor(id: Long, startedTimestamp: Date) {
        this.id = id
        this.startedTimestamp = startedTimestamp
    }

    fun setId(id: Long?) {
        this.id = id!!
    }
}
