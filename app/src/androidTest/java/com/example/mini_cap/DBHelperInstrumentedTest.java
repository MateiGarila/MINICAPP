package com.example.mini_cap;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.mini_cap.controller.DBHelper;
import com.example.mini_cap.model.Preset;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class DBHelperInstrumentedTest {

    private Context context;
    private DBHelper dbHelper;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        dbHelper = DBHelper.get(context);
    }

    @After
    public void tearDown() {
        dbHelper.close();
    }

    @Test
    public void testUpdateUser() {
        // Insert a test user into the database
        Preset preSet = new Preset(1, "John", 25, "Fair");
        long userIdL = dbHelper.insertPreset(preSet);
        int userId = (int) userIdL;

        // Update the user's details
        Preset updatedPreset = new Preset(userId, "John", 30, "Medium");
        int rowsUpdated = dbHelper.updatePreset(userId, updatedPreset);

        // Fetch the updated user from the database
        Preset fetchedPreset = dbHelper.getPreset(userId);

        // Assert that the update was successful
        assertEquals(1, rowsUpdated);
        assertEquals(updatedPreset.getName(), fetchedPreset.getName());
        assertEquals(updatedPreset.getAge(), fetchedPreset.getAge());
        assertEquals(updatedPreset.getSkinTone(), fetchedPreset.getSkinTone());
    }
}