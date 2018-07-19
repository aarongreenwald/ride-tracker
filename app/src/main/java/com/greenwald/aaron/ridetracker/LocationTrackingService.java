package com.greenwald.aaron.ridetracker;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.greenwald.aaron.ridetracker.model.Segment;
import com.greenwald.aaron.ridetracker.model.TrackPoint;
import com.greenwald.aaron.ridetracker.model.Trip;

import java.time.Instant;
import java.util.Date;
import java.util.LinkedList;

//https://stackoverflow.com/a/14478281
public class LocationTrackingService extends Service
{
    public LocationManager locationManager;
    public MyLocationListener listener;
    static boolean isRunning = false;
    private DataStore ds;
    private Trip trip;
    private Segment segment;

    @SuppressLint("MissingPermission")
    @Override
    public void onStart(Intent intent, int startId)
    {
        this.ds = new DataStore(getApplicationContext());
        //For now every segment is a new trip
        this.trip = ds.createTrip("Some Trip");
        this.segment = ds.startTripSegment(trip);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        listener = new MyLocationListener(this.ds, this.segment);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 0, listener);
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        this.ds.stopTripSegment(this.segment);
        Log.v("STOP_SERVICE", "DONE");
        locationManager.removeUpdates(listener);
    }

    public static Thread performOnBackgroundThread(final Runnable runnable) {
        final Thread t = new Thread() {
            @Override
            public void run() {
                runnable.run();
            }
        };
        t.start();
        return t;
    }

    public class MyLocationListener implements LocationListener
    {

        private final DataStore ds;
        private final Segment segment;
        LinkedList<TrackPoint> track = new LinkedList<TrackPoint>();

        public MyLocationListener(DataStore ds, Segment segment) {
            this.ds = ds;
            this.segment = segment;
        }

        public void onLocationChanged(final Location loc)
        {
            TrackPoint trackPoint = new TrackPoint(loc.getLatitude(), loc.getLongitude(), loc.getAccuracy(), Date.from(Instant.now()));
            track.add(trackPoint);
            Log.i("AGGG", trackPoint.toString());
            this.ds.recordSegmentPoint(this.segment, trackPoint);
            Intent intent = new Intent();
            intent.putExtra("point", trackPoint.toString());
            intent.setAction("LOCATION_CHANGED");
            sendBroadcast(intent);
        }

        public void onProviderDisabled(String provider)
        {
            String message = String.format("%s DISABLED", provider.toUpperCase());
            showToast(message);
        }

        private void showToast(String message) {
            Toast.makeText( getApplicationContext(), message, Toast.LENGTH_SHORT ).show();
        }


        public void onProviderEnabled(String provider)
        {
            String message = String.format("%s ENABLED", provider.toUpperCase());
            showToast(message);
        }


        public void onStatusChanged(String provider, int status, Bundle extras)
        {
            showToast("Status changed: " + status);
        }

    }
}