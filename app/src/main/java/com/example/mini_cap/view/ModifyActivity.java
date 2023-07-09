package com.example.mini_cap.view;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.mini_cap.R;

public class ModifyActivity extends AppCompatActivity {
    private EditText nameEditText;
    private EditText ageEditText;
    private Button submitButton;
    private Button CancelButton;
    private Button deleteButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify);

        nameEditText = findViewById(R.id.name_edittext);
        ageEditText = findViewById(R.id.age_edittext);
        submitButton = findViewById(R.id.submit_button);
        CancelButton = findViewById(R.id.Cancel_button);
        deleteButton = findViewById(R.id.delete_button);

        Intent intent = getIntent();
        String name = intent.getStringExtra("name");
        String age = intent.getStringExtra("age");

        nameEditText.setText(name);
        ageEditText.setText(age);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newName = nameEditText.getText().toString();
                String newAge = ageEditText.getText().toString();
                if (newName.isEmpty() || newAge.isEmpty()) {
                    Toast.makeText(ModifyActivity.this, "Please edit both name and age", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(ModifyActivity.this, "Name and age updated", Toast.LENGTH_LONG).show();
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("newName", newName);
                    resultIntent.putExtra("newAge", newAge);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                }
            }
        });
        CancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle Cancel button click
                // Redirect the user to the EditActivity menu
                //openEditActivity();
                Intent intent = new Intent(ModifyActivity.this, EditActivity.class);
                startActivity(intent);
            }
        });
        //}
        //private void openEditActivity() {
        // Intent intent = new Intent(ModifyActivity.this, EditActivity.class);
        // startActivity(intent);
        // }

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle Delete button click
                showDeleteConfirmationDialog();
            }
        });
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete User");
        builder.setMessage("Are you sure you wish to delete this user?");
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Delete the specific session user
                Toast.makeText(ModifyActivity.this, "User deleted successfully", Toast.LENGTH_LONG).show();
                onBackPressed(); // Redirect the user to the EditActivity menu
            }
        });
        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Handle "NO" button click (back button)
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}