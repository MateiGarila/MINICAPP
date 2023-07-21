package com.example.mini_cap.controller;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.mini_cap.model.User;
import com.example.mini_cap.model.Stats;

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

        //Create stats table
        String CREATE_STATS_TABLE = "CREATE TABLE " + Dict.TABLE_STATS + " (" +
                Dict.COLUMN_LOGID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                Dict.COLUMN_TIMESTAMP + " TEXT NOT NULL, " +
                Dict.COLUMN_EXPOSURE + " TEXT NOT NULL)";

        db.execSQL(CREATE_STATS_TABLE);

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
        db.execSQL("DROP TABLE IF EXISTS " + Dict.TABLE_STATS);
        onCreate(db);

    }

    /**
     * Function for updating an already entered user in the db
     * @param userId ID for the user to be updated
     * @param updatedUser user object containing updated user information
     * @return int for rows updated, if non-zero update was successful
     */
    public int updateUser(int userId, User updatedUser) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(Dict.COLUMN_USER_SURNAME, updatedUser.getSurname());
        contentValues.put(Dict.COLUMN_USER_NAME, updatedUser.getName());
        contentValues.put(Dict.COLUMN_USER_AGE, updatedUser.getAge());
        contentValues.put(Dict.COLUMN_USER_SKINTONE, updatedUser.getSkinTone());

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

    /**
     * Function for deleting a user in the db
     * @param userId ID for the user to be deleted
     * @return int for rows deleted, if non-zero delete was successful
     */
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

    /**
     * Function for inserting new stats into the db
     * @param stats
     * @return
     */

    public long insertStats(Stats stats){

        // If -1 is returned, function did not insert stats into db.
        long id = -1;

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        // ID is incremented by the db instead of inserted
        contentValues.put(Dict.COLUMN_TIMESTAMP, stats.getTimestamp());
        contentValues.put(Dict.COLUMN_EXPOSURE, stats.getExposure());

        try{
            id = db.insertOrThrow(Dict.TABLE_STATS, null, contentValues);
        }catch(Exception e){
            Toast.makeText(context, "DB Insert Error @ insertStats(): " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }finally{
            db.close();
        }

    return id;
    }
    /**
     * Function for returning Stats for a given hour on a given date
     * @param timestamp must be in form "dd/MM/yyyy HH:mm:ss"
     * @return Stats object with data for timestamp entered
      */

    public Stats getStatsForHour(String timestamp) {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = null;
        Stats stats = null;

        try{
            cursor = db.rawQuery("SELECT * FROM " + Dict.TABLE_STATS + " WHERE " + Dict.COLUMN_TIMESTAMP + " = ?", new String[]{String.valueOf(timestamp)});

            if (cursor != null){
                if (cursor.moveToFirst()){
                    do{
                        @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex(Dict.COLUMN_LOGID));
                        @SuppressLint("Range") float exposure = cursor.getFloat(cursor.getColumnIndex(Dict.COLUMN_EXPOSURE));

                        stats = new Stats(id, exposure, timestamp);
                    } while(cursor.moveToNext());
                }

                cursor.close();
            }

            // Check if the cursor is empty, set stats to zero if no records are found.
            if (stats == null) {
                // Assuming the Stats constructor takes 0 values for id and exposure as well.
                stats = new Stats(0, 0.0f, timestamp);
            }

        }catch (Exception e){
            Toast.makeText(context, "DB Fetch Error @ getStatsForHour(): " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }finally{
            db.close();
        }

        return stats;

    }

    /**
     * Function for returning the UV exposure for a specified day
     * @param date must be in form "dd/MM/yyyy", hour is concatenated to date string to create full timestamp
     * @return array of hourly UV exposure floats for specified day from 8 am to 6 pm
     */

    public float[] getExposureForDay(String date){

        float[] dailyExposure = new float[11];

        for(int i = 8; i<19; i++){
            int index = i - 8;
            @SuppressLint("DefaultLocale") String timestamp = String.format("%s %02d:00:00", date, i);
            dailyExposure[index] = getStatsForHour(timestamp).getExposure();
        }

        return dailyExposure;

    }

}
