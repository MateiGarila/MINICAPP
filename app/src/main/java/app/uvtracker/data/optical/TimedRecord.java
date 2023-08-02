package app.uvtracker.data.optical;

import androidx.annotation.NonNull;

import java.util.Date;

public class TimedRecord<T> {

    @NonNull
    private final T data;

    @NonNull
    private final Date day;

    @NonNull
    private final Date instant;

    private final int sampleNumber;
    private final int sampleInterval;

    public TimedRecord(@NonNull T object, long deviceBootTime, int globalSampleNumber, int sampleInterval) {
        this.data = object;
        this.sampleInterval = sampleInterval;
        this.instant = new Date(deviceBootTime + (long)globalSampleNumber * sampleInterval * 1000);
        int year = this.instant.getYear() + 1900;
        int month = this.instant.getMonth() + 1;
        int date = this.instant.getDate();
        this.day = new Date(year - 1900, month - 1, date);
        int midnightSampleNumber = (int)Math.ceil((double)(this.day.getTime() - deviceBootTime) / (double)(sampleInterval * 1000));
        this.sampleNumber = globalSampleNumber - midnightSampleNumber;
    }

    @NonNull
    public T getData() {
        return this.data;
    }

    @NonNull
    public Date getDay() {
        return this.day;
    }

    @NonNull
    public Date getInstant() {
        return this.instant;
    }

    public int getSampleNumber() {
        return this.sampleNumber;
    }

    public int getSampleInterval() {
        return this.sampleInterval;
    }

    @Override
    @NonNull
    public String toString() {
        return String.format("%d/%02d/%02d-%02d:%02d:%02d(%d): %s",
                this.instant.getYear() + 1900,
                this.instant.getMonth() + 1,
                this.instant.getDate(),
                this.instant.getHours(),
                this.instant.getMinutes(),
                this.instant.getSeconds(),
                this.sampleNumber,
                this.data
        );
    }

}
