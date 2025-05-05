package com.example.comexamplehobbyhub;

import com.google.firebase.firestore.Exclude;

import java.util.Date;

public class Event {
    @Exclude
    private String id;
    private String name;
    private int participants;
    private double latitude;
    private double longitude;

    private Date eventDate;
    private Date autoDeleteAt;



    public Event() {
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

    public Date getEventDate() {
        return eventDate;
    }

    public Date getAutoDeleteAt() {
        return autoDeleteAt;
    }

}
