package com.example.comexamplehobbyhub;

public class User {
    private String uid, email, username, hobby;
    private String profileImage;

    public User() {} // Empty constructor for Firebase

    public User(String uid, String email, String username, String hobby, String profileImage) {
        this.uid = uid;
        this.email = email;
        this.username = username;
        this.hobby = hobby;
        this.profileImage = profileImage;
    }

    public String getUid() { return uid; }
    public String getEmail() { return email; }
    public String getUsername() { return username; }
    public String getHobby() { return hobby; }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }
}