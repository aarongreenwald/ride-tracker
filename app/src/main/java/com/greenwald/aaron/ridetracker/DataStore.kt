package com.greenwald.aaron.ridetracker


import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.greenwald.aaron.ridetracker.model.Segment
import com.greenwald.aaron.ridetracker.model.SegmentPoint
import com.greenwald.aaron.ridetracker.model.Trip
import java.time.Instant
import java.util.*


private val DATABASE_VERSION = 1
private val DATABASE_NAME = "ride-tracker"

internal class DataStore(context: Context) {

    private val db: DatabaseHelper

    val trips: ArrayList<Trip>
        get() = db.trips

    init {
        this.db = DatabaseHelper(context)
    }

    fun createTrip(name: String): Trip {
        val trip = Trip(name)
        val id = db.addTrip(trip)
        return trip.copy(id = id)
    }

    fun startTripSegment(trip: Trip): Segment {
        val segment = Segment(Date.from(Instant.now()))
        val id = db.addSegment(trip, segment)
        return segment.copy(id = id)
    }

    fun stopTripSegment(segment: Segment) {
        db.updateSegment(segment.id, Date.from(Instant.now()))
    }

    fun recordSegmentPoint(segment: Segment, point: SegmentPoint) {
        //should the timestamp be set here, or does it come as part of the point?
        db.addSegmentPoint(segment, point)
    }

    fun getTrip(id: Long): Trip {
        return db.getTrip(id)
    }

    fun getTripWithDetails(id: Long): Trip {
        return db.getTripWithDetails(id)
    }


    internal inner class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

        val trips: ArrayList<Trip>
            get() {
                val trips = ArrayList<Trip>()

                val selectQuery = "SELECT  * FROM $TABLE_TRIPS order by $COL_ID desc"

                val db = this.readableDatabase
                val c = db.rawQuery(selectQuery, null)

                if (c.moveToFirst()) {
                    do {
                        val trip = createTripFromCursor(c)
                        trips.add(trip)
                    } while (c.moveToNext())
                }

                return trips
            }

        override fun onCreate(db: SQLiteDatabase) {

            db.execSQL(CREATE_TABLE_TRIPS)
            db.execSQL(CREATE_TABLE_TRIP_SEGMENTS)
            db.execSQL(CREATE_TABLE_SEGMENT_POINTS)
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            db.execSQL("DROP TABLE IF EXISTS $TABLE_TRIPS")
            db.execSQL("DROP TABLE IF EXISTS $TABLE_TRIP_SEGMENTS")
            db.execSQL("DROP TABLE IF EXISTS $TABLE_SEGMENT_POINTS")

            onCreate(db)
        }

        fun addTrip(trip: Trip): Long {
            val db = this.writableDatabase

            val values = ContentValues()
            values.put(COL_TRIP_NAME, trip.name)
            return db.insert(TABLE_TRIPS, null, values)

        }

        fun addSegment(trip: Trip, segment: Segment): Long {
            val db = this.writableDatabase

            val values = ContentValues()
            values.put(COL_TRIP_ID, trip.id)
            values.put(COL_SEGMENT_STARTED_TIMESTAMP, segment.startedTimestamp.toString())

            return db.insert(TABLE_TRIP_SEGMENTS, null, values)
        }

        fun addSegmentPoint(segment: Segment, point: SegmentPoint): Long {
            val db = this.writableDatabase

            val values = ContentValues()
            values.put(COL_TRIP_SEGMENT_ID, segment.id)
            values.put(COL_TIMESTAMP, point.dateTime.toString())
            values.put(COL_LATITUDE, point.latitude)
            values.put(COL_LONGITUDE, point.longitude)
            values.put(COL_ALTITUDE, point.altitude)
            values.put(COL_ACCURACY, point.accuracy)

            return db.insert(TABLE_SEGMENT_POINTS, null, values)
        }

        fun updateSegment(id: Long, stoppedTimestamp: Date) {
            val db = this.writableDatabase

            val values = ContentValues()
            values.put(COL_SEGMENT_STOPPED_TIMESTAMP, stoppedTimestamp.toString())

            db.update(TABLE_TRIP_SEGMENTS, values, "$COL_ID = ?",
                    arrayOf(id.toString()))
        }

        fun getTrip(id: Long): Trip {
            val db = this.readableDatabase

            val selectQuery = """SELECT  * FROM $TABLE_TRIPS WHERE $COL_ID = $id"""

            val c = db.rawQuery(selectQuery, null)

            c?.moveToFirst()

            return createTripFromCursor(c)
        }

