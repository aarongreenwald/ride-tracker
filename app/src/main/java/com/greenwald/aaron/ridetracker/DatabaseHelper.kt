package com.greenwald.aaron.ridetracker

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import io.requery.android.database.sqlite.SQLiteDatabase
import io.requery.android.database.sqlite.SQLiteOpenHelper
import com.greenwald.aaron.ridetracker.model.*
import java.util.*

private const val DATABASE_VERSION = 29
private const val DATABASE_NAME = "ride-tracker"

internal class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {

        turnOnForeignKeySupport(db)
        db.execSQL(CREATE_TABLE_TRIPS)
        db.execSQL(CREATE_TABLE_TRIP_SEGMENTS)
        db.execSQL(CREATE_TABLE_SEGMENT_POINTS)

        db.execSQL(CREATE_TABLE_STATS_SEGMENT_POINTS)

        recreateViews(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {

        //TODO move the basic stats from ssp to the segment_points table and drop ssp as well
        if (oldVersion < 20) {
            arrayOf("stats_trip_segments", "stats_trips").forEach { table -> db.execSQL("""DROP TABLE IF EXISTS $table""") }
        }

        recreateViews(db)

    }

    private fun <T>withDatabase(func: (db: SQLiteDatabase) -> T): T {
        val db = this.writableDatabase
        turnOnForeignKeySupport(db)

        val result = func(db)

        db.close()
        return result
    }

    fun getTrips(): ArrayList<Trip> = withDatabase { db ->
        val trips = ArrayList<Trip>()

        val selectQuery = """SELECT  *
                FROM $TABLE_TRIPS a LEFT JOIN $VIEW_TRIPS b
                    ON a.$COL_ID = b.$COL_TRIP_ID
                order by $COL_ID desc""".trimMargin()

        val c = db.rawQuery(selectQuery, null)

        if (c.moveToFirst()) {
            do {
                val trip = createTripFromCursor(c)
                trips.add(trip)
            } while (c.moveToNext())
        }
        trips
    }

    fun insertTrip(trip: Trip): TripId {
        return withDatabase { db ->
            val values = ContentValues()
            values.put(COL_TRIP_NAME, trip.name)
            val tripId = db.insert(TABLE_TRIPS, null, values)
            tripId
        }
    }

    fun updateTripName(id: TripId, newName: String) {
        withDatabase { db ->
            val values = ContentValues()
            values.put(COL_TRIP_NAME, newName)

            db.update(TABLE_TRIPS, values, "$COL_ID = ?",
                    arrayOf(id.toString()))

        }
    }

    fun deleteTrip(ids: Array<TripId>) {
        withDatabase { db ->
            val idsAsStrings = ids.map { id -> id.toString() }.toTypedArray()
            val inList = ids.map { id -> "?" }.toTypedArray().joinToString (  "," )
            turnOnForeignKeySupport(db)
            db.delete(TABLE_TRIPS, "$COL_ID in ($inList)", idsAsStrings)
        }
    }

    fun mergeTrips(from: Array<TripId>, to: TripId) {
        withDatabase { db ->
            val idsAsStrings = from.map { id -> id.toString() }.toTypedArray()
            val inList = from.map { id -> "?" }.toTypedArray().joinToString (  "," )
            val values = ContentValues()
            values.put(COL_TRIP_ID, to)
            db.update(TABLE_TRIP_SEGMENTS, values, "$COL_TRIP_ID in ($inList)", idsAsStrings)
            turnOnForeignKeySupport(db)
            deleteTrip(from)
        }
    }

    fun insertSegment(trip: Trip, segment: Segment): SegmentId {
        return withDatabase { db ->
            val values = ContentValues()
            values.put(COL_TRIP_ID, trip.id)
            values.put(COL_SEGMENT_STARTED_TIMESTAMP, segment.startedTimestamp.time)

            db.insert(TABLE_TRIP_SEGMENTS, null, values)
        }
    }

    fun updateSegment(id: SegmentId, stoppedTimestamp: Date) {
        withDatabase { db->
            val values = ContentValues()
            values.put(COL_SEGMENT_STOPPED_TIMESTAMP, stoppedTimestamp.time)

            db.update(TABLE_TRIP_SEGMENTS, values, "$COL_ID = ?", arrayOf(id.toString()))
        }
    }

    fun insertSegmentPoint(segment: Segment, point: SegmentPoint): SegmentPointId {
        return withDatabase { db ->
            val values = ContentValues()
            values.put(COL_TRIP_SEGMENT_ID, segment.id)
            values.put(COL_TIMESTAMP, point.dateTime.toString())
            values.put(COL_LATITUDE, point.latitude)
            values.put(COL_LONGITUDE, point.longitude)
            values.put(COL_ALTITUDE, point.altitude.value)
            values.put(COL_ACCURACY, point.accuracy)

            db.insert(TABLE_SEGMENT_POINTS, null, values)
        }
    }

    fun insertSegmentPointStats(id: SegmentPointId, distance: Meters, elapsedTime: Milliseconds, altitudeChange: Meters) {
        withDatabase {db ->
            val values = ContentValues()
            values.put(COL_SEGMENT_POINT_ID, id.toString())
            values.put(COL_DISTANCE, distance.value)
            values.put(COL_ELAPSED_TIME, elapsedTime.value)
            values.put(COL_AVG_SPEED, KilometersPerHour.from(distance, elapsedTime).value)
            values.put(COL_ALTITUDE_CHANGE, altitudeChange.value)
            values.put(COL_SLOPE, Degrees.from(altitudeChange, distance).value)

            db.insert(TABLE_STATS_SEGMENT_POINTS, null, values)
        }
    }

    fun getTrip(id: TripId): Trip {
        return withDatabase { db ->
            val selectQuery = """
            SELECT *
            FROM $TABLE_TRIPS t
                LEFT JOIN $VIEW_TRIPS vt ON t.$COL_ID = vt.$COL_TRIP_ID
                WHERE t.$COL_ID = $id""".trimMargin()

            val c = db.rawQuery(selectQuery, null)

            c?.moveToFirst()
            createTripFromCursor(c)
        }
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

    private fun getSegmentsForTripId(tripId: TripId): ArrayList<Segment> {
        return withDatabase { db ->
            val selectQuery = """
            SELECT ts.started_time, ts.stopped_time, vs.* FROM $TABLE_TRIP_SEGMENTS ts
            inner join $VIEW_SEGMENTS vs on ts.id = vs.trip_segment_id
            WHERE ts.$COL_TRIP_ID = $tripId
        """.trimMargin()

            val cursor = db.rawQuery(selectQuery, null)


            val result = ArrayList<Segment>()
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    val segment = createSegmentFromCursor(cursor)
                    val withSegmentPoints = segment.copy(segmentPoints = getSegmentPointsForSegmentId(segment.id))
                    result.add(withSegmentPoints)
                }
            }
            result
        }
    }

