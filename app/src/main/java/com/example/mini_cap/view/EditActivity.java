package com.example.mini_cap.view;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mini_cap.R;
import com.example.mini_cap.controller.DBHelper;
import com.example.mini_cap.model.User;
import com.example.mini_cap.view.ModifyActivity;

import java.util.ArrayList;

public class EditActivity extends AppCompatActivity {
    private Button cancelButton;
    private Button modifyButton;
    private Button deleteButton;
    private Button submitButton;
    private TextView nameTextView1;
    private TextView ageTextView1;
    private TextView nameTextView2;
    private TextView ageTextView2;
    private TextView nameTextView3;
    private TextView ageTextView3;

    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        dbHelper = new DBHelper(getBaseContext());

        modifyButton = findViewById(R.id.modify_button);
        cancelButton = findViewById(R.id.cancel_button);
        submitButton = findViewById(R.id.submit_button);
        deleteButton = findViewById(R.id.delete_button);

        nameTextView1 = findViewById(R.id.name_textview1);
        ageTextView1 = findViewById(R.id.age_textview1);

        nameTextView2 = findViewById(R.id.name_textview2);
        ageTextView2 = findViewById(R.id.age_textview2);

        nameTextView3 = findViewById(R.id.name_textview3);
        ageTextView3 = findViewById(R.id.age_textview3);

        ArrayList<User> all_users = dbHelper.getAllUsers();

        while(all_users.size() > 3){
            all_users.remove(all_users.size()-1);
        }

        ArrayList<TextView> textViewsName = new ArrayList<>();
        textViewsName.add(nameTextView1);
        textViewsName.add(nameTextView2);
        textViewsName.add(nameTextView3);

        ArrayList<TextView> textViewsAge = new ArrayList<>();
        textViewsAge.add(ageTextView1);
        textViewsAge.add(ageTextView2);
        textViewsAge.add(ageTextView3);

        for(int i = 0; i<all_users.size();i++){
            textViewsName.get(i).setText("Name: " +all_users.get(i).getSurname() + ", " + all_users.get(i).getName());
            textViewsAge.get(i).setText("Age: " +String.valueOf(all_users.get(i).getAge()));
        }
        
        
        


        nameTextView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameTextView1.getText().toString().substring(6); // Remove "Name: " prefix
                String age = ageTextView1.getText().toString().substring(5); // Remove "Age: " prefix
                Intent intent = new Intent(EditActivity.this, ModifyActivity.class);
                intent.putExtra("name", name);
                intent.putExtra("age", age);
                startActivity(intent);
            }
        });

        nameTextView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameTextView2.getText().toString().substring(6); // Remove "Name: " prefix
                String age = ageTextView2.getText().toString().substring(5); // Remove "Age: " prefix
                Intent intent = new Intent(EditActivity.this, ModifyActivity.class);
                intent.putExtra("name", name);
                intent.putExtra("age", age);
                startActivity(intent);
            }
        });

        nameTextView3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameTextView3.getText().toString().substring(6); // Remove "Name: " prefix
                String age = ageTextView3.getText().toString().substring(5); // Remove "Age: " prefix
                Intent intent = new Intent(EditActivity.this, ModifyActivity.class);
                intent.putExtra("name", name);
                intent.putExtra("age", age);
                startActivity(intent);
            }
        });

        modifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name;
                String age;

                if (nameTextView1.getVisibility() == View.VISIBLE) {
                    name = nameTextView1.getText().toString().substring(6);
                    age = ageTextView1.getText().toString().substring(5);
                } else if (nameTextView2.getVisibility() == View.VISIBLE) {
                    name = nameTextView2.getText().toString().substring(6);
                    age = ageTextView2.getText().toString().substring(5);
                } else {
                    name = nameTextView3.getText().toString().substring(6);
                    age = ageTextView3.getText().toString().substring(5);
                }

                Intent intent = new Intent(EditActivity.this, ModifyActivity.class);
                intent.putExtra("name", name);
                intent.putExtra("age", age);
                startActivity(intent);
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle Cancel button click
                finish();
            }
        });

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
                Toast.makeText(EditActivity.this, "User deleted successfully", Toast.LENGTH_SHORT).show();
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
    private void showInfo(String name, String age) {
        nameTextView1.setText("Name: " + name);
        ageTextView1.setText("Age: " + age);
        nameTextView2.setText("");  // Clear the text in nameTextView2
        ageTextView2.setText("");  // Clear the text in ageTextView2
        nameTextView2.setVisibility(View.VISIBLE);
        ageTextView2.setVisibility(View.VISIBLE);
        nameTextView3.setVisibility(View.VISIBLE);
        ageTextView3.setVisibility(View.VISIBLE);
        modifyButton.setVisibility(View.VISIBLE);
        deleteButton.setVisibility(View.VISIBLE);
    }

    private void deleteInfo() {
        nameTextView1.setText("Name: ");
        ageTextView1.setText("Age: ");
        nameTextView2.setVisibility(View.VISIBLE);
        ageTextView2.setVisibility(View.VISIBLE);
        nameTextView3.setVisibility(View.VISIBLE);
        ageTextView3.setVisibility(View.VISIBLE);
        modifyButton.setVisibility(View.GONE);
        deleteButton.setVisibility(View.GONE);
        Toast.makeText(this, "Information deleted", Toast.LENGTH_SHORT).show();
    }

}



