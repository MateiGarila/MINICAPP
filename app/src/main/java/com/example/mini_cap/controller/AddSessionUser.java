package com.example.mini_cap.controller;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.fragment.app.DialogFragment;

import com.example.mini_cap.R;
import com.example.mini_cap.model.Preset;

public class AddSessionUser extends DialogFragment {

    private EditText name, age;
    private Spinner skinTone;
    private String selectedSkinTone;


    public AddSessionUser(){
        //Required empty public constructor
    }



//    public interface AddSessionUserListener{
//        void onSessionUserAdded(Preset preset);
//    }
//
//    private AddSessionUserListener listener;
//
//    @Override
//    public void onAttach(Context context) {
//        super.onAttach(context);
//        try {
//            listener = (AddSessionUserListener) context;
//        } catch (ClassCastException e) {
//            throw new ClassCastException(context.toString() + " must implement AddSessionUserListener");
//        }
//    }
//
//    @Override
//    public Dialog onCreateDialog(Bundle savedInstanceState){
//        // Create dialog with for entering a new user
//        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//        LayoutInflater inflater = requireActivity().getLayoutInflater();
//        View view = inflater.inflate(R.layout.fragment_add_user, null);
//
//        // Linking Edit Text fields with variables
//        name = view.findViewById(R.id.editTextName);
//        age = view.findViewById(R.id.editTextAge);
//        skinTone = view.findViewById(R.id.spinnerSkinTone);
//
//
//        builder.setView(view)
//                // Submit button calls validate function
//                .setPositiveButton("Submit", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        validateInput();
//                    }
//                })
//                // Cancel button returns to activity
//                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dismiss();
//                    }
//                });
//        return builder.create();
//    }
//
//    private void validateInput() {
//        String nameString = name.getText().toString().trim();
//        String ageString = age.getText().toString().trim();
//        selectedSkinTone = skinTone.getSelectedItem().toString();
//        int ageInt = Integer.parseInt(ageString);
//
//        if (nameString.isEmpty() || ageString.isEmpty()  || selectedSkinTone == null){
//            Toast.makeText(getActivity(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
//        }if (ageInt < 5 || ageInt > 99){
//            Toast.makeText(getActivity(), "Please enter a valid age", Toast.LENGTH_SHORT).show();
//        }else{
//
//            Preset sessionPreSet = new Preset(1, nameString, ageInt, selectedSkinTone);
//            listener.onSessionUserAdded(sessionPreSet);
//            dismiss();
//        }
//
//    }

}
