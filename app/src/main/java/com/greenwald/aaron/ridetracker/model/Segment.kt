package com.greenwald.aaron.ridetracker.model

import java.io.Serializable
import java.util.Date

data class Segment(
    val startedTimestamp: Date,
    val id: SegmentId = 0,
    val segmentPoints: List<SegmentPoint> = emptyList()
): Serializable