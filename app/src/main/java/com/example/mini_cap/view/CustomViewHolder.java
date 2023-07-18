package com.example.mini_cap.view;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mini_cap.R;

/**
 * This class is used to fetch and identify each item in the recyclerview_session_preset layout file
 */
public class CustomViewHolder extends RecyclerView.ViewHolder {

    public CardView cardView;
    public TextView textView;

    public CustomViewHolder(@NonNull View itemView) {
        super(itemView);
        cardView = itemView.findViewById(R.id.main_container);
        textView = itemView.findViewById(R.id.recycleTextView);
    }
}
