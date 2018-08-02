package com.greenwald.aaron.ridetracker.model

import java.util.Date

data class Segment(
        val startedTimestamp: Date,
        val id: Long = 0,
        val segmentPoints: List<SegmentPoint> = emptyList()
)