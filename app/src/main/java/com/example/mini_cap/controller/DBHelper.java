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
import com.example.mini_cap.model.Stats;
import com.example.mini_cap.model.Day;

import app.uvtracker.data.optical.OpticalRecord;
import app.uvtracker.data.optical.TimedOpticalRecord;
import app.uvtracker.data.optical.Timestamp;
import app.uvtracker.sensor.pii.connection.application.event.SyncDataReceivedEvent;
import app.uvtracker.sensor.pii.event.EventHandler;
import app.uvtracker.sensor.pii.event.IEventListener;

import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper implements IEventListener{

    private static final boolean DEBUG_READ_ALS = true;

    private final Context context;
    private final String TAG = "DBHelper";

    private static final int interval = 10;
    private static final int offset = 8;

    /**
     * Database constructor
     * @param context
     */
    public DBHelper(@Nullable Context context) {
        super(context, Dict.DATABASE_NAME, null, Dict.DATABASE_VERSION);
        this.context = context;
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

        //Create stats table
        String CREATE_STATS_TABLE = "CREATE TABLE " + Dict.TABLE_STATS + " (" +
                Dict.COLUMN_TIMESTAMP + " BIGINT PRIMARY KEY NOT NULL, " + // Make timestamp the primary key
                Dict.COLUMN_UVINDEX + " TEXT NOT NULL)";

        String CREATE_TIMESTAMP_INDEX = "CREATE UNIQUE INDEX idx_timestamp ON " +
                Dict.TABLE_STATS + "(" + Dict.COLUMN_TIMESTAMP + ")";

        db.execSQL(CREATE_STATS_TABLE);
        db.execSQL(CREATE_TIMESTAMP_INDEX);

    }

    /**
     * Method for inserting a preset in the database
     * @param preset
     * @return
     */
    public long insertPreset(Preset preset){

        //Anything goes wrong and we see -1. It means that Preset was not inserted
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
    public Preset getPreset(int presetId) {

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
     * Function for inserting new stats into the db
     * @param records
     * @return
     */
    public long insertStats(List<TimedOpticalRecord> records) {
        // If -1 is returned, function did not insert stats into db.
        long id = -1;

        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction(); // Begin the transaction

        try {
            for (TimedOpticalRecord record : records) {
                ContentValues contentValues = new ContentValues();
                Timestamp timestamp = record.getTimestamp();
                Day date = new Day(timestamp.getDay());

                Long primaryKey = date.toDatabaseNumber() + timestamp.getSampleNumber();

                String dataString = record.getData().flatten();

                contentValues.put(Dict.COLUMN_TIMESTAMP, primaryKey);
                contentValues.put(Dict.COLUMN_UVINDEX, dataString);

                // Insert the record into the database with CONFLICT_IGNORE
                id = db.insertWithOnConflict(Dict.TABLE_STATS, null, contentValues, SQLiteDatabase.CONFLICT_IGNORE);
            }

            db.setTransactionSuccessful(); // Mark the transaction as successful
        } catch (Exception e) {
            Toast.makeText(context, "DB Insert Error @ insertStats(): " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            db.endTransaction(); // End the transaction
            db.close();
        }

        return id;
    }

    /**
     * Function for returning Stats for a given hour on a given date
     * @param timestamp must be in form "yyyy/mm/dd-sampleNumber"
     * @return Stats object with data for timestamp entered
      */
    public Stats getStats(Long timestamp) {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = null;
        Stats stats = null;

        try{
            cursor = db.rawQuery("SELECT * FROM " + Dict.TABLE_STATS + " WHERE " + Dict.COLUMN_TIMESTAMP + " = ?", new String[]{String.valueOf(timestamp)});

            if (cursor != null){
                if (cursor.moveToFirst()){
                    do{
                        @SuppressLint("Range") String exposure = cursor.getString(cursor.getColumnIndex(Dict.COLUMN_UVINDEX));

                        stats = new Stats(exposure, timestamp);
                    } while(cursor.moveToNext());
                }

                cursor.close();
            }

            // Check if the cursor is empty, set stats to zero if no records are found.
            if (stats == null) {
                // Assuming the Stats constructor takes 0 values for id and exposure as well.
                stats = new Stats("0", timestamp);
            }

        }catch (Exception e){
            Toast.makeText(context, "DB Fetch Error @ getStatsForHour(): " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }finally{
            db.close();
        }

        return stats;

    }

    /**
     * Function for returning Stats for a given day
     * @param day must be in form "yyyy/mm/dd"
     * @return Stats object with data for timestamp entered
     */
    public float getDailyAvg(Day day){
        Long dateLong = day.toDatabaseNumber();
        SQLiteDatabase db = this.getReadableDatabase();
        List<Stats> statsList = new ArrayList<>();

        Cursor cursor;

        try {
            // The SQL query to select all records where the date matches the given day
            cursor = db.rawQuery("SELECT * FROM " + Dict.TABLE_STATS + " WHERE " + Dict.COLUMN_TIMESTAMP + " >= ? AND " + Dict.COLUMN_TIMESTAMP + " < ?",
                    new String[]{String.valueOf(dateLong), String.valueOf(dateLong + (24 * 60 * 60 )/interval)});

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        @SuppressLint("Range") long timestamp = cursor.getLong(cursor.getColumnIndex(Dict.COLUMN_TIMESTAMP));
                        @SuppressLint("Range") String exposure = cursor.getString(cursor.getColumnIndex(Dict.COLUMN_UVINDEX));

                        // Create a Stats object and add it to the list
                        statsList.add(new Stats(exposure, timestamp));
                    } while (cursor.moveToNext());
                }

                cursor.close();
            }

        } catch (Exception e) {
            Toast.makeText(context, "DB Fetch Error @ getStatsForDay(): " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            db.close();
        }

        float sum = 0.0f, avg;

        for(Stats stats : statsList){
            OpticalRecord opticalRecord = OpticalRecord.unflatten(stats.getExposure());
            if (opticalRecord != null) {
                sum += DEBUG_READ_ALS ? opticalRecord.illuminance : opticalRecord.uvIndex;
            }
        }
        int sampleCount = statsList.size();
        avg = sum/sampleCount;

        return avg;
    }

    public List<Stats> getStatsBetweenTimestamps(long timestamp1, long timestamp2) {
        SQLiteDatabase db = this.getReadableDatabase();
        List<Stats> statsList = new ArrayList<>();

        Cursor cursor;

        try {
            cursor = db.rawQuery("SELECT * FROM " + Dict.TABLE_STATS +
                            " WHERE " + Dict.COLUMN_TIMESTAMP + " >= ? AND " + Dict.COLUMN_TIMESTAMP + " < ?",
                    new String[]{String.valueOf(timestamp1), String.valueOf(timestamp2)});

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        @SuppressLint("Range") String exposure = cursor.getString(cursor.getColumnIndex(Dict.COLUMN_UVINDEX));

                        // Get the timestamp from the cursor
                        @SuppressLint("Range") long timestamp = cursor.getLong(cursor.getColumnIndex(Dict.COLUMN_TIMESTAMP));

                        statsList.add(new Stats(exposure, timestamp));
                    } while (cursor.moveToNext());
                }

                cursor.close();
            }
        } catch (Exception e) {
            Toast.makeText(context, "DB Fetch Error @ getStatsBetweenTimestamps(): " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            db.close();
        }

        return statsList;
    }



    /**
     * Function for returning the UV exposure for a specified day
     * @param date is a date object, hour is concatenated to date string to create full timestamp
     * @return hourly avg exposure from 8 am to 6 pm
     */
    public float getMinuteAvg(Day date, int minute, int hour){
        int minuteSample = Math.round((float)(3600 * hour + 60 * minute) / (float)interval);
        int nextMinuteSample = Math.round((float)(3600 * hour + 60 * (minute+1))/ (float)interval);

        Long timestamp1 = date.toDatabaseNumber() + minuteSample;
        Long timestamp2 = date.toDatabaseNumber() + nextMinuteSample;

        List<Stats> statsList = getStatsBetweenTimestamps(timestamp1, timestamp2);


        // Calculate the average exposure for the given minute range if needed
        float sum = 0.0f, avg;

        for(Stats stats : statsList){
            OpticalRecord opticalRecord = OpticalRecord.unflatten(stats.getExposure());
            if (opticalRecord != null) {
                sum += DEBUG_READ_ALS ? opticalRecord.illuminance : opticalRecord.uvIndex;
            }
        }
        int sampleCount = statsList.size();
        avg = sampleCount > 0 ? sum / sampleCount : 0.0f;

        return avg;
    }

    public float getHourlyAvg(Day date, int hour) {
        float hourlyAvg = 0.0f;

        // Get hour sample numbers and query the DB

        int nextHour = hour + 1;
        int hourSample = (hour*3600)/interval;
        int nextHourSample = (nextHour*3600)/interval;
        Long timestamp1 = date.toDatabaseNumber() + hourSample;
        Long timestamp2 = date.toDatabaseNumber() + nextHourSample;

        Log.d("timestamp1", String.valueOf(timestamp1));
        Log.d("timestamp2", String.valueOf(timestamp2));

        List<Stats> statsList = getStatsBetweenTimestamps(timestamp1, timestamp2);

        // Calculate the average exposure for the given minute range if needed
        float sum = 0.0f, avg;

        for (Stats stats : statsList) {
            OpticalRecord opticalRecord = OpticalRecord.unflatten(stats.getExposure());
            if (opticalRecord != null) {
                sum += DEBUG_READ_ALS ? opticalRecord.illuminance : opticalRecord.uvIndex;
            }
        }

        int sampleCount = statsList.size();
        avg = sampleCount > 0 ? sum / sampleCount : 0.0f;

        return avg;
    }


    /**
     * Function for returning the UV exposure for a specified day
     * @param date must be in form "yyyy/MM/dd"
     * @return array of hourly UV exposure floats for specified day from 8 am to 6 pm
     */
    public float[] getExposureForDay(Day date){

        float[] dailyExposure = new float[11];

        for (int i = 8; i < 19; i++) {
            int index = i - 8;

            dailyExposure[index] = getHourlyAvg(date, i);

            Log.d("Call hourly avg", String.valueOf(date));

        }

        return dailyExposure;

    }

    /**
     * This method is called when upgrading the database
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
    @EventHandler
    public void syncDataReceived(SyncDataReceivedEvent syncDataReceivedEvent){
        List<TimedOpticalRecord> data = syncDataReceivedEvent.getData();

        if(data.size() == 0) Log.d(TAG, "[Sync] Data size: 0.");
        else Log.d(TAG, String.format("[Sync] Data size: %d; first: %s; last: %s.", data.size(), data.get(0), data.get(data.size() - 1)));

        insertStats(data);

    }

}
