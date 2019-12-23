package com.example.biketracker;

import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class Map implements OnMapReadyCallback {
    private String TAG = "Map";
    private GoogleMap mMap = null;
    private ArrayList<LatLng> spots = null;
    private boolean mapReady = false;

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "Map is ready!!");
        spots = new ArrayList<>();
        if(googleMap != null){
            Log.d(TAG, "googleMap is not null...");
            mMap = googleMap;
            mapReady = true;
        }
        else {
            Log.e(TAG, "googleMap is null!!");
        }
    }
    boolean ready() {
        return mapReady;
    }
    public void addMarker(LatLng latlng) {
        mMap.addMarker(new MarkerOptions().position(latlng));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
    }

    public void newMarker(LatLng latlng) {
        while(!mapReady) {
            Log.d(TAG, "Waiting until map is ready...");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        mMap.addMarker(new MarkerOptions().position(latlng));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
    }
}
