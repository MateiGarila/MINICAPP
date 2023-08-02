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

import app.uvtracker.data.optical.OpticalRecord;
import app.uvtracker.data.optical.TimedRecord;
import app.uvtracker.sensor.pii.connection.application.event.SyncDataReceivedEvent;
import app.uvtracker.sensor.pii.event.EventHandler;
import app.uvtracker.sensor.pii.event.IEventListener;

import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper implements IEventListener{

    private Context context;
    private final String TAG = "DBHelper";

    private static int interval = 10;
    private static int offset = 8;

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
                Dict.COLUMN_LOGID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                Dict.COLUMN_TIMESTAMP + " TEXT NOT NULL, " +
                Dict.COLUMN_UVINDEX + " TEXT NOT NULL)";

        db.execSQL(CREATE_STATS_TABLE);

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
     * Function for inserting new stats into the db
     * @param record
     * @return
     */
    public long insertStats(TimedRecord<OpticalRecord> record){

        // If -1 is returned, function did not insert stats into db.
        long id = -1;

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();


        @SuppressLint("DefaultLocale")
        String primaryKey = String.format("%d/%02d/%02d-%d",
                record.getDay().getYear() + 1900,
                record.getDay().getMonth() + 1,
                record.getDay().getDate(),
                record.getSampleNumber()
        );

        String dataString = record.getData().flatten();

        // ID is incremented by the db instead of inserted
        // primaryKey is in form yyyy/mm/dd-sampleNumber
        contentValues.put(Dict.COLUMN_TIMESTAMP, primaryKey);
        contentValues.put(Dict.COLUMN_UVINDEX, dataString);

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
     * @param timestamp must be in form "yyyy/mm/dd-sampleNumber"
     * @return Stats object with data for timestamp entered
      */
    public Stats getStats(String timestamp) {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = null;
        Stats stats = null;

        try{
            cursor = db.rawQuery("SELECT * FROM " + Dict.TABLE_STATS + " WHERE " + Dict.COLUMN_TIMESTAMP + " = ?", new String[]{String.valueOf(timestamp)});

            if (cursor != null){
                if (cursor.moveToFirst()){
                    do{
                        @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex(Dict.COLUMN_LOGID));
                        @SuppressLint("Range") String exposure = cursor.getString(cursor.getColumnIndex(Dict.COLUMN_UVINDEX));

                        stats = new Stats(id, exposure, timestamp);
                    } while(cursor.moveToNext());
                }

                cursor.close();
            }

            // Check if the cursor is empty, set stats to zero if no records are found.
            if (stats == null) {
                // Assuming the Stats constructor takes 0 values for id and exposure as well.
                stats = new Stats(0, "0", timestamp);
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
     * @param date must be in form "yyyy/mm/dd"
     * @return Stats object with data for timestamp entered
     */
    public float getDailyAvg(String date){
        SQLiteDatabase db = this.getReadableDatabase();
        List<Stats> statsList = new ArrayList<>();

        Cursor cursor = null;

        try {
            // The SQL query to select all records where the date matches the given day
            cursor = db.rawQuery("SELECT * FROM " + Dict.TABLE_STATS + " WHERE SUBSTR(" + Dict.COLUMN_TIMESTAMP + ", 1, 10) = ?", new String[]{date});

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex(Dict.COLUMN_LOGID));
                        @SuppressLint("Range") String timestamp = cursor.getString(cursor.getColumnIndex(Dict.COLUMN_TIMESTAMP));
                        @SuppressLint("Range") String exposure = cursor.getString(cursor.getColumnIndex(Dict.COLUMN_UVINDEX));

                        // Create a Stats object and add it to the list
                        statsList.add(new Stats(id, timestamp, exposure));
                    } while (cursor.moveToNext());
                }

                cursor.close();
            }

        } catch (Exception e) {
            Toast.makeText(context, "DB Fetch Error @ getStatsForDay(): " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            db.close();
        }

        float avg = 0.0f;
        // For number of records in a minute = (60/record interval)
        // In an hour = (Records in a min) * 60
        // In a day = (Records in an hour) * 24
        int dailyRecords = (60/interval)*1440;
        for(Stats stats : statsList){
            avg += Float.parseFloat(stats.getExposure());
        }

        return avg/dailyRecords;
    }

    public List<Stats> getStatsBetweenTimestamps(String timestamp1, String timestamp2) {
        SQLiteDatabase db = this.getReadableDatabase();
        List<Stats> statsList = new ArrayList<>();

        Cursor cursor = null;

        try {
            cursor = db.rawQuery("SELECT * FROM " + Dict.TABLE_STATS +
                            " WHERE " + Dict.COLUMN_TIMESTAMP + " >= ? AND " + Dict.COLUMN_TIMESTAMP + " < ?",
                    new String[]{timestamp1, timestamp2});

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex(Dict.COLUMN_LOGID));
                        @SuppressLint("Range") String exposure = cursor.getString(cursor.getColumnIndex(Dict.COLUMN_UVINDEX));

                        // Get the timestamp from the cursor
                        @SuppressLint("Range") String timestamp = cursor.getString(cursor.getColumnIndex(Dict.COLUMN_TIMESTAMP));

                        statsList.add(new Stats(id, exposure, timestamp));
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
     * @param date must be in form "yyyy/mm/dd", hour is concatenated to date string to create full timestamp
     * @return hourly avg exposure from 8 am to 6 pm
     */
    public float getMinuteAvg(String date, int minute, int hour){
        int minuteSample = Math.round((float)(3600 * hour + 60 * minute) / (float)interval);
        int nextMinuteSample = Math.round((float)3600 * hour + 60 * (minute+1)/ (float)interval);

        String timestamp1 = date + "-" + minuteSample;
        String timestamp2 = date + "-" + nextMinuteSample;

        List<Stats> statsList = getStatsBetweenTimestamps(timestamp1, timestamp2);


        // Calculate the average exposure for the given minute range if needed
        float avg = 0.0f;
        int minuteRecords = (60/interval);

        for(Stats stats : statsList){
            avg += Float.parseFloat(stats.getExposure());
        }

        return avg/minuteRecords;
    }

    public float getHourlyAvg(String date, int hour){
        float[] minuteAvgs = new float[60];

        // Get minute averages for each minute in the hour
        for (int i = 0; i < 60; i++) {
            minuteAvgs[i] = getMinuteAvg(date, hour, i);
        }

        // Calculate the hourly average
        float totalMinuteAvg = 0.0f;
        for (float minuteAvg : minuteAvgs) {
            totalMinuteAvg += minuteAvg;
        }

        float hourlyAvg = totalMinuteAvg / 60.0f;
        return hourlyAvg;
    }

    /**
     * Function for returning the UV exposure for a specified day
     * @param date must be in form "yyyy/MM/dd"
     * @return array of hourly UV exposure floats for specified day from 8 am to 6 pm
     */
    public float[] getExposureForDay(String date){

        float[] dailyExposure = new float[11];

        for (int i = 8; i < 19; i++) {
            int index = i - 8;

            dailyExposure[index] = getHourlyAvg(date, i);

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
        List<TimedRecord<OpticalRecord>> data = syncDataReceivedEvent.getData();

        if(data.size() == 0) Log.d(TAG, "[Sync] Data size: 0.");
        else Log.d(TAG, String.format("[Sync] Data size: %d; first: %s; last: %s.", data.size(), data.get(0), data.get(data.size() - 1)));

        for(TimedRecord<OpticalRecord> record : data) {
            this.insertStats(record);
        }
    }

}
