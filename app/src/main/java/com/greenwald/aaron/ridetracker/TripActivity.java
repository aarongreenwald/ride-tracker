package com.greenwald.aaron.ridetracker;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.greenwald.aaron.ridetracker.model.Trip;

public class TripActivity extends AppCompatActivity {

    private long tripId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.tripId = getIntent().getLongExtra("tripId", -1);

        if (tripId == -1) {
            throw new RuntimeException("Cannot open a trip without a valid tripId");
        }

        DataStore ds = new DataStore(getApplicationContext());
        Trip trip = ds.getTripWithDetails(tripId);

        setContentView(R.layout.activity_trip);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(trip.getName());
        setSupportActionBar(toolbar);



        ViewPager viewPager = findViewById(R.id.tripTabsViewPager);
        TripActivityPagerAdapter pagerAdapter = new TripActivityPagerAdapter(super.getSupportFragmentManager(), trip);
        viewPager.setAdapter(pagerAdapter);

        TabLayout tabs = findViewById(R.id.tripTabsLayout);
        tabs.setupWithViewPager(viewPager);
        tabs.getTabAt(0).setText(R.string.stats);
        tabs.getTabAt(1).setText(R.string.map);


        final FloatingActionButton fab = findViewById(R.id.playPauseButton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        onPlayPauseClicked();
                    }
                });

            }
        });
        showCurrentStatus(this);

    }

    private void onPlayPauseClicked() {
        final Context context = TripActivity.this;

        final String TAG = "TRACKER:";
        Log.i(TAG, "starting");
        LocationManager locationManager = (LocationManager) context
                .getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Log.i(TAG, "provider enabled");

            if (needsLocationPermission(context)) {
                Log.i(TAG, "need to request permissions");
                ActivityCompat.requestPermissions(TripActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PackageManager.PERMISSION_GRANTED);
            } else {
                Intent intent = new Intent(this, LocationTrackingService.class);
                intent.putExtra("tripId", this.tripId);
                if (LocationTrackingService.isRunning) {
                    stopService(intent);
                    LocationTrackingService.isRunning = false;
                } else {
                    startService(intent);
                    LocationTrackingService.isRunning = true;
                }

                showCurrentStatus(context);

            }

        } else {
            Log.i(TAG, "no enabled provider");
        }
    }

    private void showCurrentStatus(final Context context) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final FloatingActionButton fab = findViewById(R.id.playPauseButton);
                fab.setImageResource(LocationTrackingService.isRunning ?
                        android.R.drawable.ic_media_pause :
                        android.R.drawable.ic_media_play
                );
            }
        });
    }

    private boolean needsLocationPermission(Context context) {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED;
    }

    private class TripActivityPagerAdapter extends FragmentPagerAdapter {

        private final Trip trip;

        public TripActivityPagerAdapter(FragmentManager supportFragmentManager, Trip trip) {
            super(supportFragmentManager);
            this.trip = trip;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public Fragment getItem(int i) {
            Bundle bundle = new Bundle();
            bundle.putSerializable("trip", this.trip);
            switch (i) {
                case 0: {
                    Fragment fragment = StatsFragment.instantiate(getApplicationContext(), StatsFragment.class.getName());
                    fragment.setArguments(bundle);
                    return fragment;
                }
                case 1: {
                    Fragment fragment = MapFragment.instantiate(getApplicationContext(), MapFragment.class.getName());
                    fragment.setArguments(bundle);
                    return fragment;
                }

                default: return null;
            }
        }

    }
}

