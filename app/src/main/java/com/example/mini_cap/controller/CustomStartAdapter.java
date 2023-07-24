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

    private final Context context;
    private final ArrayList<Preset> presets;
    private static final String TAG = "StartSessionFragment";
    private final SelectListener selectListener;

    /**
     * A custom adapter for the RecyclerView found in the fragment that is used to start a session
     * with a preset
     * @param context
     * @param presets an ArrayList of presets that will be displayed in the RecyclerView
     * @param selectListener used for the onClickListener
     */
    public CustomStartAdapter(Context context, ArrayList<Preset> presets, SelectListener selectListener){
        this.context = context;
        this.presets = presets;
        this.selectListener = selectListener;
    }

    /**
     * Inflater for the custom view for each item in the RecyclerView
     *
     * @param parent The ViewGroup into which the new View will be added after it is bound to
     *               an adapter position.
     * @param viewType The view type of the new View.
     *
     * @return
     */
    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CustomViewHolder(LayoutInflater.from(context).inflate(R.layout.recyclerview_session_preset, parent, false));
    }

    /**
     * This method sets the text and the onClickListener for each item in the RecyclerView
     *
     * @param holder The ViewHolder which should be updated to represent the contents of the
     *        item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder holder, int position) {

        Preset preset = presets.get(position);
        holder.textView.setText(preset.getName());

        holder.cardView.setOnClickListener(v -> selectListener.onItemClicked(preset));
    }

    /**
     * Method used to get the item count
     * @return the number of items
     */
    @Override
    public int getItemCount() {
        return presets.size();
    }
}
