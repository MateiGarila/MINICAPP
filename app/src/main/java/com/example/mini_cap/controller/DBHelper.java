package com.example.mini_cap.controller;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import androidx.annotation.Discouraged;
import androidx.annotation.Nullable;

import com.example.mini_cap.model.User;

import java.lang.annotation.Documented;
import java.util.ArrayList;

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

    /**
     * Method for inserting a user in the database
     * @param user
     * @return
     */
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
            Toast.makeText(context, "DB Insert Error @ insertUser(): " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }finally {
            db.close();
        }

        return id;
    }

    /**
     * Method used to fetch all users stored in the database
     * @return ArrayList<User> of all available users
     */
    public ArrayList<User> getAllUsers(){

        ArrayList<User> users = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = null;

        try{

            cursor = db.query(Dict.TABLE_USER, null, null, null, null, null, null);

            if(cursor != null){
                if(cursor.moveToFirst()){
                    do{
                        @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex(Dict.COLUMN_USER_ID));
                        @SuppressLint("Range") String surName = cursor.getString(cursor.getColumnIndex(Dict.COLUMN_USER_SURNAME));
                        @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(Dict.COLUMN_USER_NAME));
                        @SuppressLint("Range") int age = cursor.getInt(cursor.getColumnIndex(Dict.COLUMN_USER_AGE));
                        @SuppressLint("Range") String skinTone = cursor.getString(cursor.getColumnIndex(Dict.COLUMN_USER_SKINTONE));

                        users.add(new User(id, surName, name, age, skinTone));

                    }while(cursor.moveToNext());
                }

                cursor.close();
            }

        }catch (Exception e){
            Toast.makeText(context, "DB Fetch Error @ getAllUsers(): " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }finally {
            db.close();
        }

        return users;
    }

    /**
     * Method used to fetch a specific user from the database
     * @param userId id of the fetched user
     * @return User object with the specified id
     */
    public User getUser(int userId) {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = null;
        User user = null;

        try {

            cursor = db.rawQuery("SELECT * FROM " + Dict.TABLE_USER + " WHERE " + Dict.COLUMN_USER_ID + " = ?", new String[]{String.valueOf(userId)});

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {

                        @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex(Dict.COLUMN_USER_ID));
                        @SuppressLint("Range") String surName = cursor.getString(cursor.getColumnIndex(Dict.COLUMN_USER_SURNAME));
                        @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(Dict.COLUMN_USER_NAME));
                        @SuppressLint("Range") int age = cursor.getInt(cursor.getColumnIndex(Dict.COLUMN_USER_AGE));
                        @SuppressLint("Range") String skinTone = cursor.getString(cursor.getColumnIndex(Dict.COLUMN_USER_SKINTONE));

                        user = new User(id, surName, name, age, skinTone);

                    } while (cursor.moveToNext());
                }

                cursor.close();
            }

        } catch (Exception e) {
            Toast.makeText(context, "DB Fetch Error @ getUser(): " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            db.close();
        }

        return user;
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

    public int updateUser(int userId) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        User user = getUser(userId);

        contentValues.put(Dict.COLUMN_USER_SURNAME, user.getSurname());
        contentValues.put(Dict.COLUMN_USER_NAME, user.getName());
        contentValues.put(Dict.COLUMN_USER_AGE, user.getAge());
        contentValues.put(Dict.COLUMN_USER_SKINTONE, user.getSkinTone());

        String selection = Dict.COLUMN_USER_ID + " = ?";
        String[] selectionArgs = {String.valueOf(userId)};

        int rowsUpdated = 0;

        try{
            rowsUpdated = db.update(Dict.TABLE_USER, contentValues, selection, selectionArgs);
        } catch (Exception e) {
            Toast.makeText(context, "DB Update Error @ updateUser(): " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            db.close();
        }

        return rowsUpdated;

    }

    public int deleteUser(int userId) {

        SQLiteDatabase db = this.getWritableDatabase();

        String selection = Dict.COLUMN_USER_ID + " = ?";
        String[] selectionArgs = {String.valueOf(userId)};

        int rowsDeleted = 0;

        try{
            rowsDeleted = db.delete(Dict.TABLE_USER, selection, selectionArgs);
        } catch (Exception e) {
            Toast.makeText(context, "DB Delete Error @ deleteUser(): " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            db.close();
        }

        return rowsDeleted;
    }
}
