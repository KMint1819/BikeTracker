package com.example.biketracker;

import android.util.Log;

import androidx.fragment.app.Fragment;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;


class MFragment extends Fragment {
    private String TAG = "MFragment";
    protected Socket socket = null;
    protected BufferedReader reader = null;
    protected BufferedWriter writer = null;

    protected boolean connect(String ip, int port) {
        Log.i(TAG, "Connecting to " + ip + ":" + port);
        try {
            socket = new Socket(ip, port);
        } catch (IOException e) {
            Log.e(TAG, "Cannot connect!");
            Log.e(TAG, e.toString());
            return false;
        }
        Log.i(TAG, "Successfully connected!");
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.i(TAG, "IO stream created successfully!");
        return true;
    }
    protected class Timer implements Runnable {
        int timeout;

        Timer(int timeout) {
            this.timeout = timeout;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(timeout);
            } catch (InterruptedException e) {
                Log.i(TAG, "Timer interrupted");
            }
        }
    }
}