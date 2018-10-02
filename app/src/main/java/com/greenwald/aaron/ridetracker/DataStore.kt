package com.greenwald.aaron.ridetracker


import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.greenwald.aaron.ridetracker.model.*
import java.time.Instant
import java.util.*


private val DATABASE_VERSION = 3
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
        val id = db.insertTrip(trip)
        return trip.copy(id = id)
    }

    fun startTripSegment(trip: Trip): Segment {
        val segment = Segment(Date.from(Instant.now()))
        val id = db.insertSegment(trip, segment)
        return segment.copy(id = id)
    }

    fun stopTripSegment(segment: Segment) {
        db.updateSegment(segment.id, Date.from(Instant.now()))
        db.insertSegmentStats(segment.id)
    }

    fun recordSegmentPoint(segment: Segment, point: SegmentPoint) {
        //should the timestamp be set here, or does it come as part of the point?
        db.insertSegmentPoint(segment, point)
        //TODO segment point stats
    }

    fun getTrip(id: TripId): Trip {
        return db.getTrip(id)
    }

    fun getTripWithDetails(id: TripId): Trip {
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


            db.execSQL(CREATE_TABLE_STATS_TRIPS)
            db.execSQL(CREATE_TABLE_STATS_TRIP_SEGMENTS)
            db.execSQL(CREATE_TABLE_STATS_SEGMENT_POINTS)
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            db.execSQL("DROP TABLE IF EXISTS $TABLE_TRIPS")
            db.execSQL("DROP TABLE IF EXISTS $TABLE_TRIP_SEGMENTS")
            db.execSQL("DROP TABLE IF EXISTS $TABLE_SEGMENT_POINTS")

            db.execSQL("DROP TABLE IF EXISTS $TABLE_STATS_TRIPS")
            db.execSQL("DROP TABLE IF EXISTS $TABLE_STATS_TRIP_SEGMENTS")
            db.execSQL("DROP TABLE IF EXISTS $TABLE_STATS_SEGMENT_POINTS")

            onCreate(db)
        }

        fun insertTrip(trip: Trip): TripId {
            val db = this.writableDatabase

            val values = ContentValues()
            values.put(COL_TRIP_NAME, trip.name)
            return db.insert(TABLE_TRIPS, null, values)

        }

        fun insertSegment(trip: Trip, segment: Segment): SegmentId {
            val db = this.writableDatabase

            val values = ContentValues()
            values.put(COL_TRIP_ID, trip.id)
            values.put(COL_SEGMENT_STARTED_TIMESTAMP, segment.startedTimestamp.toString())

            return db.insert(TABLE_TRIP_SEGMENTS, null, values)
        }

        fun insertSegmentPoint(segment: Segment, point: SegmentPoint): SegmentPointId {
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

        fun updateSegment(id: SegmentId, stoppedTimestamp: Date) {
            val db = this.writableDatabase

            val values = ContentValues()
            values.put(COL_SEGMENT_STOPPED_TIMESTAMP, stoppedTimestamp.toString())

            db.update(TABLE_TRIP_SEGMENTS, values, "$COL_ID = ?",
                    arrayOf(id.toString()))
        }

        fun insertSegmentStats(id: SegmentId) {

            val db = this.writableDatabase

            val selectQuery = """SELECT
                SUM($COL_DISTANCE),
                SUM($COL_ELAPSED_TIME),
                MIN($COL_ALTITUDE),
                MAX($COL_ALTITUDE),
                MIN($COL_SLOPE),
                MAX($COL_SLOPE),
                SUM(CASE WHEN $COL_ALTITUDE_CHANGE > 0 THEN $COL_ALTITUDE_CHANGE ELSE 0 END),
                SUM(CASE WHEN $COL_ALTITUDE_CHANGE < 0 THEN ABS($COL_ALTITUDE_CHANGE) ELSE 0 END)
                FROM $TABLE_STATS_SEGMENT_POINTS WHERE $COL_SEGMENT_POINT_ID IN
                    (SELECT $COL_SEGMENT_POINT_ID FROM $TABLE_SEGMENT_POINTS WHERE $COL_TRIP_SEGMENT_ID = $id)""".trimMargin()

            val c = db.rawQuery(selectQuery, null)
            c!!.moveToFirst()

            val elapsedTime = Milliseconds(c.getLong(1))

            val values = ContentValues()
            values.put(COL_TRIP_SEGMENT_ID, id.toString())

            values.put(COL_DISTANCE, c.getLong(0))
            values.put(COL_ELAPSED_TIME, elapsedTime.value)
            values.put(COL_MIN_ALTITUDE, c.getLong(2))
            values.put(COL_MAX_ALTITUDE, c.getLong(3))
            values.put(COL_SPEED, KilometersPerHour.from(c.getLong(0), elapsedTime).value)
            values.put(COL_MIN_SLOPE, c.getLong(4))
            values.put(COL_MAX_SLOPE, c.getLong(5))
            values.put(COL_TOTAL_ASCENT, c.getLong(6))
            values.put(COL_TOTAL_DESCENT, c.getLong(7))

            db.insert(TABLE_STATS_TRIP_SEGMENTS, null, values)
            updateTripStatsForSegment(id)
        }

        fun insertSegmentPointStats(id: SegmentPointId, distance: Meters, elapsedTime: Milliseconds, altitudeChange: Meters) {
            val db = this.writableDatabase
            val values = ContentValues()
            values.put(COL_SEGMENT_POINT_ID, id.toString())
            values.put(COL_DISTANCE, distance)
            values.put(COL_ELAPSED_TIME, elapsedTime.value)
            values.put(COL_SPEED, KilometersPerHour.from(distance, elapsedTime).value)
            values.put(COL_ALTITUDE_CHANGE, altitudeChange)
            values.put(COL_SLOPE, Degrees.from(altitudeChange, distance).value)

            db.insert(TABLE_STATS_SEGMENT_POINTS, null, values)
        }

        fun getTrip(id: TripId): Trip {
            val db = this.readableDatabase

            val selectQuery = """SELECT  * FROM $TABLE_TRIPS a
                LEFT JOIN $TABLE_STATS_TRIPS b ON a.$COL_ID = b.$COL_TRIP_ID
                WHERE $COL_ID = $id""".trimMargin()

            val c = db.rawQuery(selectQuery, null)

            c?.moveToFirst()

            return createTripFromCursor(c)
        }

        fun getTripWithDetails(id: TripId): Trip {

            //            String selectQuery = "SELECT  * FROM " +
            //                    TABLE_TRIPS + "trips left join " +
            //                    TABLE_TRIP_SEGMENTS + "segments on trips." + COL_ID + " = segments." + COL_TRIP_ID + " left join " +
            //                    TABLE_SEGMENT_POINTS + "points on segments." + COL_ID + " = points." + COL_TRIP_SEGMENT_ID +
            //                    "WHERE trips." + COL_ID + " = " + id;

            //this is a super dumb way to do this, but "whatever" for now
            val trip = getTrip(id)
            return trip.copy(segments = getSegmentsForTripId(id))
        }

        private fun updateTripStatsForSegment(id: SegmentId) {

            deleteTripStatsForSegment(id)

            val db = this.writableDatabase

            val selectQuery = """SELECT
                SUM($COL_DISTANCE),
                SUM($COL_ELAPSED_TIME),
                MIN($COL_MIN_ALTITUDE),
                MAX($COL_MAX_ALTITUDE),
                MIN($COL_MIN_SLOPE),
                MAX($COL_MAX_SLOPE),
                SUM($COL_TOTAL_ASCENT),
                SUM($COL_TOTAL_DESCENT),
                MAX($COL_DISTANCE),
                MAX($COL_ELAPSED_TIME),
                FROM $TABLE_STATS_TRIP_SEGMENTS WHERE $COL_TRIP_SEGMENT_ID IN
                    (SELECT $COL_TRIP_SEGMENT_ID FROM $TABLE_STATS_TRIP_SEGMENTS WHERE $COL_TRIP_ID = $id)""".trimMargin()

            val c = db.rawQuery(selectQuery, null)
            c!!.moveToFirst()

            val values = ContentValues()
            values.put(COL_TRIP_SEGMENT_ID, id.toString())

            values.put(COL_DISTANCE, c.getString(0))
//            values.put(COL_ELAPSED_TIME, c.getString(1))
            val elapsedTime = c.getLong(1)
            values.put(COL_RIDE_TIME, elapsedTime)
            values.put(COL_MIN_ALTITUDE, c.getString(2))
            values.put(COL_MAX_ALTITUDE, c.getString(3))
            values.put(COL_SPEED, KilometersPerHour.from(c.getLong(0), Milliseconds(elapsedTime)).value)
            values.put(COL_MIN_SLOPE, c.getString(4))
            values.put(COL_MAX_SLOPE, c.getString(5))
            values.put(COL_TOTAL_ASCENT, c.getString(6))
            values.put(COL_TOTAL_DESCENT, c.getString(7))
            values.put(COL_MAX_TRIP_SEGMENT_DISTANCE, c.getString(8))
            values.put(COL_MAX_TRIP_SEGMENT_ELAPSED_TIME, c.getString(9))

            db.insert(TABLE_STATS_TRIPS, null, values)
        }

        private fun deleteTripStatsForSegment(id: SegmentId) {
            val db = db.writableDatabase
            val where = """$COL_TRIP_ID IN
                (SELECT $COL_TRIP_ID FROM $TABLE_TRIP_SEGMENTS WHERE $COL_ID = ?) """.trimMargin()
            db.delete(TABLE_STATS_TRIPS, where, arrayOf(id.toString()))
        }

        private fun getSegmentsForTripId(tripId: TripId): ArrayList<Segment> {
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

        private fun getSegmentPointsForSegmentId(segmentId: SegmentId): ArrayList<SegmentPoint> {
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
            id = cursor.getLong(cursor.getColumnIndex(COL_ID))//,
//            distance = cursor.getLong(cursor.getColumnIndex(COL_DISTANCE)) / 1000
        )


        private val TABLE_TRIPS = "trips"
        private val TABLE_TRIP_SEGMENTS = "trip_segments"
        private val TABLE_SEGMENT_POINTS = "segment_points"
        private val TABLE_STATS_SEGMENT_POINTS = "stats_segment_points"
        private val TABLE_STATS_TRIP_SEGMENTS = "stats_trip_segments"
        private val TABLE_STATS_TRIPS = "stats_trips"

        private val COL_ID = "id"

        private val COL_TRIP_NAME = "name"
        private val COL_TRIP_ID = "trip_id"
        private val COL_SEGMENT_STARTED_TIMESTAMP = "started_time"
        private val COL_SEGMENT_STOPPED_TIMESTAMP = "stopped_time"

        private val COL_TRIP_SEGMENT_ID = "trip_segment_id"
        private val COL_LONGITUDE = "longitude"
        private val COL_LATITUDE = "latitude"
        private val COL_ALTITUDE = "altitude"
        private val COL_ACCURACY = "accuracy"
        private val COL_TIMESTAMP = "timestamp"

        private val COL_SEGMENT_POINT_ID = "segment_point_id"
        private val COL_DISTANCE = "distance"
        private val COL_ELAPSED_TIME = "elapsed_time"
        private val COL_ALTITUDE_CHANGE = "altitude_change"
        private val COL_SLOPE = "slope"
        private val COL_SPEED = "speed"

        private val COL_MIN_ALTITUDE = "min_altitude"
        private val COL_MAX_ALTITUDE = "max_altitude"
        private val COL_MIN_SLOPE = "min_slope"
        private val COL_MAX_SLOPE = "max_slope"
        private val COL_TOTAL_ASCENT = "total_ascent"
        private val COL_TOTAL_DESCENT = "total_descent"

        private val COL_RIDE_TIME = "ride_time"
        private val COL_MAX_TRIP_SEGMENT_DISTANCE = "max_trip_segment_distance"
        private val COL_MAX_TRIP_SEGMENT_ELAPSED_TIME = "max_trip_segment_elapsed_time"


        private val CREATE_TABLE_TRIPS = """CREATE TABLE $TABLE_TRIPS(
            $COL_ID INTEGER PRIMARY KEY,
            $COL_TRIP_NAME TEXT )"""

        private val CREATE_TABLE_TRIP_SEGMENTS = """CREATE TABLE $TABLE_TRIP_SEGMENTS (
            $COL_ID INTEGER PRIMARY KEY,
            $COL_TRIP_ID INTEGER,
            $COL_SEGMENT_STARTED_TIMESTAMP DATETIME,
            $COL_SEGMENT_STOPPED_TIMESTAMP DATETIME)"""

        private val CREATE_TABLE_SEGMENT_POINTS = """CREATE TABLE $TABLE_SEGMENT_POINTS(
            $COL_ID INTEGER PRIMARY KEY,
            $COL_TRIP_SEGMENT_ID INTEGER,
            $COL_TIMESTAMP DATETIME,
            $COL_LATITUDE INTEGER,
            $COL_LONGITUDE INTEGER,
            $COL_ALTITUDE INTEGER,
            $COL_ACCURACY INTEGER)"""

        private val CREATE_TABLE_STATS_SEGMENT_POINTS = """CREATE TABLE $TABLE_STATS_SEGMENT_POINTS(
            $COL_SEGMENT_POINT_ID INTEGER PRIMARY KEY,
            $COL_DISTANCE LONG,
            $COL_ELAPSED_TIME LONG,
            $COL_ALTITUDE_CHANGE LONG,
            $COL_SLOPE LONG,
            $COL_SPEED LONG)"""

        private val CREATE_TABLE_STATS_TRIP_SEGMENTS = """CREATE TABLE $TABLE_STATS_TRIP_SEGMENTS(
            $COL_TRIP_SEGMENT_ID INTEGER PRIMARY KEY,
            $COL_DISTANCE LONG,
            $COL_ELAPSED_TIME LONG,
            $COL_SPEED LONG,
            $COL_MIN_ALTITUDE LONG,
            $COL_MAX_ALTITUDE LONG,
            $COL_MIN_SLOPE LONG,
            $COL_MAX_SLOPE LONG,
            $COL_TOTAL_ASCENT LONG,
            $COL_TOTAL_DESCENT LONG)"""

        private val CREATE_TABLE_STATS_TRIPS = """CREATE TABLE $TABLE_STATS_TRIPS(
            $COL_TRIP_ID INTEGER PRIMARY KEY,
            $COL_DISTANCE LONG,
            $COL_ELAPSED_TIME LONG,
            $COL_RIDE_TIME LONG,
            $COL_SPEED LONG,
            $COL_MIN_ALTITUDE LONG,
            $COL_MAX_ALTITUDE LONG,
            $COL_MIN_SLOPE LONG,
            $COL_MAX_SLOPE LONG,
            $COL_TOTAL_ASCENT LONG,
            $COL_TOTAL_DESCENT LONG,
            $COL_MAX_TRIP_SEGMENT_DISTANCE LONG,
            $COL_MAX_TRIP_SEGMENT_ELAPSED_TIME LONG)"""

    }
}

