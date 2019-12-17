package com.example.biketracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

public class WorkSpace extends AppCompatActivity {
    private static final String TAG = "WorkSpace";

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    TabLayout tabs;

    private String ip;
    private int port;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_space);
        Log.d(TAG, "App started...");
        Bundle extras = getIntent().getExtras();
        ip = extras.getString("ip");
        port = extras.getInt("port");

        tabs = findViewById(R.id.tabs);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = findViewById(R.id.view_pager);
        FloatingActionButton fab = findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        setViewPager(mViewPager);
        tabs.setupWithViewPager(mViewPager);
    }

    private void setViewPager(ViewPager viewPager) {
        SectionsPagerAdapter adapter = new SectionsPagerAdapter(getSupportFragmentManager());
        adapter.add(new HomeFragment(ip, port), "Home");
        adapter.add(new HistoryFragment(), "History");
        viewPager.setAdapter(adapter);
    }
}
