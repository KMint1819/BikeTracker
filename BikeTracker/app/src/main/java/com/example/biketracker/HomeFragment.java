package com.example.biketracker;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Objects;

public class HomeFragment extends Fragment implements OnMapReadyCallback {
    private static final String TAG = "HomeFragment";
    private TextView textPosition = null;
    private TextView statusBar = null;
    private SupportMapFragment mapFragment = null;
    private String ip;
    private int port;
    private Socket socket = null;
    private BufferedReader reader = null;
    private BufferedWriter writer = null;

    private GoogleMap mMap = null;
    private ArrayList<LatLng> spots = null;
    private Marker curMarker = null;
    HomeFragment(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Created!");
        funcThread.start();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "Creating view...");
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        Log.d(TAG, "View finished.");
        textPosition = view.findViewById(R.id.txt_position);
        statusBar = view.findViewById(R.id.server_status_bar);
        Button btnClear = view.findViewById(R.id.btn_clear);
        btnClear.setOnClickListener(clearListener);

        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            Log.d(TAG, "mapFragment is not null...");
            mapFragment.getMapAsync(this);
            Log.d(TAG, "getMapAsync called...");
        } else {
            Log.e(TAG, "mapFragment is null!!");
        }
//        while (!map.ready()) {
//            Log.d(TAG, "Waiting for map ready...");
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
        return view;
    }

    private View.OnClickListener clearListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            textPosition.setText("");
        }
    };
    private Thread funcThread = new Thread(new Runnable() {
        @SuppressLint("DefaultLocale")
        @Override
        public void run() {
            connect();
            startRequest();
            JsonObject rcv_obj;
            int idx = 0;
            while (true) {
                connect();
                GetRequestExe getRequestExe = new GetRequestExe();
                Thread t = new Thread(getRequestExe);
                Thread timer = new Thread(new Timer(1000));
                t.start();
                timer.start();
                while (timer.isAlive()) {
                    if (!t.isAlive()) {
                        Log.d(TAG, "Get request finished!");
                        break;
                    }
                }
                if (t.isAlive()) {
                    t.interrupt();
                    timer.interrupt();
                    Log.d(TAG, "GET request timeout!");
                    textPosition.append(String.format("%d -------TIMEOUT-------\n", idx++));
                    continue;
                }
                timer.interrupt();
                rcv_obj = getRequestExe.getObject();
                JsonObject position = rcv_obj.getAsJsonObject("position");
                final String longitude = position.get("longitude").toString();
                final String latitude = position.get("latitude").toString();
                Log.i(TAG, String.format("Longitude: <%s>, latitude: <%s>", longitude, latitude));

                Objects.requireNonNull(getActivity()).runOnUiThread(new PositionUpdater(longitude, latitude, idx++));
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    });

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "Map is ready!!");
        mMap = googleMap;
        if(googleMap != null){
            Log.d(TAG, "googleMap is not null...");
            mMap = googleMap;
        }
        else {
            Log.e(TAG, "googleMap is null!!");
        }
    }
    private void newMarker(LatLng latlng) {
        if(mMap == null) {
            Log.e(TAG, "new Marker mMap is null!");
        }
        else {
            if(curMarker != null) {
                curMarker.remove();
            }
            curMarker = mMap.addMarker(new MarkerOptions().position(latlng));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
        }
    }
    class PositionUpdater implements Runnable {
        private String latitude;
        private String longitude;
        private int idx;

        PositionUpdater(String longitude, String latitude, int idx) {
            this.longitude = longitude;
            this.latitude = latitude;
            this.idx = idx;
        }

        @SuppressLint("DefaultLocale")
        @Override
        public void run() {
            textPosition.setText(String.format("%d. (%s, %s)", idx++, longitude, latitude));
            newMarker(new LatLng(Float.parseFloat(longitude), Float.parseFloat(latitude)));
        }
    }

    private void connect() {
        Log.i(TAG, "Connecting to " + ip + ":" + port);
        try {
            socket = new Socket(ip, port);
        } catch (IOException e) {
            Log.e(TAG, "Cannot connect!");
            Log.e(TAG, e.toString());
        }
        Log.i(TAG, "Successfully connected!");
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();

        }
        Log.i(TAG, "IO stream created successfully!");
    }

    private void startRequest() {
        Log.i(TAG, "Requesting start request...");
        try {
            String request = new Request(RequestType.START).toJson();
            Log.i(TAG, "Sending:\n" + request);
            writer.write(request);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String rcv_str = "";
        while (true) {
            try {
                rcv_str = reader.readLine();
            } catch (IOException e) {
                Log.e(TAG, String.format("Received string <%s> error!", rcv_str));
                e.printStackTrace();
                continue;
            }
            if (rcv_str != null && !rcv_str.equals("")) {
                break;
            }
        }
        Log.i(TAG, "START receives " + rcv_str);
        Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                statusBar.setText(R.string.server_ready);
            }
        });
    }

    private class Timer implements Runnable {
        int timeout;

        Timer(int timeout) {
            this.timeout = timeout;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(timeout);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private class GetRequestExe implements Runnable {
        private JsonObject jsonObject = null;

        @Override
        public void run() {
            Log.d(TAG, "Sending get request...");
            try {
                String request = new Request(RequestType.GET).toJson();
                Log.i(TAG, "Sending:\n" + request);
                writer.write(request);
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            String rcv_str = "";
            while (true) {
                try {
                    Log.d(TAG, "Receving GET...");
                    rcv_str = reader.readLine();
                } catch (IOException e) {
                    Log.e(TAG, String.format("Received string <%s> error!", rcv_str));
                    e.printStackTrace();
                    continue;
                }
                if (rcv_str == null || rcv_str.equals("")) {
                    Log.d(TAG, "Invalid GET response: " + rcv_str);
                } else {
                    break;
                }
            }
            Log.d(TAG, "GET Receives " + rcv_str);
            jsonObject = new Gson().fromJson(rcv_str, JsonObject.class);
        }

        JsonObject getObject() {
            return jsonObject;
        }
    }
}
