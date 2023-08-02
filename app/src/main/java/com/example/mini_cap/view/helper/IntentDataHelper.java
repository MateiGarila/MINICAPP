package com.example.mini_cap.view.helper;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.mini_cap.controller.SensorController;

public class IntentDataHelper {

    @SuppressLint("StaticFieldLeak")
    @Nullable
    private static SensorController sensorController;

    @Nullable
    public static SensorController readSensorController() {
        SensorController inst = IntentDataHelper.sensorController;
        IntentDataHelper.sensorController = null; // nulling out this reference prevents memory leak
        return inst;
    }

    public static void writeSensorController(@NonNull SensorController sensorController) {
        IntentDataHelper.sensorController = sensorController;
    }

}
