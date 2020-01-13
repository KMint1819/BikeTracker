package com.example.biketracker;

import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.Stack;

public class Map implements OnMapReadyCallback {
    private String TAG = "Map";
    private GoogleMap mMap = null;
//    private ArrayList<LatLng> spots = null;
    private Stack<LatLng> spots = null;
    private boolean mapReady = false;
    private Marker curMarker = null;

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "Map is ready!!");
        spots = new Stack<>();
        if (googleMap != null) {
            Log.d(TAG, "googleMap is not null...");
            mMap = googleMap;
            mapReady = true;
        } else {
            Log.e(TAG, "googleMap is null!!");
        }
    }

    boolean ready() {
        return mapReady;
    }

    public void clear() {
        mMap.clear();
        spots.clear();
    }

    public void addTrack(LatLng latlng) {
        if(spots.size() == 0) {
            spots.add(latlng);
            mMap.addPolyline(new PolylineOptions().add(spots.lastElement()));
        }
    }

    public void newMarker(LatLng latlng) {
        if (mMap == null) {
            Log.e(TAG, "new Marker mMap is null!");
        } else {
            if (curMarker != null) {
                curMarker.remove();
            }
            curMarker = mMap.addMarker(new MarkerOptions().position(latlng));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
        }
    }

}
