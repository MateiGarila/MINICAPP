package com.example.mini_cap.view;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.mini_cap.R;

public class EndSessionActivity extends AppCompatActivity {
    protected Button stopBTN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end_session);

        stopBTN = findViewById(R.id.StopSessionBTN);

        stopBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle Cancel button click
                Intent intent = new Intent(EndSessionActivity.this, SessionActivity.class);
                startActivity(intent);
            }
        });
    }
}