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

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";
    private TextView textArea = null;
    private Button btnClear = null;

    public void addInfo(String s) {
        Log.d(TAG, String.format("Adding <%s> to textArea...", s));
        textArea.append(s + '\n');
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
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        textArea = view.findViewById(R.id.textArea);
        btnClear = view.findViewById(R.id.btn_clear);
        btnClear.setOnClickListener(clearListener);
        return view;
    }

    View.OnClickListener clearListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            textArea.setText(R.string.text_area);
        }
    };
}
