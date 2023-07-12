package com.example.mini_cap.controller;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.os.Bundle;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;

import com.example.mini_cap.R;
import com.example.mini_cap.model.PreSet;

public class AddSessionUser extends DialogFragment {
    private EditText surname;
    private EditText name;
    private EditText age;
    private Spinner skinTone;
    private String selectedSkinTone;


    public interface AddSessionUserListener{
        void onSessionUserAdded(PreSet preSet);
    }

    private AddSessionUserListener listener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (AddSessionUserListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement AddSessionUserListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        // Create dialog with for entering a new user
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialogfragment_adduser, null);

        // Linking Edit Text fields with variables
        surname = view.findViewById(R.id.editTextSurname);
        name = view.findViewById(R.id.editTextName);
        age = view.findViewById(R.id.editTextAge);
        skinTone = view.findViewById(R.id.spinnerSkinTone);


        builder.setView(view)
                // Submit button calls validate function
                .setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        validateInput();
                    }
                })
                // Cancel button returns to activity
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                });
        return builder.create();
    }

    private void validateInput() {
        String surnameString = surname.getText().toString().trim();
        String nameString = name.getText().toString().trim();
        String ageString = age.getText().toString().trim();
        selectedSkinTone = skinTone.getSelectedItem().toString();
        int ageInt = Integer.parseInt(ageString);

        if (surnameString.isEmpty() || nameString.isEmpty() || ageString.isEmpty()  || selectedSkinTone == null){
            Toast.makeText(getActivity(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
        }if (ageInt < 5 || ageInt > 99){
            Toast.makeText(getActivity(), "Please enter a valid age", Toast.LENGTH_SHORT).show();
        }else{

            PreSet sessionPreSet = new PreSet(1, nameString, ageInt, selectedSkinTone);
            listener.onSessionUserAdded(sessionPreSet);
            dismiss();
        }

    }

}
