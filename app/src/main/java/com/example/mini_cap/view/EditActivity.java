package com.example.mini_cap.view;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mini_cap.R;
import com.example.mini_cap.controller.CustomEditAdapter;
import com.example.mini_cap.controller.DBHelper;
import com.example.mini_cap.controller.SelectListener;
import com.example.mini_cap.model.Preset;

import java.util.ArrayList;

public class EditActivity extends AppCompatActivity implements SelectListener {

    protected TextView mainTextView;
    protected RecyclerView listOfPresets;
    protected Button backBTN;
    private DBHelper dbHelper;
    private CustomEditAdapter customEditAdapter;
    private static final String TAG = "EditActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        mainTextView = findViewById(R.id.topText);
        listOfPresets = findViewById(R.id.listOfPresets);
        backBTN = findViewById(R.id.backBTN);

        backBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        setRecyclerView();

    }

    /**
     * This method is used to set up the RecyclerView in the EditActivity
     */
    public void setRecyclerView(){

        ArrayList<Preset> presets =  DBHelper.get(this).getAllPresets();
        listOfPresets.setLayoutManager(new GridLayoutManager(this, 1));
        customEditAdapter = new CustomEditAdapter(this, presets, this);
        listOfPresets.setAdapter(customEditAdapter);
    }

    /**
     * This method is used to set the onClick functionality for each item in the RecyclerView
     * @param preset that has been clicked
     *
     *        ~~Double check if this is still being used~~
     */
    public void onItemClicked(Preset preset){

        PresetFragment fragment = new PresetFragment();
        fragment.show(getSupportFragmentManager(), "EditPreset");
    }

}



