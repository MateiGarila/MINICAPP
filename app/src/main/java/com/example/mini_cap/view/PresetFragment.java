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
    protected Button cancelBTN, confirmBTN, deleteBTN;
    private DBHelper dbHelper;

    /**
     * Required empty public constructor
     */
    public PresetFragment() {

    }

    /**
     * The onCreateView which inflates the layout and determines the context which the fragment
     * was called from and manages the view accordingly
     *
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return
     */
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
        deleteBTN = view.findViewById(R.id.deleteBTN);

        setUpSpinner();

        // Retrieve the object from the arguments
        if(getArguments() != null){
            Preset presetToEdit = getArguments().getParcelable("preset");
            if(presetToEdit != null)
                presetID = presetToEdit.getPresetID();
            presetContext = getArguments().getInt("context");
            switch (presetContext){
                case 1:
                    //CREATE_PRESET
                    confirmBTN.setText(R.string.confirmBTN);
                    deleteBTN.setVisibility(View.GONE);
                    break;
                case 2:
                    //EDIT_PRESET
                    presetName.setText(presetToEdit.getName());
                    presetAge.setText(String.valueOf(presetToEdit.getAge()));
                    int spinnerIndex = getIndexForValue(skinToneSpinner, presetToEdit.getSkinTone());
                    skinToneSpinner.setSelection(spinnerIndex);
                    confirmBTN.setText(R.string.editBTN);
                    deleteBTN.setVisibility(View.VISIBLE);
                    break;
                default:
                    Toast.makeText(getContext(), "switch oopsie onCreateView", Toast.LENGTH_SHORT).show();
                    break;
            }
        }

        cancelBTN.setOnClickListener(v -> dismiss());

        int finalPresetContext = presetContext;
        int finalPresetID = presetID;
        confirmBTN.setOnClickListener(v -> {
            presetManipulation(finalPresetContext, finalPresetID);
        });

        deleteBTN.setOnClickListener(v -> {
            dbHelper.deletePreset(finalPresetID);
            ((EditActivity)getActivity()).setRecyclerView();
            dismiss();
        });

        return view;
    }

    /**
     * This method sets up the spinner in the fragment by populating it with the available strings
     */
    private void setUpSpinner(){

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.skinTone_set, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        skinToneSpinner.setAdapter(adapter);
        skinToneSpinner.setOnItemSelectedListener(this);
    }

    /**
     * This method returns the index of a specific string in the spinner
     * @param spinner spinner object to get the adapter
     * @param value string value to match
     * @return index of matched value
     */
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

    /**
     * This method uses the passed 'presetContext' to determine which DB operation to perform
     * @param presetContext integer used to differentiate 'create' and 'edit' preset
     * @param presetID is needed for the edit and delete (not implemented yet) functionalities
     */
    private void presetManipulation(int presetContext, int presetID){

        if(!(presetName.getText().toString().isEmpty())){

            if(!(presetAge.getText().toString().isEmpty())){

                String name = presetName.getText().toString();
                int age = Integer.parseInt(presetAge.getText().toString());
                String skinTone = skinToneSpinner.getSelectedItem().toString();
                Preset preset = new Preset(0, name, age, skinTone);

                if(presetContext == 1){
                    dbHelper.insertPreSet(preset);
                } else if (presetContext == 2) {
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

    /**
     * This method is used to pass data from the activity to the fragment by passing arguments
     * @param preset is used when the user wants to edit their presets, this is not needed (can be
     *               null) when creating a preset
     * @param contextIdentifier is used to determine which context the fragment was called from,
     *                          this is NEEDED all contexts can be found in the Dict file
     * @return the PresetFragment with added context and preset
     */
    public static PresetFragment newInstance(Preset preset, int contextIdentifier){

        PresetFragment fragment = new PresetFragment();
        Bundle args = new Bundle();
        args.putParcelable("preset", (Parcelable) preset);
        args.putInt("context", contextIdentifier);
        fragment.setArguments(args);
        return  fragment;
    }

    /**
     * Needed method for the Spinner object - leave as is unless new functionality is required
     * @param parent The AdapterView where the selection happened
     * @param view The view within the AdapterView that was clicked
     * @param position The position of the view in the adapter
     * @param id The row id of the item that is selected
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    /**
     * Needed method for the Spinner object - leave as is unless new functionality is required
     * @param parent The AdapterView that now contains no selected item.
     */
    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

}