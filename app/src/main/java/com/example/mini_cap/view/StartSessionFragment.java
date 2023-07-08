package com.example.mini_cap.view;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mini_cap.R;
import com.example.mini_cap.controller.DBHelper;
import com.example.mini_cap.model.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StartSessionFragment extends DialogFragment {


    private TextView current_user_edit_text, session_user_1, session_user_2, session_user_3;
    private ListView all_users_list_view;
    private Button delete_button_frag, start_button_frag;

    private ImageButton cancel_button;

    private DBHelper dbHelper;

    private Context context;

    ArrayList<User> all_users;


    public static ArrayList<User> session_users = new ArrayList<>();

    public static ArrayList<User> other_users = new ArrayList<>();

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    public StartSessionFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_start_session, container, false);

        dbHelper = new DBHelper(context);
        current_user_edit_text = view.findViewById(R.id.current_user_id);
        all_users_list_view = view.findViewById(R.id.all_users_listview_id);
        delete_button_frag = view.findViewById(R.id.frag_delete_button_id);
        start_button_frag = view.findViewById(R.id.frag_start_button_id);
        cancel_button = view.findViewById(R.id.frag_cancel_button_id);
        session_user_1 = view.findViewById(R.id.session_user_1_id);
        session_user_2 = view.findViewById(R.id.session_user_2_id);
        session_user_3 = view.findViewById(R.id.session_user_3_id);
        ArrayList<TextView> textViews = new ArrayList<>();
        textViews.add(session_user_1);
        textViews.add(session_user_2);
        textViews.add(session_user_3);

        all_users = dbHelper.getAllUsers();
        System.out.println("size of all users" + all_users.size());
        final User[] selected_user = {null};
        if (all_users.size() >0) {
            current_user_edit_text.setText("Main User: " + all_users.get(0).getSurname() + ", " +all_users.get(0).getName());
        }


        if (all_users.size() > 1){
            for (int i = 1; i < all_users.size(); i++){
                other_users.add(all_users.get(i));

            }
        }

        load_session_users(textViews);
        load_list_view(context);


        all_users_list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            int previousSelectedPosition = -1;
            View previousSelectedView = null;

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (previousSelectedPosition != -1 && previousSelectedView != null) {
                    // Deselect the previously selected item
                    previousSelectedView.setBackgroundColor(previousSelectedView.getDrawingCacheBackgroundColor());
                }

                // Highlight the currently selected item
                view.setBackgroundColor(ContextCompat.getColor(context, R.color.item_selected_color));
                previousSelectedPosition = position;
                previousSelectedView = view;

                // Handle item click here
                selected_user[0] = other_users.get(position);
            }
        });

        delete_button_frag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selected_user[0]!= null){
                    User delete_user = selected_user[0];
                    other_users.remove(selected_user[0]);

                    for (int i = 0; i < session_users.size(); i++){
                        System.out.println("ids: " + session_users.get(i).getUserID() + " " + delete_user.getUserID());
                        if (session_users.get(i).getUserID() == delete_user.getUserID()) {
                            session_users.remove(i);
                            load_session_users(textViews);
                        }
                    }

                    load_list_view(context);

                }
            }
        });

        start_button_frag.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (selected_user[0]!= null){
                    if(session_users.size() < 3){
                        session_users.add(selected_user[0]);
                        load_session_users(textViews);
                    } else {
                        Toast.makeText(context, "You reached the limit for session users", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        cancel_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dismiss();
            }
        });


        return view;


    }

    private void load_list_view(Context context) {
        List<String> list_of_profiles = new ArrayList<>();


        if (other_users.size() < 1) {
            return;
        }

        String account_string = "";

        for (int i = 0; i < other_users.size(); i++) {
            list_of_profiles.add(account_string.concat(String.valueOf(i + 1) + ". " + other_users.get(i).getSurname() + ", " + other_users.get(i).getName()));
        }

        ArrayAdapter arrayAdapter = new ArrayAdapter(context, android.R.layout.simple_list_item_1, list_of_profiles);
        all_users_list_view.setAdapter(arrayAdapter);


    }

    private void load_session_users(ArrayList<TextView> textViews){
        for (int i = 0; i<session_users.size(); i++){
            textViews.get(i).setText("Session User " + (i+1)  + ": " +session_users.get(i).getSurname() + ", "+session_users.get(i).getName());
        }
        for (int i = session_users.size(); i<3; i++){
            textViews.get(i).setText("Session User " + (i+1) + ": ");
        }
    }

}