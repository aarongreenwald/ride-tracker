package com.greenwald.aaron.ridetracker

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.constraint.ConstraintLayout
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.TabLayout
import android.support.v4.app.*
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.ImageButton

import com.greenwald.aaron.ridetracker.model.TripId

class TripActivity : AppCompatActivity() {

    private var tripId: Long = 0
    private val dataUpdatedListeners: MutableList<(activity: TripActivity) -> Unit> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.tripId = intent.getLongExtra("tripId", -1)
        val tripName = intent.getStringExtra("tripName")

        if (tripId == -1L) {
            throw RuntimeException("Cannot open a trip without a valid tripId")
        }

        setContentView(R.layout.activity_trip)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = tripName
        setSupportActionBar(toolbar)

        val fab = findViewById<FloatingActionButton>(R.id.playPauseButton)
        fab.setOnClickListener { AsyncTask.execute { onPlayPauseClicked() } }
        fab.setImageResource(android.R.drawable.ic_media_play)

        val bigButton = findViewById<ImageButton>(R.id.bigPauseButton)
        bigButton.setOnClickListener { AsyncTask.execute { onPlayPauseClicked() } }

        updateButtonStatus()
        updateData()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        this.tripId = intent!!.getLongExtra("tripId", -1)

        if (tripId == -1L) {
            throw RuntimeException("Cannot open a trip without a valid tripId")
        }

        updateButtonStatus()
        updateData()
    }

    private fun updateData() {

        val viewPager = findViewById<ViewPager>(R.id.tripTabsViewPager)
        val pagerAdapter = TripActivityPagerAdapter(super.getSupportFragmentManager(), tripId)
        viewPager.adapter = pagerAdapter

        val tabs = findViewById<TabLayout>(R.id.tripTabsLayout)
        tabs.setupWithViewPager(viewPager)
        tabs.getTabAt(0)!!.setText(R.string.stats)
        tabs.getTabAt(1)!!.setText(R.string.map)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (!needsLocationPermission(this)) {
            onPlayPauseClicked()
        }
    }

    private fun startTracking() {
        val intent = Intent(this, LocationTrackingService::class.java)
        intent.putExtra("tripId", this.tripId)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun onPlayPauseClicked() {
        val locationManager = this
                .getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

            if (needsLocationPermission(this)) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), PackageManager.PERMISSION_GRANTED)
            } else {
                if (LocationTrackingService.isRunning) {
                    val intent = Intent(this, LocationTrackingService::class.java)
                    intent.putExtra("tripId", this.tripId)
                    stopService(intent)
                    LocationTrackingService.isRunning = false
                    LocationTrackingService.recordingTripId = null

                    runOnUiThread {
                        //this 100ms delay gives the service time to completely stop, which
                        //is necessary because stopService is async but the data need to be
                        //stored in the db before it's fetched again.
                        //this is an EMBARRASSINGLY bad workaround for this problem. How do
                        //I get notified when the service is finished stopping?
                        val handler = Handler()
                        handler.postDelayed(Runnable {
                            dataUpdatedListeners.forEach { it(this) }
                        }, 100)
                    }

                } else {
                    startTracking()
                    LocationTrackingService.isRunning = true
                    LocationTrackingService.recordingTripId = tripId
                }

                updateButtonStatus()
            }

        }
    }

    private fun updateButtonStatus() {
        runOnUiThread {
            val fab = findViewById<FloatingActionButton>(R.id.playPauseButton)
            val overlayButton = findViewById<ConstraintLayout>(R.id.pauseOverlay)
            val isAnotherTripActive = LocationTrackingService.recordingTripId != null &&
                    LocationTrackingService.recordingTripId != this.tripId
            val shouldShowPlayFab = !isAnotherTripActive && !LocationTrackingService.isRunning
            val shouldShowPauseOverlay = !isAnotherTripActive && LocationTrackingService.isRunning

            if (shouldShowPlayFab)
                fab.show()
            else
                fab.hide()

            overlayButton.visibility = if (shouldShowPauseOverlay) View.VISIBLE else View.GONE
        }
    }

    private fun needsLocationPermission(context: Context): Boolean {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
    }

    fun onDataUpdated(func: (activity: TripActivity) -> Unit) {
        this.dataUpdatedListeners.add(func)
    }

    private inner class TripActivityPagerAdapter(supportFragmentManager: FragmentManager, private val tripId: TripId) : FragmentStatePagerAdapter(supportFragmentManager) {

        override fun getCount() = 2

        override fun getItemPosition(`object`: Any): Int {
            //this forces the tabs to update way too much. this is NOT GOOD. find a better
            //way to get the tabs to update when the data is updated.
            return PagerAdapter.POSITION_NONE
        }

        override fun getItem(i: Int): Fragment? {
            val bundle = Bundle()
            bundle.putLong("tripId", tripId)
            return when (i) {
                0 -> {
                    val fragment = StatsFragment()
                    fragment.arguments = bundle
                    fragment
                }

                1 -> {
                    val fragment = MapFragment()
                    fragment.arguments = bundle
                    fragment
                }

                else -> null
            }
        }

    }
}