    fun getSegment(id: SegmentId): Segment {
        return withDatabase { db ->
            val selectQuery = """
            SELECT ts.started_time, ts.stopped_time, vs.* FROM $TABLE_TRIP_SEGMENTS ts
            inner join $VIEW_SEGMENTS vs on ts.id = vs.trip_segment_id
            WHERE ts.$COL_ID = $id
        """.trimMargin()

            val c = db.rawQuery(selectQuery, null)

            c?.moveToFirst()
            createSegmentFromCursor(c)
            //                .copy(segmentPoints = getSegmentPointsForSegmentId(segment.id))
        }
    }

    private fun getSegmentPointsForSegmentId(segmentId: SegmentId): ArrayList<SegmentPoint> {
        return withDatabase { db ->
            val selectQuery = """SELECT  * FROM $TABLE_SEGMENT_POINTS WHERE $COL_TRIP_SEGMENT_ID = $segmentId"""

            val c = db.rawQuery(selectQuery, null)

            val result = ArrayList<SegmentPoint>()
            if (c != null) {
                while (c.moveToNext()) {
                    val segmentPoint = createTrackPointFromCursor(c)
                    result.add(segmentPoint)
                }
            }
            result
        }
    }

    private fun createTrackPointFromCursor(cursor: Cursor) = SegmentPoint(
            latitude = cursor.getDouble(cursor.getColumnIndex(COL_LATITUDE)),
            longitude = cursor.getDouble(cursor.getColumnIndex(COL_LONGITUDE)),
            accuracy = cursor.getDouble(cursor.getColumnIndex(COL_ACCURACY)),
            dateTime = Date(cursor.getString(cursor.getColumnIndex(COL_TIMESTAMP))),
            altitude = Meters(cursor.getDouble(cursor.getColumnIndex(COL_ALTITUDE)))
    )

