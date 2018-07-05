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

import java.time.Instant;
import java.util.Date;

//https://stackoverflow.com/a/14478281
public class LocationTrackingService extends Service
{
//    public static final String BROADCAST_ACTION = "Hello World";
//    private static final int TWO_MINUTES = 1000 * 60 * 2;
    public LocationManager locationManager;
    public MyLocationListener listener;
//    public Location previousBestLocation = null;

    Intent intent;
//    int counter = 0;

    @Override
    public void onCreate()
    {
        super.onCreate();
//        intent = new Intent(BROADCAST_ACTION);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onStart(Intent intent, int startId)
    {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        listener = new MyLocationListener();
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 500, 0, listener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 0, listener);
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

//    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
//        if (currentBestLocation == null) {
//            // A new location is always better than no location
//            return true;
//        }
//
//        // Check whether the new location fix is newer or older
//        long timeDelta = location.getTime() - currentBestLocation.getTime();
//        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
//        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
//        boolean isNewer = timeDelta > 0;
//
//        // If it's been more than two minutes since the current location, use the new location
//        // because the user has likely moved
//        if (isSignificantlyNewer) {
//            return true;
//            // If the new location is more than two minutes older, it must be worse
//        } else if (isSignificantlyOlder) {
//            return false;
//        }
//
//        // Check whether the new location fix is more or less accurate
//        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
//        boolean isLessAccurate = accuracyDelta > 0;
//        boolean isMoreAccurate = accuracyDelta < 0;
//        boolean isSignificantlyLessAccurate = accuracyDelta > 200;
//
//        // Check if the old and new location are from the same provider
//        boolean isFromSameProvider = isSameProvider(location.getProvider(),
//                currentBestLocation.getProvider());
//
//        // Determine location quality using a combination of timeliness and accuracy
//        if (isMoreAccurate) {
//            return true;
//        } else if (isNewer && !isLessAccurate) {
//            return true;
//        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
//            return true;
//        }
//        return false;
//    }



    /** Checks whether two providers are the same */
//    private boolean isSameProvider(String provider1, String provider2) {
//        if (provider1 == null) {
//            return provider2 == null;
//        }
//        return provider1.equals(provider2);
//    }



    @Override
    public void onDestroy() {
        // handler.removeCallbacks(sendUpdatesToUI);
        super.onDestroy();
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

        public void onLocationChanged(final Location loc)
        {
            TrackPoint trackPoint = new TrackPoint(loc.getLatitude(), loc.getLongitude(), loc.getAccuracy(), Date.from(Instant.now()));
            Log.i("AGGG", trackPoint.toString());
//            intent.putExtra("Latitude", loc.getLatitude());
//            intent.putExtra("Longitude", loc.getLongitude());
//            intent.putExtra("Provider", loc.getProvider());
//            sendBroadcast(intent);
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