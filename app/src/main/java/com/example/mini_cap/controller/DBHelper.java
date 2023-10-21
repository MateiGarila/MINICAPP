package com.example.mini_cap.controller;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
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

    private static final int INTERVAL = 10;
    private final Context context;
    private final String TAG = "DBHelper";

    @SuppressLint("StaticFieldLeak")
    private static DBHelper instance;

    public static DBHelper get(@NonNull Context context) {
        if(DBHelper.instance == null)
            DBHelper.instance = new DBHelper(context);
        return DBHelper.instance;
    }

    public static void release() {
        DBHelper.instance = null;
    }

    public DBHelper(@Nullable Context context) {
        super(context, Dict.DATABASE_NAME, null, Dict.DATABASE_VERSION);
        this.context = context;
    }

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

    public ArrayList<Preset> getAllPresets(){

        ArrayList<Preset> presets = new ArrayList<>();

        try (SQLiteDatabase db = this.getReadableDatabase()) {
            Cursor cursor;

            cursor = db.query(Dict.TABLE_PRESET, null, null, null, null, null, null);

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex(Dict.COLUMN_PRESET_ID));
                        @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(Dict.COLUMN_PRESET_NAME));
                        @SuppressLint("Range") int age = cursor.getInt(cursor.getColumnIndex(Dict.COLUMN_PRESET_AGE));
                        @SuppressLint("Range") String skinTone = cursor.getString(cursor.getColumnIndex(Dict.COLUMN_PRESET_SKINTONE));

                        presets.add(new Preset(id, name, age, skinTone));

                    } while (cursor.moveToNext());
                }

                cursor.close();
            }

        } catch (Exception e) {
            Toast.makeText(context, "DB Fetch Error @ getAllPresets(): " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        return presets;
    }

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

    public void wipeStatsTable() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE from " + Dict.TABLE_STATS);
        db.close();
    }

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

    public float getMinuteAvg(Day date, int minute, int hour, boolean getALS) {
        int minuteSample = Math.round((float)(3600 * hour + 60 * minute) / (float) INTERVAL);
        int nextMinuteSample = Math.round((float)(3600 * hour + 60 * (minute+1))/ (float) INTERVAL);

        long timestamp1 = date.toDatabaseNumber() + minuteSample;
        long timestamp2 = date.toDatabaseNumber() + nextMinuteSample;

        List<Stats> statsList = getStatsBetweenTimestamps(timestamp1, timestamp2);

        float sum = 0.0f;
        int counter = 0;
        for(Stats stats : statsList){
            OpticalRecord opticalRecord = OpticalRecord.unflatten(stats.getExposure());
            if (opticalRecord != null) {
                sum += getALS ? opticalRecord.illuminance : opticalRecord.uvIndex;
                counter++;
            }
        }
        if(counter == 0) return Float.NaN;
        return sum / counter;
    }

    public float getHourlyAvg(Day date, int hour, boolean getALS) {
        int hourSample = Math.round((float)(hour * 3600)/ (float)INTERVAL);
        int nextHourSample = Math.round((float)((hour + 1) * 3600)/ (float)INTERVAL);

        long timestamp1 = date.toDatabaseNumber() + hourSample;
        long timestamp2 = date.toDatabaseNumber() + nextHourSample;

        List<Stats> statsList = getStatsBetweenTimestamps(timestamp1, timestamp2);

        float sum = 0.0f;
        int counter = 0;
        for (Stats stats : statsList) {
            OpticalRecord opticalRecord = OpticalRecord.unflatten(stats.getExposure());
            if (opticalRecord != null) {
                sum += getALS ? opticalRecord.illuminance : opticalRecord.uvIndex;
                counter++;
            }
        }
        if(counter == 0) return Float.NaN;
        return sum / counter;
    }

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
