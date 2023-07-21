package app.uvtracker.sensor.pii;

import androidx.annotation.NonNull;

import java.util.Date;

import app.uvtracker.sensor.pii.connection.application.ISensorConnection;

public interface ISensor {

    @NonNull
    String getAddress();

    @NonNull
    String getName();

    int getRssi();

    @NonNull
    Date getLastSeenAt();

    @NonNull
    ISensorConnection getConnection();

}
