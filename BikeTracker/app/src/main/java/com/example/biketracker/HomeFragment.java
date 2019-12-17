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

import java.io.IOException;
import java.net.Socket;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";
    private TextView textArea = null;
    private String ip;
    private int port;
    private Socket socket = null;
    public HomeFragment(String ip, int port){
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
        Log.i(TAG,"Created!");
        connect();

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

    View.OnClickListener clearListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            textArea.setText(R.string.text_area);
        }
    };
    private void connect(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "Connecting to " + ip + ":" + Integer.toString(port));
                try {
                    socket = new Socket(ip, port);
                } catch (IOException e) {
                    Log.e(TAG, "Cannot connect!");
                    Log.e(TAG, e.toString());
                }
            }
        }).start();
        Log.i(TAG, "Successfully connected!");
    }
}
