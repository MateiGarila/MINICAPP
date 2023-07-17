package com.example.mini_cap.controller;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mini_cap.R;
import com.example.mini_cap.model.Preset;
import com.example.mini_cap.view.CustomViewHolder;

import java.util.ArrayList;

public class CustomStartAdapter extends RecyclerView.Adapter<CustomViewHolder> {

    private Context context;
    private ArrayList<Preset> presets;
    private static final String TAG = "StartSessionFragment";
    private SelectListener selectListener;

    public CustomStartAdapter(Context context, ArrayList<Preset> presets, SelectListener selectListener){
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
        holder.textView.setText(preset.getName());

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectListener.onItemClicked(preset);
            }
        });
    }

    @Override
    public int getItemCount() {
        return presets.size();
    }
}
