package com.example.tracker;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class ReportActivity extends AppCompatActivity {

    // Text View
    TextView txtTime;
    TextView txtDist;
    TextView txtSpeed;
    TextView txtAlt;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        // get the value of the main activity
        long timeD = getIntent().getLongExtra("timeDay",0);
        long timeH = getIntent().getLongExtra("timeHour",0);
        long timeM = getIntent().getLongExtra("timeMinute",0);
        long timeS = getIntent().getLongExtra("timeSecond",0);
        double dist = getIntent().getDoubleExtra("distance",0);
        double speed = getIntent().getDoubleExtra("speed",0);
        double minAlt = getIntent().getDoubleExtra("minalt",0);
        double maxAlt = getIntent().getDoubleExtra("maxalt",0);

        // id of the text view
        txtTime = findViewById(R.id.totaltimevalue);
        txtDist = findViewById(R.id.totaldistancevalue);
        txtSpeed = findViewById(R.id.averagespeedvalue);
        txtAlt = findViewById(R.id.minmaxaltitudevalue);


        // set the value
        String curTime = timeD + " day, " + timeH + " hour, " + timeM + " minute, " + timeS + " seconds";
        txtTime.setText(curTime);
        txtDist.setText(dist+" m");
        txtSpeed.setText(speed+" m/s");
        txtAlt.setText("Min Altitude : "+minAlt+", Max Altitude : "+maxAlt);
    }
}