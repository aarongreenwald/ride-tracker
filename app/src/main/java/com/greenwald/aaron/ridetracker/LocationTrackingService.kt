package com.greenwald.aaron.ridetracker

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Toast

import com.greenwald.aaron.ridetracker.model.Meters
import com.greenwald.aaron.ridetracker.model.Milliseconds
import com.greenwald.aaron.ridetracker.model.Segment
import com.greenwald.aaron.ridetracker.model.SegmentPoint

import java.util.Date

//https://stackoverflow.com/a/14478281
class LocationTrackingService : Service() {
    private lateinit var locationManager: LocationManager
    private lateinit var listener: MyLocationListener
    private var ds: DataStore? = null
    private var segment: Segment? = null

    @SuppressLint("MissingPermission")
    override fun onStart(intent: Intent, startId: Int) {
        this.ds = DataStore(applicationContext)

        val tripId = intent.getLongExtra("tripId", -1)
        if (tripId == -1L) {
            throw RuntimeException("Cannot track location without a valid tripId")
        }
        val trip = ds!!.getTrip(tripId)

        this.segment = ds!!.startTripSegment(trip)
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        listener = MyLocationListener(this.ds!!, this.segment!!)
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 0f, listener)

        val intent = Intent(this, TripActivity::class.java)
        intent.putExtra("tripId", tripId)
        val pendingIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val notification = Notification.Builder(this)
            .setContentTitle(getText(R.string.notification_title))
//            .setContentText(getText(R.string.notification_message))
            .setSmallIcon(R.drawable.ic_motorcycle)
            .setContentIntent(pendingIntent)
//            .setTicker(getText(R.string.ticker_text))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val id = "whatever id"
            val channel = NotificationChannel(id, name, importance)
            channel.description = descriptionText
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)

            notification.setChannelId(channel.id)
        }

        startForeground(1, notification.build())
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }


    override fun onDestroy() {
        super.onDestroy()
        this.ds!!.stopTripSegment(this.segment!!)
        Log.v("STOP_SERVICE", "DONE")
        locationManager.removeUpdates(listener)
    }

    inner class MyLocationListener internal constructor(private val ds: DataStore, private val segment: Segment) : LocationListener {
        private var previousLocation: SegmentPoint? = null

        override fun onLocationChanged(loc: Location) {
            val results = FloatArray(3)
            if (this.previousLocation != null) {
                Location.distanceBetween(this.previousLocation!!.latitude, this.previousLocation!!.longitude, loc.latitude, loc.longitude, results)
            }
            val now = Date()
            val altitudeChange = if (this.previousLocation != null) (loc.altitude - this.previousLocation!!.altitude.value) else 0.0
            val elapsedTime = if (this.previousLocation != null) Milliseconds(now.time - this.previousLocation!!.dateTime.time) else Milliseconds(0)

            val segmentPoint = SegmentPoint(loc.latitude,
                    loc.longitude,
                    loc.accuracy.toDouble(),
                    Date(),
                    Meters(loc.altitude),
                    Meters(altitudeChange),
                    elapsedTime,
                    Meters(results[0].toDouble())
            )

            Log.i("AGGG", segmentPoint.toString())
            this.previousLocation = segmentPoint
            this.ds.recordSegmentPoint(this.segment, segmentPoint)
            val intent = Intent()
            intent.putExtra("point", segmentPoint.toString())
            intent.action = "LOCATION_CHANGED"
            sendBroadcast(intent)
        }

        override fun onProviderDisabled(provider: String) {
            val message = String.format("%s DISABLED", provider.toUpperCase())
            showToast(message)
        }

        private fun showToast(message: String) {
            Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
        }


        override fun onProviderEnabled(provider: String) {
            val message = String.format("%s ENABLED", provider.toUpperCase())
            showToast(message)
        }


        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
            showToast("Status changed: $status")
        }

    }

    companion object {
        internal var isRunning = false
        internal var recordingTripId: Long? = null

        fun performOnBackgroundThread(runnable: Runnable): Thread {
            val t = object : Thread() {
                override fun run() {
                    runnable.run()
                }
            }
            t.start()
            return t
        }
    }
}