        fun getTripWithDetails(id: Long): Trip {

            //            String selectQuery = "SELECT  * FROM " +
            //                    TABLE_TRIPS + "trips left join " +
            //                    TABLE_TRIP_SEGMENTS + "segments on trips." + COL_ID + " = segments." + COL_TRIP_ID + " left join " +
            //                    TABLE_SEGMENT_POINTS + "points on segments." + COL_ID + " = points." + COL_TRIP_SEGMENT_ID +
            //                    "WHERE trips." + COL_ID + " = " + id;

            //this is a super dumb way to do this, but "whatever" for now
            val trip = getTrip(id)
            return trip.copy(segments = getSegmentsForTripId(id))
        }

        private fun getSegmentsForTripId(tripId: Long): ArrayList<Segment> {
            val db = this.readableDatabase

            val selectQuery = """SELECT  * FROM $TABLE_TRIP_SEGMENTS WHERE $COL_TRIP_ID = $tripId"""

            val cursor = db.rawQuery(selectQuery, null)


            val result = ArrayList<Segment>()
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    val segment = createSegmentFromCursor(cursor)
                    val withSegmentPoints = segment.copy(segmentPoints = getSegmentPointsForSegmentId(segment.id))
                    result.add(withSegmentPoints)
                }
            }

            return result
        }

        private fun getSegmentPointsForSegmentId(segmentId: Long): ArrayList<SegmentPoint> {
            val db = this.readableDatabase

            val selectQuery = """SELECT  * FROM $TABLE_SEGMENT_POINTS WHERE $COL_TRIP_SEGMENT_ID = $segmentId"""

            val c = db.rawQuery(selectQuery, null)

            val result = ArrayList<SegmentPoint>()
            if (c != null) {
                while (c.moveToNext()) {
                    val segmentPoint = createTrackPointFromCursor(c)
                    result.add(segmentPoint)
                }
            }

            return result
        }

        private fun createTrackPointFromCursor(cursor: Cursor) = SegmentPoint(
            latitude = cursor.getDouble(cursor.getColumnIndex(COL_LATITUDE)),
            longitude = cursor.getDouble(cursor.getColumnIndex(COL_LONGITUDE)),
            accuracy = cursor.getDouble(cursor.getColumnIndex(COL_ACCURACY)),
            dateTime = Date(cursor.getString(cursor.getColumnIndex(COL_TIMESTAMP))),
            altitude = cursor.getDouble(cursor.getColumnIndex(COL_ALTITUDE))
        )

        private fun createSegmentFromCursor(c: Cursor) = Segment(
            startedTimestamp = Date(c.getString(c.getColumnIndex(COL_SEGMENT_STARTED_TIMESTAMP))),
            id = c.getLong(c.getColumnIndex(COL_ID))
        )


        private fun createTripFromCursor(cursor: Cursor?) = Trip(
            name = cursor!!.getString(cursor.getColumnIndex(COL_TRIP_NAME)),
            id = cursor.getLong(cursor.getColumnIndex(COL_ID))
        )


        private val TABLE_TRIPS = "trips"
        private val TABLE_TRIP_SEGMENTS = "trip_segments"
        private val TABLE_SEGMENT_POINTS = "segment_points"

        private val COL_ID = "id"

        private val COL_TRIP_NAME = "todo"
        private val COL_TRIP_ID = "trip_id"
        private val COL_SEGMENT_STARTED_TIMESTAMP = "started_time"
        private val COL_SEGMENT_STOPPED_TIMESTAMP = "stopped_time"

        private val COL_TRIP_SEGMENT_ID = "trip_segment_id"
        private val COL_LONGITUDE = "longitude"
        private val COL_LATITUDE = "latitude"
        private val COL_ALTITUDE = "altitude"
        private val COL_ACCURACY = "accuracy"
        private val COL_TIMESTAMP = "timestamp"


        private val CREATE_TABLE_TRIPS = """CREATE TABLE $TABLE_TRIPS(
            |$COL_ID INTEGER PRIMARY KEY,
            |$COL_TRIP_NAME TEXT )"""

        private val CREATE_TABLE_TRIP_SEGMENTS = """CREATE TABLE $TABLE_TRIP_SEGMENTS (
            |$COL_ID INTEGER PRIMARY KEY,
            |$COL_TRIP_ID INTEGER,
            |$COL_SEGMENT_STARTED_TIMESTAMP DATETIME,
            |$COL_SEGMENT_STOPPED_TIMESTAMP DATETIME)"""

        private val CREATE_TABLE_SEGMENT_POINTS = """CREATE TABLE $TABLE_SEGMENT_POINTS(
            |$COL_ID INTEGER PRIMARY KEY,
            |$COL_TRIP_SEGMENT_ID INTEGER,
            |$COL_TIMESTAMP DATETIME,
            |$COL_LATITUDE INTEGER,
            |$COL_LONGITUDE INTEGER,
            |$COL_ALTITUDE INTEGER,
            |$COL_ACCURACY INTEGER)"""

    }
}

