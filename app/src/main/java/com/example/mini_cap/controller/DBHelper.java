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

import com.example.mini_cap.model.Preset;

import java.util.ArrayList;

public class DBHelper extends SQLiteOpenHelper {

    private Context context;
    private final String TAG = "DBHelper";

    /**
     * Database constructor
     * @param context
     */
    public DBHelper(@Nullable Context context) {
        super(context, Dict.DATABASE_NAME, null, Dict.DATABASE_VERSION);
    }

    /**
     * onCreate method which creates the "preset" table for this application
     * @param db The database.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {

        //Create preset table
        String CREATE_PRESET_TABLE = "CREATE TABLE " + Dict.TABLE_PRESET + " (" +
                Dict.COLUMN_PRESET_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                Dict.COLUMN_PRESET_NAME + " TEXT NOT NULL, " +
                Dict.COLUMN_PRESET_AGE + " TEXT NOT NULL, " +
                Dict.COLUMN_PRESET_SKINTONE + " TEXT NOT NULL)";

        db.execSQL(CREATE_PRESET_TABLE);

    }

    /**
     * Method for inserting a preset in the database
     * @param preset
     * @return
     */
    public long insertPreSet(Preset preset){

        //Anything goes wrong and we see -1. This is what is causing the issue
        long id = -1;

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        //In this case we do not insert an id as the database will take care of it
        contentValues.put(Dict.COLUMN_PRESET_NAME, preset.getName());
        contentValues.put(Dict.COLUMN_PRESET_AGE, preset.getAge());
        contentValues.put(Dict.COLUMN_PRESET_SKINTONE, preset.getSkinTone());

        try{
            id = db.insertOrThrow(Dict.TABLE_PRESET, null, contentValues);
        }catch(Exception e){
            Toast.makeText(context, "DB Insert Error @ insertPreset(): " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }finally {
            db.close();
        }

        return id;
    }

    /**
     * Method used to fetch all presets stored in the database
     * @return ArrayList<Preset> of all available Presets
     */
    public ArrayList<Preset> getAllPresets(){

        ArrayList<Preset> presets = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = null;

        try{

            cursor = db.query(Dict.TABLE_PRESET, null, null, null, null, null, null);

            if(cursor != null){
                if(cursor.moveToFirst()){
                    do{
                        @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex(Dict.COLUMN_PRESET_ID));
                        @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(Dict.COLUMN_PRESET_NAME));
                        @SuppressLint("Range") int age = cursor.getInt(cursor.getColumnIndex(Dict.COLUMN_PRESET_AGE));
                        @SuppressLint("Range") String skinTone = cursor.getString(cursor.getColumnIndex(Dict.COLUMN_PRESET_SKINTONE));

                        presets.add(new Preset(id, name, age, skinTone));

                    }while(cursor.moveToNext());
                }

                cursor.close();
            }

        }catch (Exception e){
            Toast.makeText(context, "DB Fetch Error @ getAllPresets(): " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }finally {
            db.close();
        }

        return presets;
    }

    /**
     * Method used to fetch a specific Preset from the database
     * @param presetId id of the fetched Preset
     * @return Preset object with the specified id
     */
    public Preset getPreSet(int presetId) {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = null;
        Preset preset = null;

        try {

            cursor = db.rawQuery("SELECT * FROM " + Dict.TABLE_PRESET + " WHERE " + Dict.COLUMN_PRESET_ID + " = ?", new String[]{String.valueOf(presetId)});

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {

                        @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex(Dict.COLUMN_PRESET_ID));
                        @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(Dict.COLUMN_PRESET_NAME));
                        @SuppressLint("Range") int age = cursor.getInt(cursor.getColumnIndex(Dict.COLUMN_PRESET_AGE));
                        @SuppressLint("Range") String skinTone = cursor.getString(cursor.getColumnIndex(Dict.COLUMN_PRESET_SKINTONE));

                        preset = new Preset(id,name, age, skinTone);

                    } while (cursor.moveToNext());
                }

                cursor.close();
            }

        } catch (Exception e) {
            Toast.makeText(context, "DB Fetch Error @ getPreSet(): " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            db.close();
        }

        return preset;
    }

    /**
     * Method used to update a specific preset from the database
     * @param presetId the ID of the preset to be updated
     * @param updatedPreset new values for the updated preset
     * @return
     */
    public int updatePreset(int presetId, Preset updatedPreset) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        Log.d(TAG, "Made it in update Preset");
        contentValues.put(Dict.COLUMN_PRESET_NAME, updatedPreset.getName());
        contentValues.put(Dict.COLUMN_PRESET_AGE, updatedPreset.getAge());
        contentValues.put(Dict.COLUMN_PRESET_SKINTONE, updatedPreset.getSkinTone());

        String selection = Dict.COLUMN_PRESET_ID + " = ?";
        String[] selectionArgs = {String.valueOf(presetId)};

        int rowsUpdated = 0;

        try{
            rowsUpdated = db.update(Dict.TABLE_PRESET, contentValues, selection, selectionArgs);
        } catch (Exception e) {
            Toast.makeText(context, "DB Update Error @ updatePreset(): " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            db.close();
        }

        return rowsUpdated;

    }

    /**
     * This method is used to remove a preset from the database
     * @param presetId the ID of the preset about to be deleted
     * @return
     */
    public int deletePreset(int presetId) {

        SQLiteDatabase db = this.getWritableDatabase();

        String selection = Dict.COLUMN_PRESET_ID + " = ?";
        String[] selectionArgs = {String.valueOf(presetId)};

        int rowsDeleted = 0;

        try{
            rowsDeleted = db.delete(Dict.TABLE_PRESET, selection, selectionArgs);
        } catch (Exception e) {
            Toast.makeText(context, "DB Delete Error @ deletePreset(): " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            db.close();
        }

        return rowsDeleted;
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
        db.execSQL("DROP TABLE IF EXISTS " + Dict.TABLE_PRESET);
        //db.execSQL("DROP TABLE IF EXISTS " + Dict.);
        onCreate(db);

    }
}
