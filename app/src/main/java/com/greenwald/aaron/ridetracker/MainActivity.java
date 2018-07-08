package com.greenwald.aaron.ridetracker;

import android.Manifest;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        trackLocation();
                    }
                });

            }
        });

    }

    private void trackLocation() {
        final Context context = MainActivity.this;

        final String TAG = "TRACKER:";
        Log.i(TAG, "starting");
        LocationManager locationManager = (LocationManager) context
                .getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Log.i(TAG, "provider enabled");

            LocationListener listener = getLocationListener(TAG);

            if (needsLocationPermission(context)) {
                Log.i(TAG, "need to request permissions");
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PackageManager.PERMISSION_GRANTED);
            } else {
                Intent intent = new Intent(this, LocationTrackingService.class);
                if (LocationTrackingService.isRunning) {
                    stopService(intent);
                    LocationTrackingService.isRunning = false;
                } else {
                    startService(intent);
                    LocationTrackingService.isRunning = true;
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
                        fab.setImageResource(LocationTrackingService.isRunning ?
                                android.R.drawable.ic_media_pause :
                                android.R.drawable.ic_media_play
                        );

                        String text = String.format("Tracking %s", LocationTrackingService.isRunning ? "on" : "paused");
                        Toast toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
                        toast.show();
                    }
                });

            }

        } else {
            Log.i(TAG, "no enabled provider");
        }
    }

    private boolean needsLocationPermission(Context context) {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED;
    }

    @NonNull
    private LocationListener getLocationListener(final String TAG) {
        return new LocationListener() {

                @Override
                public void onLocationChanged(Location location) {
                    logLocation(location, TAG);
                }

                @Override
                public void onProviderDisabled(String arg0) {
                }

                @Override
                public void onProviderEnabled(String arg0) {
                }

                @Override
                public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
                }

            };
    }

    private void logLocation(Location location, String TAG) {
        double precision = Math.pow(10, 6);
        double valueLatitude = ((int) (precision * location
                .getLatitude())) / precision;
        double valueLongitude = ((int) (precision * location
                .getLongitude())) / precision;
        Log.i(TAG, "onLocationChanged");
        Log.v(TAG, "LAT: " + valueLatitude + " & LONG: " + valueLongitude);
    }

}

