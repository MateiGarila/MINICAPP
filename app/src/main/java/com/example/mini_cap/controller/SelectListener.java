package com.example.mini_cap.controller;

import com.example.mini_cap.model.Preset;

/**
 * This interface is what allows items in each RecyclerView to have custom onClickListeners
 */
public interface SelectListener {

    void onItemClicked(Preset preset);
}
