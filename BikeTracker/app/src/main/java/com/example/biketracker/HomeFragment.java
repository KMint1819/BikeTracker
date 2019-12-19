package com.example.biketracker;

import android.os.Bundle;
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
import java.util.Objects;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";
    private TextView textArea = null;
    private String ip;
    private int port;
    private Socket socket = null;
    private BufferedReader reader = null;
    private BufferedWriter writer = null;

    HomeFragment(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public void addInfo(String s) {
        Log.d(TAG, String.format("Adding <%s> to textArea...", s));
        textArea.append(s + '\n');
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
        @Override
        public void run() {
            connect();
            startRequest();
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
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
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
            break;
        }
        Log.i(TAG, "Received " + rcv_str);
        Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textArea.setText(R.string.server_ready);
            }
        });
    }
}
