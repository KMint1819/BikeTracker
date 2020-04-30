package com.example.biketracker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class HomeFragment extends MFragment {
    private static final String TAG = "HomeFragment";
    private TextView textPosition = null;
    private TextView statusBar = null;
    private TextView img_mapCover = null;
    private TextView statusColor = null;
    private Button btnStart = null;
    private SupportMapFragment mapFragment = null;
    //
    private String ip;
    private int port;

    private boolean startSign = false;
    private Thread funcThread = null;
    private Map map = null;
    private boolean bikeMoved = false;
    private LatLng firstMoved = null;
    private Context mContext = null;
    HomeFragment(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Created!");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        textPosition = view.findViewById(R.id.txt_position);
        statusBar = view.findViewById(R.id.server_status_bar);
        statusColor = view.findViewById(R.id.status_color);
        Button btnClear = view.findViewById(R.id.btn_clear);
        btnClear.setOnClickListener(clearListener);
        btnStart = view.findViewById(R.id.btn_start);
        btnStart.setOnClickListener(startListener);
        img_mapCover = view.findViewById(R.id.map_cover);
        img_mapCover.setOnClickListener(startListener);
        map = new Map();
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(map);
        } else {
            Log.e(TAG, "mapFragment is null!!");
        }
        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    private View.OnClickListener startListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (funcThread != null && funcThread.isAlive()) {
                Log.i(TAG, "Trying to stop funcThread...");
                startSign = false;
                btnStart.setText(R.string.btn_start);
                textPosition.setText("");
                statusBar.setText(R.string.server_not_ready);
                statusColor.setBackgroundColor(Color.GREEN);
                map.clear();
                img_mapCover.setVisibility(View.VISIBLE);
                funcThread.interrupt();
//                Thread t = new Thread(new stopRequest());
//                t.start();
//                try {
//                    t.join();
//                } catch (java.lang.InterruptedException e){
//                    e.printStackTrace();
//                }
            } else {
                Log.i(TAG, "Trying to start funcThread...");
                bikeMoved = false;
                startSign = true;
                btnStart.setText(R.string.btn_stop);
                img_mapCover.setVisibility(View.INVISIBLE);
                funcThread = new Thread(mainFunc);
                funcThread.start();
            }
        }
    };
    private View.OnClickListener clearListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            textPosition.setText("");
        }
    };
    private Runnable mainFunc = new Runnable() {
        @SuppressLint("DefaultLocale")
        @Override
        public void run() {
            connect(ip, port);
            startRequest();
            JsonObject rcv_obj;
            int idx = 0;
            while (connect(ip, port) && startSign) {
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
                    textPosition.setText(String.format("%d -------TIMEOUT-------", idx++));
                    continue;
                }
                timer.interrupt();
                rcv_obj = getRequestExe.getObject();
                if (startSign) {
                    JsonObject position = rcv_obj.getAsJsonObject("position");
                    final double longitude = position.get("longitude").getAsFloat();
                    final double latitude = position.get("latitude").getAsFloat();
                    final int moved = position.get("moved").getAsInt();
                    Log.i(TAG, String.format("Longitude: <%f>, latitude: <%f>", longitude, latitude));
                    if (!bikeMoved && moved != 0) {
                        bikeMoved = true;
                        Log.i(TAG, "Moved!!!!!");
                    }
                    else if(!bikeMoved) {
                        firstMoved = new LatLng(latitude, longitude);
                    }
                    final String str_lng = Double.toString(longitude);
                    final String str_lat = Double.toString(latitude);
                    Objects.requireNonNull(getActivity()).runOnUiThread(new PositionUpdater(str_lng, str_lat, idx++));
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        Log.i(TAG, "mainFunc interrupted!");
                    }
                }
            }
            Log.i(TAG, "Leaving mainFunc...");
        }
    };

    class PositionUpdater implements Runnable {
        private String latitude;
        private String longitude;
        private LatLng latlng;
        private int idx;

        PositionUpdater(String longitude, String latitude, int idx) {
            this.longitude = longitude;
            this.latitude = latitude;
            latlng = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
            this.idx = idx;
        }

        PositionUpdater(LatLng latlng, int idx) {
            this.latlng = latlng;
            this.latitude = Double.toString(latlng.latitude);
            this.longitude = Double.toString(latlng.longitude);
            this.idx = idx;
        }

        @SuppressLint("DefaultLocale")
        @Override
        public void run() {
            textPosition.setText(String.format("%d. (%s, %s)", idx++, longitude, latitude));
            map.clearMarker();
            map.newMarker(latlng);
            if (bikeMoved) {
                if(firstMoved != null) {
                    // Moved first
                    statusColor.setBackgroundColor(Color.RED);
                    statusBar.setText("Moved!!!");
                    map.addTrack(firstMoved, latlng);
                    firstMoved = null;
                }
                else {
                    map.addTrack(latlng);
                }

            }
        }
    }



    private void startRequest() {
        Log.i(TAG, "Requesting start request...");
        try {
            String request = new Request(RequestType.START).toJson();
            writer.write(request);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String rcv_str = "";
        while (startSign) {
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
            while (startSign) {
                try {
                    Log.d(TAG, "Receiving GET...");
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
