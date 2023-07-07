package com.example.mini_cap.controller;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.os.Bundle;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;

import com.example.mini_cap.R;
import com.example.mini_cap.model.User;

public class AddSessionUser extends DialogFragment {
    private EditText surname;
    private EditText name;
    private EditText age;
    private Spinner skinTone;
    private String selectedSkinTone;


    public interface AddSessionUserListener{
        void onSessionUserAdded(User user);
    }

    private AddSessionUserListener listener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialogfragment_adduser, null);

        surname = view.findViewById(R.id.editTextSurname);
        name = view.findViewById(R.id.editTextName);
        age = view.findViewById(R.id.editTextAge);
        skinTone = view.findViewById(R.id.spinnerSkinTone);

        builder.setView(view)
                .setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        validateInput();
                    }
                })
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

        skinTone.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedSkinTone = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                skinTone = null;
            }
        });

        if (surnameString.isEmpty() || nameString.isEmpty() || ageString.isEmpty()){
            Toast.makeText(getActivity(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
        }else{
            int ageInt = Integer.parseInt(ageString);
            User sessionUser = new User(0, surnameString, nameString, ageInt, selectedSkinTone);
            listener.onSessionUserAdded(sessionUser);
            dismiss();
        }

    }
}
