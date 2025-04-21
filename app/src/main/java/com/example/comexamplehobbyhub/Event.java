package com.example.comexamplehobbyhub;

import com.google.firebase.firestore.Exclude;

public class Event {
    @Exclude
    private String id;
    private String name;
    private int participants;
    private double latitude;
    private double longitude;

    public Event() {
        // Firebase требует пустой конструктор
    }

    public Event(String id, String name, int participants, double latitude, double longitude) {
        this.id = id;
        this.name = name;
        this.participants = participants;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public int getParticipants() { return participants; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
}
