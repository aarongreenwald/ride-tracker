package com.greenwald.aaron.ridetracker

//https://gist.github.com/joshdholtz/4522551

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.greenwald.aaron.ridetracker.model.SegmentId
import com.greenwald.aaron.ridetracker.model.SegmentPoint


class MapFragment : Fragment() {

    private lateinit var mapView: MapView
    private lateinit var map: GoogleMap
    private val intentFilter = IntentFilter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        intentFilter.addAction("LOCATION_CHANGED")

        val view = inflater.inflate(R.layout.fragment_map, container, false)
        mapView = view.findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)

        updateData(activity!!)

        return view
    }

    private fun updateData(activity: Activity) {
        AsyncTask.execute {
            val ds = DataStore(activity.applicationContext)
            val trip = ds.getTripWithDetails(arguments!!.getLong("tripId"))

            val segmentPolylines: List<Pair<SegmentId, PolylineOptions>> = trip.segments.map { segment ->
                val locations = segment.segmentPoints.map(SegmentPoint::latLng)

                val speedFactor = segment.maxSpeed.value / trip.maxSpeed.value
                val polyline = PolylineOptions()
                val colorCode = colorForSpeedFactor(speedFactor)
                polyline.color(Color.parseColor(colorCode))
                polyline.jointType(JointType.ROUND)
                polyline.clickable(true)
                polyline.addAll(locations)
                Pair(segment.id, polyline)
            }

            activity.runOnUiThread {
                mapView.getMapAsync(OnMapReadyCallback { googleMap ->
                    map = googleMap
                    if (ActivityCompat.checkSelfPermission(context!!,
                                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context!!,
                                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return@OnMapReadyCallback
                    }
                    map.isMyLocationEnabled = true
                    map.uiSettings.setAllGesturesEnabled(true)
                    map.uiSettings.isZoomControlsEnabled = true
                    map.uiSettings.isMyLocationButtonEnabled = true

                    segmentPolylines.forEach { segment ->
                        map.addPolyline(segment.second).tag = segment.first
                    }

                    map.setOnPolylineClickListener { polyline ->
                        val segment = ds.getSegment(polyline.tag as SegmentId).toString()
                        showSegmentDialog(segment)
                    }

                    val builder = LatLngBounds.builder()
                    val locations = trip.getAllPoints()
                    if (locations.isNotEmpty()) {
                        locations.forEach { latLng -> builder.include(latLng) }
                        map.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 200))
                    }
                })

                MapsInitializer.initialize(this.activity!!)
            }


        }
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        (activity as TripActivity).onDataUpdated { tripActivity -> updateData(tripActivity) }
    }

    private fun colorForSpeedFactor(speedFactor: Double): String {
        val red = (speedFactor * 255).toInt().toString(16).padStart(2, '0')
        val green = ((1 - speedFactor) * 255).toInt().toString(16).padStart(2, '0')
        return """#$red${green}00"""
    }

    private fun showSegmentDialog(segment: String) {
        val alertDialog = AlertDialog.Builder(context).create()
        alertDialog.setTitle("Trip Segment")
        alertDialog.setMessage(segment)
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK"
        ) { dialog, _ -> dialog.dismiss() }
        alertDialog.show()
    }


    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

}