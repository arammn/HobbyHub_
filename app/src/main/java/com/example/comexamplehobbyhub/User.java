package com.example.comexamplehobbyhub;

public class User {
    private String uid, email, username, hobby;

    public User() {} // Empty constructor for Firebase

    public User(String uid, String email, String username, String hobby) {
        this.uid = uid;
        this.email = email;
        this.username = username;
        this.hobby = hobby;
    }

    public String getUid() { return uid; }
    public String getEmail() { return email; }
    public String getUsername() { return username; }
    public String getHobby() { return hobby; }
}