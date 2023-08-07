package app.uvtracker.data.optical;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

public class OpticalRecord {

    private static final String TAG = OpticalRecord.class.getSimpleName();

    @Nullable
    public static OpticalRecord unflatten(@NonNull String input) {
        try {
            JSONObject obj = new JSONObject(input);
            return new OpticalRecord(
                    (float)obj.getDouble("uv"),
                    (float)obj.getDouble("vis")
            );
        } catch (JSONException e) {
            Log.w(TAG, "Could not parse record " + input);
            e.printStackTrace();
            return null;
        }
    }

    public final boolean valid;
    public final float uvIndex;
    public final float illuminance;

    @Nullable
    private String flattenedString;

    public OpticalRecord() {
        this.valid = false;
        this.uvIndex = 0f;
        this.illuminance = 0f;
    }

    public OpticalRecord(float uvIndex, float illuminance) {
        this.valid = true;
        this.uvIndex = round2(uvIndex);
        this.illuminance = round2(illuminance);
    }

    @NonNull
    public String flatten() {
        if(this.flattenedString == null)
            this.flattenedString = this.flattenCore();
        return this.flattenedString;
    }

    @NonNull
    private String flattenCore() {
        try {
            JSONObject obj = new JSONObject();
            obj.put("uv", this.uvIndex);
            obj.put("vis", this.illuminance);
            return obj.toString();
        } catch (JSONException e) {
            // As per documented suggestion
            throw new RuntimeException(e);
        }
    }

    @NonNull
    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "{%1$.1flux,%2$.1fuvi}", this.illuminance, this.uvIndex);
    }

    private static float round2(float input) {
        if(Float.isNaN(input)) return 0.0f;
        return Math.round(input * 100.0f) / 100.0f;
    }

}
