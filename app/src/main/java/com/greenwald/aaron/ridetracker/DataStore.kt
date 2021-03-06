package com.greenwald.aaron.ridetracker

import android.content.Context
import com.greenwald.aaron.ridetracker.model.*
import java.util.*

internal class DataStore(context: Context) {

    private val db: DatabaseHelper = DatabaseHelper(context)

    val trips: ArrayList<Trip>
        get() = db.getTrips()

    fun createTrip(name: String): Trip {
        val trip = Trip(name)
        val id = db.insertTrip(trip)
        return trip.copy(id = id)
    }

    fun startTripSegment(trip: Trip): Segment {
        val segment = Segment(startedTimestamp = Date())
        val id = db.insertSegment(trip, segment)
        return segment.copy(id = id)
    }

    fun stopTripSegment(segment: Segment) {
        db.updateSegment(segment.id, Date())
    }

    fun recordSegmentPoint(segment: Segment, point: SegmentPoint) {
        //should the timestamp be set here, or does it come as part of the point?
        val segmentPointId = db.insertSegmentPoint(segment, point)
        db.insertSegmentPointStats(segmentPointId, point.distance, point.elapsedTime, point.altitudeChange)
    }

    fun getTrip(id: TripId): Trip {
        return db.getTrip(id)
    }

    fun getTripWithDetails(id: TripId): Trip {
        return db.getTripWithDetails(id)
    }

    fun setTripName(id: TripId, newName: String) {
        db.updateTripName(id, newName)
    }

    fun getSegment(id: SegmentId): Segment {
        return db.getSegment(id)
    }

    fun deleteTrip(ids: Array<TripId>) {
        db.deleteTrip(ids)
    }

    fun mergeTrips(from: Array<TripId>, to: TripId) {
        db.mergeTrips(from, to)
    }

}

