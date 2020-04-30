package com.example.biketracker;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.json.JSONArray;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class HistoryFragment extends MFragment {
    private static final String TAG = "HistoryFragment";
    private String ip;
    private int port;
    private Map map;
    private Thread mainThread;
    private boolean requesting = false;
    //
    private Button btn_update;
    private SupportMapFragment mapFragment;

    public HistoryFragment(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);
        map = new Map();
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(map);
        } else {
            Log.e(TAG, "mapFragment is null!!");
        }
        btn_update = view.findViewById(R.id.btn_update);
        btn_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Trying to start mainThread...");
                mainThread = new Thread(mainFunc);
                mainThread.start();
            }
        });
        return view;
    }

    private Runnable mainFunc = new Runnable() {
        @SuppressLint("DefaultLocale")
        @Override
        public void run() {
            JsonObject rcv_obj;
            int idx = 0;
            connect(ip, port);
            requesting = true;
            HistoryRequestExe historyRequestExe = new HistoryRequestExe();
            Thread t = new Thread(historyRequestExe);
            Thread timer = new Thread(new Timer(1000));
            t.start();
            timer.start();
            while (timer.isAlive()) {
                if (!t.isAlive()) {
                    Log.d(TAG, "History request finished!");
                    break;
                }
            }
            if (t.isAlive()) {
                requesting = false;
                t.interrupt();
                timer.interrupt();
                Log.d(TAG, "History request timeout!");
            } else {
                timer.interrupt();
                rcv_obj = historyRequestExe.getObject();
                JsonArray datas = rcv_obj.getAsJsonArray("history");
                List<RcvData> rcvDatas = new ArrayList<>();
                for (int i = 0; i < datas.size(); ++i) {
                    JsonObject data = datas.get(i).getAsJsonObject();
                    try {
                        rcvDatas.add(new RcvData(data));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                Objects.requireNonNull(getActivity()).runOnUiThread(new PositionUpdater(rcvDatas));
            }
            Log.i(TAG, "Leaving mainFunc...");
        }
    };

    class RcvData implements Comparable<RcvData> {
        LatLng latlng;
        Calendar time;

        public RcvData(JsonObject data) throws ParseException {
            this.latlng = parsePosition(data.getAsJsonObject("position"));
            this.time = parseTime(data.getAsJsonObject("time"));

        }

        private LatLng parsePosition(JsonObject position) {
            float longitude = Float.parseFloat(position.getAsJsonPrimitive("longitude").getAsString());
            float latitude = Float.parseFloat(position.getAsJsonPrimitive("latitude").getAsString());
            return new LatLng(latitude, longitude);
        }

        private Calendar parseTime(JsonObject time) throws ParseException {
            int year = time.getAsJsonPrimitive("year").getAsInt();
            int month = time.getAsJsonPrimitive("month").getAsInt();
            int day = time.getAsJsonPrimitive("day").getAsInt();
            int hour = time.getAsJsonPrimitive("hour").getAsInt();
            int minute = time.getAsJsonPrimitive("minute").getAsInt();
            int second = time.getAsJsonPrimitive("second").getAsInt();
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, day, hour, minute, second);
            return calendar;
        }

        @Override
        public int compareTo(RcvData other) {
            return time.compareTo(other.time);
        }
    }

    class PositionUpdater implements Runnable {
        private List<RcvData> rcvDatas;

        PositionUpdater(List<RcvData> rcvDatas) {
            this.rcvDatas = rcvDatas;
            Collections.sort(this.rcvDatas);
        }

        @SuppressLint("DefaultLocale")
        @Override
        public void run() {
            for (int i = 0; i < rcvDatas.size(); ++i) {
                Log.d(TAG, "rcvDatas.get(i).latlng = " + rcvDatas.get(i).latlng);
                Log.d(TAG, "rcvDatas.get(i).time = " + rcvDatas.get(i).time);
                map.newMarker(rcvDatas.get(i).latlng);
                map.addTrack(rcvDatas.get(i).latlng);
            }
        }
    }


    private class HistoryRequestExe implements Runnable {
        private JsonObject jsonObject = null;

        @Override
        public void run() {
            Log.d(TAG, "Sending history request...");
            try {
                String request = new Request(RequestType.HISTORY).toJson();
                Log.i(TAG, "Sending:\n" + request);
                writer.write(request);
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            String rcv_str = "";
            while (requesting) {
                try {
                    Log.d(TAG, "Receiving HISTORY...");
                    rcv_str = reader.readLine();
                } catch (IOException e) {
                    Log.e(TAG, String.format("Received string <%s> error!", rcv_str));
                    e.printStackTrace();
                    continue;
                }
                if (rcv_str == null || rcv_str.equals("")) {
                    Log.d(TAG, "Invalid HISTORY response: " + rcv_str);
                } else {
                    break;
                }
            }

            Log.d(TAG, "HISTORY Receives " + rcv_str);
            jsonObject = new Gson().fromJson(rcv_str, JsonObject.class);
        }

        JsonObject getObject() {
            return jsonObject;
        }
    }
}
