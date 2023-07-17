package com.example.mini_cap.controller;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mini_cap.R;
import com.example.mini_cap.model.Preset;
import com.example.mini_cap.view.CustomViewHolder;
import com.example.mini_cap.view.PresetFragment;

import java.util.ArrayList;

/**
 * This Adapter is for the RecyclerView found in the EditActivity
 */
public class CustomEditAdapter extends RecyclerView.Adapter<CustomViewHolder> {

    private Context context;
    private ArrayList<Preset> presets;
    private static final String TAG = "EditActivity";
    private SelectListener selectListener;

    public CustomEditAdapter(Context context, ArrayList<Preset> presets, SelectListener selectListener){
        this.context = context;
        this.presets = presets;
        this.selectListener = selectListener;
    }

    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CustomViewHolder(LayoutInflater.from(context).inflate(R.layout.recyclerview_session_preset, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder holder, int position) {

        Preset preset = presets.get(position);
        holder.textView.setText("Preset name: " + preset.getName());

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //selectListener.onItemClicked(preset);
                PresetFragment presetFragment = PresetFragment.newInstance(preset, Dict.EDIT_PRESET);
                presetFragment.show(((AppCompatActivity) context).getSupportFragmentManager(), "EditPreset");
            }
        });
    }

    @Override
    public int getItemCount() {
        return presets.size();
    }
}
