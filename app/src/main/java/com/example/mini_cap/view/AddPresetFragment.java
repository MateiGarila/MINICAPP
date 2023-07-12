package com.example.mini_cap.view;

import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.mini_cap.R;
import com.example.mini_cap.controller.DBHelper;
import com.example.mini_cap.model.Preset;


public class AddPresetFragment extends DialogFragment implements AdapterView.OnItemSelectedListener {

    protected TextView presetTextView;
    protected EditText presetName, presetAge;
    protected Spinner skinToneSpinner;
    protected Button cancelBTN, confirmBTN;

    public AddPresetFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_preset, container, false);

        presetTextView = view.findViewById(R.id.addPresetTextView);
        presetName = view.findViewById(R.id.presetName);
        presetAge = view.findViewById(R.id.presetAge);
        skinToneSpinner = view.findViewById(R.id.presetSkinTone);
        cancelBTN = view.findViewById(R.id.presetCancelBTN);
        confirmBTN = view.findViewById(R.id.presetConfirmBTN);

        setUpSpinner();

        cancelBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        confirmBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


            }
        });

        return view;
    }

    private void setUpSpinner(){

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.skinTone_set, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        skinToneSpinner.setAdapter(adapter);
        skinToneSpinner.setOnItemSelectedListener(this);
    }

    private void createPreset(Preset preset){

        DBHelper dbHelper = new DBHelper(getActivity());
        dbHelper.insertPreSet(preset);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {


    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}