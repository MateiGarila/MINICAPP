package com.example.mini_cap.controller;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.mini_cap.model.User;

public class DBHelper extends SQLiteOpenHelper {

    private Context context;

    /**
     * Database constructor
     * @param context
     */
    public DBHelper(@Nullable Context context) {
        super(context, Dict.DATABASE_NAME, null, Dict.DATABASE_VERSION);
    }

    /**
     * onCreate method which creates the "user" table for this application
     * @param db The database.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {

        //Create user table
        String CREATE_USER_TABLE = "CREATE TABLE " + Dict.TABLE_USER + " (" +
                Dict.COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                Dict.COLUMN_USER_SURNAME + " TEXT NOT NULL, " +
                Dict.COLUMN_USER_NAME + " TEXT NOT NULL, " +
                Dict.COLUMN_USER_AGE + " TEXT NOT NULL, " +
                Dict.COLUMN_USER_SKINTONE + " TEXT NOT NULL)";

        db.execSQL(CREATE_USER_TABLE);

    }

    public long insertUser(User user){

        //Anything goes wrong and we see -1. This is what is causing the issue
        long id = -1;

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        //In this case we do not insert an id as the database will take care of it
        contentValues.put(Dict.COLUMN_USER_SURNAME, user.getSurname());
        contentValues.put(Dict.COLUMN_USER_NAME, user.getName());
        contentValues.put(Dict.COLUMN_USER_AGE, user.getAge());
        contentValues.put(Dict.COLUMN_USER_SKINTONE, user.getSkinTone());

        try{
            id = db.insertOrThrow(Dict.TABLE_USER, null, contentValues);
        }catch(Exception e){
            Toast.makeText(context, "DB Insert Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }finally {
            db.close();
        }

        return id;
    }



    /**
     * In case of database update
     * @param db The database.
     * @param oldVersion The old database version.
     * @param newVersion The new database version.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        //drop existing tables
        db.execSQL("DROP TABLE IF EXISTS " + Dict.TABLE_USER);
        //db.execSQL("DROP TABLE IF EXISTS " + Dict.);
        onCreate(db);

    }
}
