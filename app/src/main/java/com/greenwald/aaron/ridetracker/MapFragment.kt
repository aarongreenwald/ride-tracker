package com.greenwald.aaron.ridetracker

//https://gist.github.com/joshdholtz/4522551

import android.Manifest
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
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.greenwald.aaron.ridetracker.model.SegmentPoint
import com.greenwald.aaron.ridetracker.model.Trip

class MapFragment : Fragment() {

    private lateinit var mapView: MapView
    private lateinit var map: GoogleMap
    private val intentFilter = IntentFilter()
//    private var polylineOptions = PolylineOptions()

//    private val receiver = object : BroadcastReceiver() {
//        override fun onReceive(context: Context, intent: Intent) {
//            if (intent.action == "LOCATION_CHANGED") {
//                val point = intent.getStringExtra("point")
//                val location = SegmentPoint.fromString(point)
//                addPointToMap(location)
//                focusOnLocation(location)
//            }
//        }
//    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
//        context!!.registerReceiver(receiver, intentFilter)
    }

//    private fun addPointToMap(location: SegmentPoint) {
//        polylineOptions.add(location.latLng)
//        map.addPolyline(polylineOptions)
//    }

//    private fun focusOnLocation(location: SegmentPoint?) {
//        val camPos = CameraPosition.Builder()
//                .target(location!!.latLng)
//                .zoom(15f)
//                .build()
//        map.animateCamera(CameraUpdateFactory.newCameraPosition(camPos))
//    }

    override fun onPause() {
//        context!!.unregisterReceiver(receiver)
        super.onPause()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        intentFilter.addAction("LOCATION_CHANGED")

        val trip =  DataStore(activity!!.applicationContext).getTripWithDetails(arguments!!.getLong("tripId"))

        val polylines: List<PolylineOptions> = trip.segments.map { segment ->
            val locations = segment.segmentPoints.map(SegmentPoint::latLng)
            val polyline = PolylineOptions()
            polyline.color(Color.RED)
            polyline.jointType(JointType.ROUND)
            polyline.addAll(locations)
            polyline
        }

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
            map.uiSettings.isZoomControlsEnabled = true
            map.uiSettings.isMyLocationButtonEnabled = true

            polylines.forEach { polyline -> map.addPolyline(polyline) }

            val builder = LatLngBounds.builder()
            val locations = trip.getAllPoints()
            if (locations.isNotEmpty()) {
                locations.forEach { latLng -> builder.include(latLng) }
                map.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 200))
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