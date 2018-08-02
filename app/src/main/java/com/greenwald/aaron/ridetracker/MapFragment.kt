package com.greenwald.aaron.ridetracker

//https://gist.github.com/joshdholtz/4522551

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
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
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.greenwald.aaron.ridetracker.model.SegmentPoint
import com.greenwald.aaron.ridetracker.model.Trip

import java.util.ArrayList

class MapFragment : Fragment() {

    internal lateinit var mapView: MapView
    internal lateinit var map: GoogleMap
    private val intentFilter = IntentFilter()
    internal var polylineOptions = PolylineOptions()

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == "LOCATION_CHANGED") {
                val point = intent.getStringExtra("point")
                val location = SegmentPoint.fromString(point)
                addPointToMap(location)
                focusOnLocation(location)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
        context!!.registerReceiver(receiver, intentFilter)
    }

    private fun addPointToMap(location: SegmentPoint) {
        polylineOptions.add(location.latLng)
        map.addPolyline(polylineOptions)
    }

    private fun focusOnLocation(location: SegmentPoint?) {
        val camPos = CameraPosition.Builder()
                .target(location!!.latLng)
                .zoom(15f)
                .build()
        map.animateCamera(CameraUpdateFactory.newCameraPosition(camPos))
    }

    override fun onPause() {
        context!!.unregisterReceiver(receiver)
        super.onPause()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        polylineOptions.color(Color.RED)
        polylineOptions.jointType(JointType.ROUND)
        intentFilter.addAction("LOCATION_CHANGED")

        val trip = arguments!!.getSerializable("trip") as Trip
        val locations = trip.getAllLocations()
        polylineOptions.addAll(locations)


        val view = inflater.inflate(R.layout.fragment_map, container, false)
        mapView = view.findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)

        mapView.getMapAsync(OnMapReadyCallback { googleMap ->
            map = googleMap
            if (ActivityCompat.checkSelfPermission(context!!,
                            Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context!!,
                            Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return@OnMapReadyCallback
            }
            map.isMyLocationEnabled = true
            map.uiSettings.setAllGesturesEnabled(true)
            map.uiSettings.isMyLocationButtonEnabled = true
            map.addPolyline(polylineOptions)
            val startingPoint = trip.startingPoint
            if (startingPoint != null) {
                focusOnLocation(startingPoint)
            }
        })


        MapsInitializer.initialize(this.activity!!)
        return view
    }


    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

}