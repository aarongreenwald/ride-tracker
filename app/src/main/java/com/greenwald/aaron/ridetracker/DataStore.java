package com.greenwald.aaron.ridetracker;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

import com.greenwald.aaron.ridetracker.model.Segment;
import com.greenwald.aaron.ridetracker.model.TrackPoint;
import com.greenwald.aaron.ridetracker.model.Trip;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;

class DataStore {

    private final DatabaseHelper db;

    DataStore(Context context) {
        this.db = new DatabaseHelper(context);
    }

    Trip createTrip(String name) {
        Trip trip = new Trip(name);
        Long id = db.addTrip(trip);
        trip.setId(id);
        return trip;

    }

    Segment startTripSegment(Trip trip) {
        Segment segment = new Segment(Date.from(Instant.now()));
        Long id = db.addSegment(trip, segment);
        segment.setId(id);
        return segment;
    }

    void stopTripSegment(Segment segment) {
        db.updateSegment(segment.getId(), Date.from(Instant.now()));
    }

    void recordSegmentPoint(Segment segment, TrackPoint point) {
        //should the timestamp be set here, or does it come as part of the point?
        db.addSegmentPoint(segment, point);
    }

    ArrayList<Trip> getTrips() {
        return db.getTrips();
    }

    public Trip getTrip(Long id) {
        return db.getTrip(id);
    }

    class DatabaseHelper extends SQLiteOpenHelper {

        // db version
        private static final int DATABASE_VERSION = 1;

        // db name
        private static final String DATABASE_NAME = "ride-tracker";

        // tables, fields
        private static final String TABLE_TRIPS = "trips";
        private static final String TABLE_TRIP_SEGMENTS = "trip_segments";
        private static final String TABLE_SEGMENT_POINTS = "segment_points";

        private static final String COL_ID = "id";

        private static final String COL_TRIP_NAME = "todo";
        private static final String COL_TRIP_ID = "trip_id";
        private static final String COL_SEGMENT_STARTED_TIMESTAMP = "started_time";
        private static final String COL_SEGMENT_STOPPED_TIMESTAMP = "stopped_time";

        private static final String COL_TRIP_SEGMENT_ID = "trip_segment_id";
        private static final String COL_LONGITUDE = "longitude";
        private static final String COL_LATITUDE = "latitude";
        private static final String COL_ALTITUDE = "altitude";
        private static final String COL_ACCURACY = "accuracy";
        private static final String COL_TIMESTAMP = "timestamp";


        private static final String CREATE_TABLE_TRIPS = "CREATE TABLE " + TABLE_TRIPS + "("
                + COL_ID + " INTEGER PRIMARY KEY,"
                + COL_TRIP_NAME + " TEXT "
                + ")";

        private static final String CREATE_TABLE_TRIP_SEGMENTS = "CREATE TABLE " + TABLE_TRIP_SEGMENTS + "("
                + COL_ID + " INTEGER PRIMARY KEY,"
                + COL_TRIP_ID + " INTEGER," //FOREIGN KEY
                + COL_SEGMENT_STARTED_TIMESTAMP + " DATETIME,"
                + COL_SEGMENT_STOPPED_TIMESTAMP + " DATETIME"
                + ")";

        private static final String CREATE_TABLE_SEGMENT_POINTS = "CREATE TABLE "
                + TABLE_SEGMENT_POINTS + "(" + COL_ID + " INTEGER PRIMARY KEY,"
                + COL_TRIP_SEGMENT_ID + " INTEGER," //FOREIGN KEY
                + COL_TIMESTAMP + " DATETIME,"
                + COL_LATITUDE + " INTEGER,"
                + COL_LONGITUDE + " INTEGER,"
                + COL_ALTITUDE + " INTEGER,"
                + COL_ACCURACY + " INTEGER"
                + ")";

        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            db.execSQL(CREATE_TABLE_TRIPS);
            db.execSQL(CREATE_TABLE_TRIP_SEGMENTS);
            db.execSQL(CREATE_TABLE_SEGMENT_POINTS);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRIPS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRIP_SEGMENTS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_SEGMENT_POINTS);

            onCreate(db);
        }

        public long addTrip(Trip trip) {
            SQLiteDatabase db = this.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(COL_TRIP_NAME, trip.getName());
            return db.insert(TABLE_TRIPS, null, values);

        }

        public long addSegment(Trip trip, Segment segment) {
            SQLiteDatabase db = this.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(COL_TRIP_ID, trip.getId());
            values.put(COL_SEGMENT_STARTED_TIMESTAMP, segment.getStartedTimestamp().toString());

            return db.insert(TABLE_TRIP_SEGMENTS, null, values);
        }

        public long addSegmentPoint(Segment segment, TrackPoint point) {
            SQLiteDatabase db = this.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(COL_TRIP_SEGMENT_ID, segment.getId());
            values.put(COL_TIMESTAMP, point.dateTime.toString());
            values.put(COL_LATITUDE, point.latitude);
            values.put(COL_LONGITUDE, point.longitude);
            values.put(COL_ALTITUDE, point.altitude);
            values.put(COL_ACCURACY, point.accuracy);

            return db.insert(TABLE_SEGMENT_POINTS, null, values);
        }

        public void updateSegment(long id, Date stoppedTimestamp) {
            SQLiteDatabase db = this.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(COL_SEGMENT_STOPPED_TIMESTAMP, stoppedTimestamp.toString());

            db.update(TABLE_TRIP_SEGMENTS, values, COL_ID + " = ?",
                    new String[] { String.valueOf(id) });
        }

        public ArrayList<Trip> getTrips() {
            ArrayList<Trip> trips = new ArrayList<Trip>();

            String selectQuery = "SELECT  * FROM " + TABLE_TRIPS + " order by " + COL_ID + " desc";


            SQLiteDatabase db = this.getReadableDatabase();
            Cursor c = db.rawQuery(selectQuery, null);

            if (c.moveToFirst()) {
                do {
                    Trip trip = createTripFromCursor(c);
                    trips.add(trip);
                } while (c.moveToNext());
            }

            return trips;
        }

        public Trip getTrip(Long id) {
            SQLiteDatabase db = this.getReadableDatabase();

            String selectQuery = "SELECT  * FROM " + TABLE_TRIPS + " WHERE "
                    + COL_ID + " = " + id;

            Cursor c = db.rawQuery(selectQuery, null);

            if (c != null)
                c.moveToFirst();

            return createTripFromCursor(c);
        }

        @NonNull
        private Trip createTripFromCursor(Cursor cursor) {
            return new Trip(
                cursor.getLong(cursor.getColumnIndex(COL_ID)),
                cursor.getString(cursor.getColumnIndex(COL_TRIP_NAME))
            );
        }
    }
}