    private fun createSegmentFromCursor(cursor: Cursor) = Segment(
            id = cursor.getLong(cursor.getColumnIndex(COL_TRIP_SEGMENT_ID)),
            startedTimestamp = Date(cursor.getLong(cursor.getColumnIndex(COL_SEGMENT_STARTED_TIMESTAMP))),
            stoppedTimestamp = dateOrNull(cursor.getLong(cursor.getColumnIndex(COL_SEGMENT_STOPPED_TIMESTAMP))),
            distance = Kilometers((cursor.getDouble(cursor.getColumnIndex(COL_DISTANCE)) / 1000.0).toDouble()),
            elapsedTime = Milliseconds(cursor.getLong(cursor.getColumnIndex(COL_ELAPSED_TIME))),
            maxAltitude = Meters(cursor.getDouble(cursor.getColumnIndex(COL_MAX_ALTITUDE))),
            minAltitude = Meters(cursor.getDouble(cursor.getColumnIndex(COL_MIN_ALTITUDE))),
            maxSpeed = KilometersPerHour(cursor.getDouble(cursor.getColumnIndex(COL_MAX_SPEED)))
    )

    private fun dateOrNull(long: Long?): Date? = if (long != null) { Date(long) } else { null }

    private fun createTripFromCursor(cursor: Cursor?): Trip {
        return Trip(
                name = cursor!!.getString(cursor.getColumnIndex(COL_TRIP_NAME)),
                id = cursor.getLong(cursor.getColumnIndex(COL_ID)),
                distance = Kilometers((cursor.getDouble(cursor.getColumnIndex(COL_DISTANCE)) / 1000.0).toDouble()),
                elapsedTime = Milliseconds(cursor.getLong(cursor.getColumnIndex(COL_ELAPSED_TIME))),
                ridingTime = Milliseconds(cursor.getLong(cursor.getColumnIndex(COL_RIDE_TIME))),
                maxAltitude = Meters(cursor.getDouble(cursor.getColumnIndex(COL_MAX_ALTITUDE))),
                minAltitude = Meters(cursor.getDouble(cursor.getColumnIndex(COL_MIN_ALTITUDE))),
                maxSpeed = KilometersPerHour(cursor.getDouble(cursor.getColumnIndex(COL_MAX_SPEED)))
        )
    }

    private fun turnOnForeignKeySupport(db: SQLiteDatabase) {
        db.execSQL("PRAGMA foreign_keys = ON;")
    }

    private fun recreateViews(db: SQLiteDatabase) {
        arrayOf(VIEW_TRIPS, VIEW_SEGMENTS, VIEW_SEGMENT_POINTS).forEach { view -> db.execSQL("""DROP VIEW IF EXISTS $view""") }
        db.execSQL(CREATE_VIEW_SEGMENT_POINTS)
        db.execSQL(CREATE_VIEW_SEGMENTS)
        db.execSQL(CREATE_VIEW_TRIPS)
    }

    private val SPEED_SMOOTH_FACTOR = 5
    private val TABLE_TRIPS = "trips"
    private val TABLE_TRIP_SEGMENTS = "trip_segments"
    private val TABLE_SEGMENT_POINTS = "segment_points"
    private val TABLE_STATS_SEGMENT_POINTS = "stats_segment_points"

    private val VIEW_SEGMENT_POINTS = "v_segment_points"
    private val VIEW_TRIPS = "v_trips"
    private val VIEW_SEGMENTS = "v_segments"

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
    private val COL_AVG_SPEED = "speed"

