package com.example.biketracker;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private Button btn_connect;
    private EditText edt_ip;
    private EditText edt_port;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn_connect = findViewById(R.id.btn_connect);
        edt_ip = findViewById(R.id.ip_edt);
        edt_port = findViewById(R.id.port_edt);
        btn_connect.setOnClickListener(connectListner);
    }

    View.OnClickListener connectListner = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String ip = edt_ip.getText().toString();
            int port = Integer.parseInt(edt_port.getText().toString());
            Intent intent = new Intent(MainActivity.this, WorkSpace.class);
            intent.putExtra("ip", ip);
            intent.putExtra("port", port);
            startActivity(intent);
        }
    };
}