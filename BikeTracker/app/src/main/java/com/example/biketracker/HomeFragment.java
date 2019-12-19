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

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";
    private TextView textArea = null;
    private TextView statusBar = null;
    private String ip;
    private int port;
    private Socket socket = null;
    private BufferedReader reader = null;
    private BufferedWriter writer = null;

    HomeFragment(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "Created!");
        funcThread.start();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        textArea = view.findViewById(R.id.textArea);
        textArea.setMovementMethod(new ScrollingMovementMethod());
        statusBar = view.findViewById(R.id.server_status_bar);
        Button btnClear = view.findViewById(R.id.btn_clear);
        btnClear.setOnClickListener(clearListener);
        return view;
    }

    private View.OnClickListener clearListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            textArea.setText("");
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
                    textArea.append("-------TIMEOUT-------\n");
                    continue;
                }
                timer.interrupt();
                rcv_obj = getRequestExe.getObject();
                JsonObject position = rcv_obj.getAsJsonObject("position");
                String longitude = position.get("longitude").toString();
                String latitude = position.get("latitude").toString();
                Log.i(TAG, String.format("Longitude: <%s>, latitude: <%s>", longitude, latitude));
                textArea.append(String.format("%d. (%s, %s)\n", idx++, longitude, latitude));
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    });

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

        public Timer(int timeout) {
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
            return;
        }

        public JsonObject getObject() {
            return jsonObject;
        }
    }

    ;
}
