package app.uvtracker.data.optical;

import androidx.annotation.NonNull;

import java.util.Date;
import java.util.Locale;

public class Timestamp {

    @NonNull
    private final Date day;

    @NonNull
    private final Date instant;

    private final int sampleNumber;
    private final int sampleInterval;

    public Timestamp(@NonNull Date day, int sampleNumber, int sampleInterval) {
        this.sampleNumber = sampleNumber;
        this.sampleInterval = sampleInterval;
        this.day = new Date(day.getYear(), day.getMonth(), day.getDate());
        this.instant = new Date(this.day.getTime() + (long)sampleNumber * sampleInterval * 1000);
    }

    public Timestamp(@NonNull Date instant, int sampleInterval) {
        int seconds =
                    instant.getHours() * 3600
                +   instant.getMinutes() * 60
                +   instant.getSeconds();
        int sampleNumber = (int)Math.round((double)seconds / (double)sampleInterval);
        this.sampleNumber = sampleNumber;
        this.sampleInterval = sampleInterval;
        this.day = new Date(instant.getYear(), instant.getMonth(), instant.getDate());
        this.instant = new Date(this.day.getTime() + (long)sampleNumber * sampleInterval * 1000);
    }

    public Timestamp(long deviceBootTime, int globalSampleNumber, int sampleInterval) {
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

    @NonNull
    public String toShortString() {
        return String.format(Locale.getDefault(),
                "%d-%02d-%02d-%d",
                this.instant.getYear() + 1900,
                this.instant.getMonth() + 1,
                this.instant.getDate(),
                this.sampleNumber
        );
    }

    @Override
    @NonNull
    public String toString() {
        return String.format(Locale.getDefault(),
                "%d/%02d/%02d-%02d:%02d:%02d(%d)",
                this.instant.getYear() + 1900,
                this.instant.getMonth() + 1,
                this.instant.getDate(),
                this.instant.getHours(),
                this.instant.getMinutes(),
                this.instant.getSeconds(),
                this.sampleNumber
        );
    }

}
