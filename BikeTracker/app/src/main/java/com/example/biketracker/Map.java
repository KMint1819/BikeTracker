package com.example.biketracker;

import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

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

    public boolean ready() {
        return mapReady;
    }

    Runnable clear = new Runnable() {
        @Override
        public void run() {
            mMap.clear();
            spots.clear();
        }
    };

    public void addTrack(LatLng pre, LatLng now) {
        Log.i(TAG, String.format("Adding two polyline: (%f, %f), (%f, %f)",
                pre.latitude, pre.longitude,
                now.latitude, now.longitude));
        mMap.addPolyline(new PolylineOptions()
                .add(pre)
                .add(now)
                .visible(true));
        spots.add(pre);
        spots.add(now);
    }

    public void addTrack(LatLng latlng) {
        if (spots.size() == 0) {
            spots.add(latlng);
        } else {
            Log.i(TAG, String.format("Adding Polyline: (%f, %f), (%f, %f)",
                    spots.lastElement().latitude, spots.lastElement().longitude,
                    latlng.latitude, latlng.longitude));
            mMap.addPolyline(new PolylineOptions()
                    .add(spots.lastElement())
                    .add(latlng)
                    .visible(true));
            spots.add(latlng);

        }
    }

    public void clearMarker() {
        if (curMarker != null) {
            curMarker.remove();
        }
    }

    public void newMarker(LatLng latlng) {
        if (mMap == null) {
            Log.e(TAG, "new Marker mMap is null!");
        } else {
            curMarker = mMap.addMarker(new MarkerOptions().position(latlng));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
            updateCameraZoom();
        }
    }

    private void updateCameraZoom() {
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15.0f));
    }

//    private static double haversine(LatLng latlng1, LatLng latlng2) {
//        double lat1 = latlng1.latitude;
//        double lon1 = latlng1.longitude;
//        double lat2 = latlng2.latitude;
//        double lon2 = latlng2.longitude;
//        final int R = 6371; // Radius of the earth
//
//        double latDistance = Math.toRadians(lat2 - lat1);
//        double lonDistance = Math.toRadians(lon2 - lon1);
//        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
//                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
//                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
//        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
//        double distance = R * c * 1000; // convert to meters
//        return distance;
//    }

}
