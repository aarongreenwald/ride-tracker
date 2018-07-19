package com.greenwald.aaron.ridetracker;

//https://gist.github.com/joshdholtz/4522551

import android.Manifest;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.PolylineOptions;
import com.greenwald.aaron.ridetracker.model.TrackPoint;

public class MapFragment extends Fragment {

    MapView mapView;
    GoogleMap map;
    private IntentFilter intentFilter = new IntentFilter();
    PolylineOptions polylineOptions = new PolylineOptions();


    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        getContext().registerReceiver(receiver, intentFilter);
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("LOCATION_CHANGED")) {
                String point = intent.getStringExtra("point");
                TrackPoint location = TrackPoint.fromString(point);
                addPointToMap(location);
                focusOnLocation(location);
            }
        }
    };

    private void addPointToMap(TrackPoint location) {
        polylineOptions.add(location.getLatLng());
        map.addPolyline(polylineOptions);
    }

    private void focusOnLocation(TrackPoint location) {
        CameraPosition camPos = new CameraPosition.Builder()
                .target(location.getLatLng())
                .zoom(15)
                .build();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(camPos));
    }

    @Override
    public void onPause() {
        getContext().unregisterReceiver(receiver);
        super.onPause();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        polylineOptions.color(Color.RED);
        polylineOptions.jointType(JointType.ROUND);
        intentFilter.addAction("LOCATION_CHANGED");

        View view = inflater.inflate(R.layout.fragment_map, container, false);
        mapView = view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                map = googleMap;
                if (ActivityCompat.checkSelfPermission(getContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(getContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                map.setMyLocationEnabled(true);
                map.getUiSettings().setAllGesturesEnabled(true);
                map.getUiSettings().setMyLocationButtonEnabled(true);
            }
        });


        MapsInitializer.initialize(this.getActivity());
        return view;
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

}