package com.example.mini_cap.view;

import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mini_cap.R;
import com.example.mini_cap.controller.DBHelper;
import com.example.mini_cap.model.Preset;


public class PresetFragment extends DialogFragment implements AdapterView.OnItemSelectedListener {

    protected TextView presetTextView;
    protected EditText presetName, presetAge;
    protected Spinner skinToneSpinner;
    protected Button cancelBTN, confirmBTN;
    private DBHelper dbHelper;

    public PresetFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_preset, container, false);

        dbHelper = new DBHelper(getContext());
        int presetContext = 0;
        int presetID = 0;
        presetTextView = view.findViewById(R.id.addPresetTextView);
        presetName = view.findViewById(R.id.presetName);
        presetAge = view.findViewById(R.id.presetAge);
        skinToneSpinner = view.findViewById(R.id.presetSkinTone);
        cancelBTN = view.findViewById(R.id.presetCancelBTN);
        confirmBTN = view.findViewById(R.id.presetConfirmBTN);

        setUpSpinner();

        // Retrieve the object from the arguments
        if(getArguments() != null){
            Preset presetToEdit = getArguments().getParcelable("preset");
            presetID = presetToEdit.getPresetID();
            presetContext = getArguments().getInt("context");
            switch (presetContext){
                case 1:
                    //CREATE_PRESET
                    confirmBTN.setText(R.string.confirmBTN);
                    break;
                case 2:
                    //EDIT_PRESET
                    presetName.setText(presetToEdit.getName());
                    presetAge.setText(String.valueOf(presetToEdit.getAge()));
                    int spinnerIndex = getIndexForValue(skinToneSpinner, presetToEdit.getSkinTone());
                    skinToneSpinner.setSelection(spinnerIndex);
                    confirmBTN.setText(R.string.editBTN);
                    break;
                default:
                    Toast.makeText(getContext(), "switch oopsie", Toast.LENGTH_SHORT).show();
                    break;
            }
        }

        cancelBTN.setOnClickListener(v -> dismiss());

        int finalPresetContext = presetContext;
        int finalPresetID = presetID;
        confirmBTN.setOnClickListener(v -> {
            presetManipulation(finalPresetContext, finalPresetID);
        });
        return view;
    }

    private void setUpSpinner(){

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.skinTone_set, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        skinToneSpinner.setAdapter(adapter);
        skinToneSpinner.setOnItemSelectedListener(this);
    }

    private int getIndexForValue(Spinner spinner, String value) {
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinner.getAdapter();
        int count = adapter.getCount();
        for (int i = 0; i < count; i++) {
            if (adapter.getItem(i).equals(value)) {
                return i;
            }
        }
        return 0;  // Default value if desired value not found
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }


    public void presetManipulation(int presetContext, int presetID){

        if(!(presetName.getText().toString().isEmpty())){

            if(!(presetAge.getText().toString().isEmpty())){

                String name = presetName.getText().toString();
                int age = Integer.parseInt(presetAge.getText().toString());
                String skinTone = skinToneSpinner.getSelectedItem().toString();
                Preset preset = new Preset(0, name, age, skinTone);

                if(presetContext == 1){
                    dbHelper.insertPreSet(preset);
                } else if (presetContext == 2) {
                    //THIS DOES WORK
                    //Need to update view
                    dbHelper.updatePreset(presetID, preset);
                    ((EditActivity)getActivity()).setRecyclerView();
                }else {
                    Toast.makeText(getContext(), "PresetContext not supported", Toast.LENGTH_SHORT).show();
                }
                dismiss();

            }else{
                Toast.makeText(getContext(), "Please include an age for your preset", Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(getContext(), "Your preset needs a name.", Toast.LENGTH_SHORT).show();
        }
    }


    public static PresetFragment newInstance(Preset preset, int contextIdentifier){

        PresetFragment fragment = new PresetFragment();
        Bundle args = new Bundle();
        args.putParcelable("preset", (Parcelable) preset);
        args.putInt("context", contextIdentifier);
        fragment.setArguments(args);
        return  fragment;
    }

}