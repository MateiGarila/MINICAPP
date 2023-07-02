package com.example.mini_cap.model;

public class User {
    
    private int UserID;
    private String surname;
    private String name;
    private int age;
    private String skinTone;

    public User(int userID, String surname, String name, int age, String skinTone) {
        UserID = userID;
        this.surname = surname;
        this.name = name;
        this.age = age;
        this.skinTone = skinTone;
    }

    public int getUserID() {
        return UserID;
    }

    public void setUserID(int userID) {
        UserID = userID;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getSkinTone() {
        return skinTone;
    }

    public void setSkinTone(String skinTone) {
        this.skinTone = skinTone;
    }

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
