package com.greenwald.aaron.ridetracker

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.AsyncTask
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.TabLayout
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log

import com.greenwald.aaron.ridetracker.model.Trip

class TripActivity : AppCompatActivity() {

    private var tripId: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.tripId = intent.getLongExtra("tripId", -1)

        if (tripId == -1L) {
            throw RuntimeException("Cannot open a trip without a valid tripId")
        }

        val ds = DataStore(applicationContext)
        val trip = ds.getTripWithDetails(tripId)

        setContentView(R.layout.activity_trip)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = trip.name
        setSupportActionBar(toolbar)


        val viewPager = findViewById<ViewPager>(R.id.tripTabsViewPager)
        val pagerAdapter = TripActivityPagerAdapter(super.getSupportFragmentManager(), trip)
        viewPager.adapter = pagerAdapter

        val tabs = findViewById<TabLayout>(R.id.tripTabsLayout)
        tabs.setupWithViewPager(viewPager)
        tabs.getTabAt(0)!!.setText(R.string.stats)
        tabs.getTabAt(1)!!.setText(R.string.map)


        val fab = findViewById<FloatingActionButton>(R.id.playPauseButton)
        fab.setOnClickListener { AsyncTask.execute { onPlayPauseClicked() } }
        showCurrentStatus(this)

    }

    private fun onPlayPauseClicked() {
        val context = this@TripActivity

        val TAG = "TRACKER:"
        Log.i(TAG, "starting")
        val locationManager = context
                .getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Log.i(TAG, "provider enabled")

            if (needsLocationPermission(context)) {
                Log.i(TAG, "need to request permissions")
                ActivityCompat.requestPermissions(this@TripActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), PackageManager.PERMISSION_GRANTED)
            } else {
                val intent = Intent(this, LocationTrackingService::class.java)
                intent.putExtra("tripId", this.tripId)
                if (LocationTrackingService.isRunning) {
                    stopService(intent)
                    LocationTrackingService.isRunning = false
                    LocationTrackingService.recordingTripId = null
                } else {
                    startService(intent)
                    LocationTrackingService.isRunning = true
                    LocationTrackingService.recordingTripId = tripId
                }

                showCurrentStatus(context)

            }

        } else {
            Log.i(TAG, "no enabled provider")
        }
    }

    private fun showCurrentStatus(context: Context) {
        runOnUiThread {
            val fab = findViewById<FloatingActionButton>(R.id.playPauseButton)
            val shouldShowPlayPauseButton = LocationTrackingService.recordingTripId == null ||
                    LocationTrackingService.recordingTripId == tripId

            val icon = if (LocationTrackingService.isRunning)
                android.R.drawable.ic_media_pause
            else
                android.R.drawable.ic_media_play

            if (!shouldShowPlayPauseButton)
                fab.hide()
            else
                fab.setImageResource(icon)



        }
    }

    private fun needsLocationPermission(context: Context): Boolean {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
    }

    private inner class TripActivityPagerAdapter(supportFragmentManager: FragmentManager, private val trip: Trip) : FragmentPagerAdapter(supportFragmentManager) {

        override fun getCount() = 2

        override fun getItem(i: Int): Fragment? {
            val bundle = Bundle()
            bundle.putSerializable("trip", this.trip)
            return when (i) {
                0 -> {
                    val fragment = StatsFragment()
                    fragment.setArguments(bundle)
                    fragment
                }

                1 -> {
                    val fragment = MapFragment()
                    fragment.setArguments(bundle)
                    fragment
                }

                else -> null
            }
        }

    }
}

