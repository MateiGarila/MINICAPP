package com.example.mini_cap;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.mini_cap.controller.DBHelper;
import com.example.mini_cap.model.User;

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
        User user = new User(1, "John", "Doe", 25, "Fair");
        long userIdL = dbHelper.insertUser(user);
        int userId = (int) userIdL;

        // Update the user's details
        User updatedUser = new User(userId, "John", "Smith", 30, "Medium");
        int rowsUpdated = dbHelper.updateUser(userId, updatedUser);

        // Fetch the updated user from the database
        User fetchedUser = dbHelper.getUser(userId);

        // Assert that the update was successful
        assertEquals(1, rowsUpdated);
        assertEquals(updatedUser.getSurname(), fetchedUser.getSurname());
        assertEquals(updatedUser.getName(), fetchedUser.getName());
        assertEquals(updatedUser.getAge(), fetchedUser.getAge());
        assertEquals(updatedUser.getSkinTone(), fetchedUser.getSkinTone());
    }
}