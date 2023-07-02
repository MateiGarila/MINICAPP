package com.example.mini_cap.view;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.mini_cap.R;

public class MainActivity extends AppCompatActivity {

    protected TextView mainView;
    protected Button toSessionActivity;
    protected Button toStatsActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainView = findViewById(R.id.mainTextView);
        toSessionActivity = findViewById(R.id.toSessionActivity);
        toStatsActivity = findViewById(R.id.toStatsActivity);

        toSessionActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
    }

    public void setToSessionActivity(){
        Intent intent = new Intent(this, SessionActivity.class);
        startActivity(intent);
    }
}