package com.example.mini_cap.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.mini_cap.R;
import com.example.mini_cap.controller.DBHelper;
import com.example.mini_cap.model.User;

import java.util.ArrayList;

public class SessionActivity extends AppCompatActivity {

    //Declaration of all UI elements
    protected TextView mainTextView, statusTextView;
    protected RecyclerView displayUser;
    protected Button startStop, addUser, editUser;

    //Needed
    private DBHelper dbHelper;
    private final static String TAG = "SessionActivity";

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
        addUser = findViewById(R.id.addUserBTN);
        editUser = findViewById(R.id.editUserBTN);

        //Temporary until we figure out a better way to navigate - Mat
        mainTextView.setOnClickListener(v -> finish());

        //dbHelper.insertUser(new User(45, "Snow", "Jon", 27, "pale"));
        //dbHelper.insertUser(new User(72, "Smith", "Jack", 54, "dark"));
        User user = dbHelper.getUser(2);

        addUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                statusTextView.setText(user.toString());
            }
        });

    }


}