    private val COL_MAX_SPEED = "max_speed"
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
            $COL_TRIP_NAME TEXT )""".trimMargin()

    private val CREATE_TABLE_TRIP_SEGMENTS = """CREATE TABLE $TABLE_TRIP_SEGMENTS (
            $COL_ID INTEGER PRIMARY KEY,
            $COL_TRIP_ID INTEGER NOT NULL,
            $COL_SEGMENT_STARTED_TIMESTAMP DATETIME,
            $COL_SEGMENT_STOPPED_TIMESTAMP DATETIME,
            FOREIGN KEY($COL_TRIP_ID) REFERENCES $TABLE_TRIPS($COL_ID) ON DELETE CASCADE)""".trimMargin()

    private val CREATE_TABLE_SEGMENT_POINTS = """CREATE TABLE $TABLE_SEGMENT_POINTS(
            $COL_ID INTEGER PRIMARY KEY,
            $COL_TRIP_SEGMENT_ID INTEGER NOT NULL,
            $COL_TIMESTAMP DATETIME NOT NULL,
            $COL_LATITUDE DOUBLE,
            $COL_LONGITUDE DOUBLE,
            $COL_ALTITUDE INTEGER,
            $COL_ACCURACY INTEGER,
            FOREIGN KEY($COL_TRIP_SEGMENT_ID) REFERENCES $TABLE_TRIP_SEGMENTS($COL_ID) ON DELETE CASCADE)""".trimMargin()

    private val CREATE_TABLE_STATS_SEGMENT_POINTS = """CREATE TABLE $TABLE_STATS_SEGMENT_POINTS(
            $COL_SEGMENT_POINT_ID INTEGER UNIQUE NOT NULL,
            $COL_DISTANCE DOUBLE,
            $COL_ELAPSED_TIME DOUBLE,
            $COL_ALTITUDE_CHANGE DOUBLE,
            $COL_SLOPE DOUBLE,
            $COL_AVG_SPEED DOUBLE,
            FOREIGN KEY($COL_SEGMENT_POINT_ID) REFERENCES $TABLE_SEGMENT_POINTS($COL_ID) ON DELETE CASCADE)""".trimMargin()

    private val CREATE_VIEW_SEGMENT_POINTS = """
        create view $VIEW_SEGMENT_POINTS as

        with smoothed as (
        select ts.trip_id
            , sp.trip_segment_id
            , sp.id segment_point_id
            , sp.timestamp
            , sp.latitude
            , sp.longitude
            , ssp.elapsed_time
            , ssp.distance
            , ssp.distance / (ssp.elapsed_time / 3600)  as speed
            , sum(distance) over (order by sp.id rows between $SPEED_SMOOTH_FACTOR PRECEDING and 0 FOLLOWING) adj_distance
            , sum(elapsed_time) over (order by sp.id rows between $SPEED_SMOOTH_FACTOR PRECEDING and 0 FOLLOWING) adj_time
        from trip_segments ts
            inner join segment_points sp on ts.id = sp.trip_segment_id
            inner join stats_segment_points ssp on sp.id = ssp.segment_point_id
        )
        select *
            , adj_distance * 3600 / adj_time adj_speed from smoothed;
    """.trimIndent()

    private val CREATE_VIEW_SEGMENTS = """
        create view $VIEW_SEGMENTS as

        select
            vsp.trip_segment_id,
            SUM(vsp.distance) distance,
            SUM(vsp.elapsed_time) elapsed_time,
            MIN(sp.altitude) min_altitude,
            MAX(sp.altitude) max_altitude,
            MIN(ssp.slope) min_slope,
            MAX(ssp.slope) max_slope,
            SUM(CASE WHEN ssp.altitude_change > 0 THEN ssp.altitude_change ELSE 0 END) total_ascent,
            SUM(CASE WHEN ssp.altitude_change < 0 THEN ABS(ssp.altitude_change) ELSE 0 END) total_descent,
            MAX(vsp.adj_speed) max_speed
        from v_segment_points vsp
            inner join stats_segment_points ssp on vsp.segment_point_id = ssp.segment_point_id
            inner join segment_points sp ON vsp.segment_point_id = sp.id
        group by vsp.trip_segment_id
    """.trimIndent()

    //TODO: if querying for a specific tripId,
    // inlining the queries instead of views
    // makes  this about 4x faster because you can put the where before the
    // expensive calculations!!
    //if querying all at once, it probably doesn't matter
    //TVFs would be nice.
    //another option is to persist this data
    private val CREATE_VIEW_TRIPS = """

    create view $VIEW_TRIPS as

    SELECT
        ts.trip_id,
        SUM(vs.distance) distance,
        MAX(ts.stopped_time) - MIN(ts.started_time) elapsed_time,
        SUM(vs.elapsed_time) ride_time,
        MAX(vs.max_speed) max_speed,
        MIN(vs.min_altitude) min_altitude,
        MAX(vs.max_altitude) max_altitude,
        MIN(vs.$COL_MIN_SLOPE) $COL_MIN_SLOPE,
        MIN(vs.$COL_MAX_SLOPE) $COL_MAX_SLOPE,
        SUM(vs.$COL_TOTAL_ASCENT) $COL_TOTAL_ASCENT,
        SUM(vs.$COL_TOTAL_DESCENT) $COL_TOTAL_DESCENT,
        MAX(vs.$COL_DISTANCE) $COL_MAX_TRIP_SEGMENT_DISTANCE,
        MAX(vs.$COL_ELAPSED_TIME) $COL_MAX_TRIP_SEGMENT_ELAPSED_TIME,
        MIN(ts.started_time) started_time,
        MAX(ts.stopped_time) stopped_time
    FROM $VIEW_SEGMENTS vs
        INNER JOIN trip_segments ts ON vs.trip_segment_id = ts.id
    group by ts.trip_id;

    """.trimIndent()

}

