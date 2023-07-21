package app.uvtracker.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Date;
import java.util.Map;

import app.uvtracker.data.type.Record;

public interface IPersistentDataAccess {

    void clearDB();

    void writeHourlyAverage(@NonNull Date time, @NonNull Record record);

    @Nullable
    Record readHourlyAverage(@NonNull Date time);

    @NonNull
    Map<Date, Record> readAllHourlyAverage();

}
