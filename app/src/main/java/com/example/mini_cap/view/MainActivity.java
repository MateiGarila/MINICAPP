package com.example.mini_cap.view;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import android.widget.Button;
import android.widget.TextView;

import com.example.mini_cap.R;

public class MainActivity extends AppCompatActivity {

    //Declaration of all UI elements
    protected TextView mainView;
    protected Button sessionActivity;
    protected Button statsActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Attaching the UI elements to their respective objects
        mainView = findViewById(R.id.mainTextView);
        sessionActivity = findViewById(R.id.toSessionActivity);
        statsActivity = findViewById(R.id.toStatsActivity);

        sessionActivity.setOnClickListener(v -> toSessionActivity());
        statsActivity.setOnClickListener(v -> toStatsActivity());

    }

    private void toSessionActivity(){
        Intent intent = new Intent(this, SessionActivity.class);
        startActivity(intent);
    }

    private void toStatsActivity(){
        Intent intent = new Intent(this, StatsActivity.class);
        startActivity(intent);
    }
}