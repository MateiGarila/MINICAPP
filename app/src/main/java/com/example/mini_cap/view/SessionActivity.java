package com.example.mini_cap.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.mini_cap.R;
import com.example.mini_cap.controller.DBHelper;

public class SessionActivity extends AppCompatActivity  {

    //Declaration of all UI elements
    protected TextView mainTextView, statusTextView;
    protected RecyclerView displayUser;
    protected Button startStop, addPreset, editUser;

    //Needed
    private DBHelper dbHelper;
    private final static String TAG = "SessionActivity";
    private boolean isAddUserButtonVisible = true; // Store the initial visibility state

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session);

        dbHelper = new DBHelper(getBaseContext());

        //Attaching the UI elements to their respective objects
        mainTextView = findViewById(R.id.sessionActivityTextView);
        statusTextView = findViewById(R.id.sessionStatusTextView);
        displayUser = findViewById(R.id.sessionUserDisplayRV);
        startStop = findViewById(R.id.startStopSessionBTN);
        addPreset = findViewById(R.id.addUserBTN);
        editUser = findViewById(R.id.editUserBTN);

        //Temporary until we figure out a better way to navigate - Mat
        mainTextView.setOnClickListener(v -> finish());

        addPreset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddPresetFragment fragment = new AddPresetFragment();
                fragment.show(getSupportFragmentManager(), "CreatePreset");
            }
        });

    }

}