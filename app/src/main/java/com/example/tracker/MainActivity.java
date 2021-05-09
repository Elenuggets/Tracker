package com.example.tracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    //Buttons
    private Button buttonEnd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        this.buttonEnd = findViewById(R.id.buttonEnd);
        buttonEnd.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // go to the reportActivity
                Intent reportActivity = new Intent(MainActivity.this,ReportActivity.class);

                // start the activity
                startActivity(reportActivity);
            }
        });
    }
}