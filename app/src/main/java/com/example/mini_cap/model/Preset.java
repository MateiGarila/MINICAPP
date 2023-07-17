package com.example.mini_cap.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class Preset implements Parcelable {
    
    private int presetID;
    private String name;
    private int age;
    private String skinTone;

    /**
     * Public constructor for User objects
     * @param presetID
     * @param name
     * @param age
     * @param skinTone
     */
    public Preset(int presetID, String name, int age, String skinTone) {
        this.presetID = presetID;
        this.name = name;
        this.age = age;
        this.skinTone = skinTone;
    }

    protected Preset(Parcel in) {
        presetID = in.readInt();
        name = in.readString();
        age = in.readInt();
        skinTone = in.readString();
    }

    public static final Creator<Preset> CREATOR = new Creator<Preset>() {
        @Override
        public Preset createFromParcel(Parcel in) {
            return new Preset(in);
        }

        @Override
        public Preset[] newArray(int size) {
            return new Preset[size];
        }
    };

    /**
     * Standard getter
     * @return presetID as an int
     */
    public int getPresetID() {
        return presetID;
    }

    /**
     * Standard setter
     * @param userID new presetID value
     */
    public void setPresetID(int userID) {
        presetID = userID;
    }

    /**
     * Standard getter
     * @return name as a String
     */
    public String getName() {
        return name;
    }

    /**
     * Standard setter
     * @param name new name value
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Standard getter
     * @return age as an int
     */
    public int getAge() {
        return age;
    }

    /**
     * Standard setter
     * @param age new age value
     */
    public void setAge(int age) {
        this.age = age;
    }

    /**
     * Standard getter
     * @return skin tone as a String
     */
    public String getSkinTone() {
        return skinTone;
    }

    /**
     * Standard setter
     * @param skinTone new skin tone value
     */
    public void setSkinTone(String skinTone) {
        this.skinTone = skinTone;
    }

    /**
     * Standard toString method
     * @return User object as a String
     */
    @Override
    public String toString() {
        return "User{" +
                "UserID=" + presetID +
                ", name='" + name + '\'' +
                ", age=" + age +
                ", skinTone='" + skinTone + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(presetID);
        dest.writeString(name);
        dest.writeInt(age);
        dest.writeString(skinTone);
    }
}
