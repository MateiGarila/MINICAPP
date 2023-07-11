package com.example.mini_cap.model;

public class User {
    
    private int UserID;
    private String surname;
    private String name;
    private int age;
    private String skinTone;

    /**
     * Public constructor for User objects
     * @param userID
     * @param surname
     * @param name
     * @param age
     * @param skinTone
     */
    public User(int userID, String surname, String name, int age, String skinTone) {
        UserID = userID;
        this.surname = surname;
        this.name = name;
        this.age = age;
        this.skinTone = skinTone;
    }

    /**
     * Standard getter
     * @return userID as an int
     */
    public int getUserID() {
        return UserID;
    }

    /**
     * Standard setter
     * @param userID new userID value
     */
    public void setUserID(int userID) {
        UserID = userID;
    }

    /**
     * Standard getter
     * @return surname as a String
     */
    public String getSurname() {
        return surname;
    }

    /**
     * Standard setter
     * @param surname new surname value
     */
    public void setSurname(String surname) {
        this.surname = surname;
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
                "UserID=" + UserID +
                ", surname='" + surname + '\'' +
                ", name='" + name + '\'' +
                ", age=" + age +
                ", skinTone='" + skinTone + '\'' +
                '}';
    }
}
