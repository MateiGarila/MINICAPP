package com.example.mini_cap;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.mini_cap.controller.DBHelper;
import com.example.mini_cap.model.PreSet;

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
        dbHelper = new DBHelper(context);
    }

    @After
    public void tearDown() {
        dbHelper.close();
    }

    @Test
    public void testUpdateUser() {
        // Insert a test user into the database
        PreSet preSet = new PreSet(1, "John", 25, "Fair");
        long userIdL = dbHelper.insertPreSet(preSet);
        int userId = (int) userIdL;

        // Update the user's details
        PreSet updatedPreSet = new PreSet(userId, "John", 30, "Medium");
        int rowsUpdated = dbHelper.updatePreSet(userId, updatedPreSet);

        // Fetch the updated user from the database
        PreSet fetchedPreSet = dbHelper.getPreSet(userId);

        // Assert that the update was successful
        assertEquals(1, rowsUpdated);
        assertEquals(updatedPreSet.getName(), fetchedPreSet.getName());
        assertEquals(updatedPreSet.getAge(), fetchedPreSet.getAge());
        assertEquals(updatedPreSet.getSkinTone(), fetchedPreSet.getSkinTone());
    }
}