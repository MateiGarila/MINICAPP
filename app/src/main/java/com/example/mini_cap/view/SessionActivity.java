package com.example.mini_cap.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mini_cap.R;
import com.example.mini_cap.controller.AddSessionUser;
import com.example.mini_cap.controller.DBHelper;
import com.example.mini_cap.controller.Dict;
import com.example.mini_cap.model.Preset;

public class SessionActivity extends AppCompatActivity  {

    //Declaration of all UI elements
    protected TextView mainTextView, statusTextView;
    protected RecyclerView displayUser;
    protected Button startStopBTN, addPresetBTN, editPresetBTN;

    //Needed
    private DBHelper dbHelper;
    private final static String TAG = "SessionActivity";
    private final boolean isCreate = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session);

        dbHelper = new DBHelper(getBaseContext());

        //Attaching the UI elements to their respective objects
        mainTextView = findViewById(R.id.sessionActivityTextView);
        statusTextView = findViewById(R.id.sessionStatusTextView);
        displayUser = findViewById(R.id.sessionUserDisplayRV);
        startStopBTN = findViewById(R.id.startStopSessionBTN);
        addPresetBTN = findViewById(R.id.addUserBTN);
        editPresetBTN = findViewById(R.id.editUserBTN);

        //Temporary until we figure out a better way to navigate - Mat
        mainTextView.setOnClickListener(v -> finish());

        addPresetBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Add Preset PRESSED");
                PresetFragment fragment = PresetFragment.newInstance(null, isCreate);
                fragment.show(getSupportFragmentManager(), "CreatePreset");

            }
        });

        editPresetBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toEditActivity();
            }
        });

        startStopBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Start Session PRESSED");
                StartSessionFragment fragment = new StartSessionFragment();
                fragment.show(getSupportFragmentManager(), "StartSession");
            }
        });

    }

    protected void toEditActivity(){
        Intent intent = new Intent(this, EditActivity.class);
        startActivity(intent);
    }

    public void startSession(Preset preset){
        Toast.makeText(this, "I got called from fragment: " + preset.getName(), Toast.LENGTH_SHORT).show();
    }

}