package com.example.comexamplehobbyhub;

import com.google.firebase.firestore.Exclude;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Event {
    @Exclude
    private String id;
    private String name;
    private double latitude;
    private double longitude;

    private Date eventDate;
    private Date autoDeleteAt;

    private String creatorId;
    private List<String> participants = new ArrayList<>();
    public void setId(String id) { this.id = id; }
    public void setCreatorId(String creatorId) { this.creatorId = creatorId; }
    public void setParticipants(List<String> participants) { this.participants = participants; }
    public void setEventDate(Date eventDate) { this.eventDate = eventDate; }




    public Event() {
    }

    public Event(String id, String name, List<String> participants, double latitude, double longitude, Date eventDate, String creatorId) {
        this.id = id;
        this.name = name;
        this.participants = participants;
        this.latitude = latitude;
        this.longitude = longitude;
        this.eventDate = eventDate;
        this.creatorId = creatorId;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }

    public String getCreatorId() { return creatorId; }
    public List<String> getParticipants() { return participants; }

    public Date getEventDate() {
        return eventDate;
    }

    public Date getAutoDeleteAt() {
        return autoDeleteAt;
    }
    @Exclude
    public boolean isUserParticipant(String userId) {
        return participants != null && participants.contains(userId);
    }

    @Exclude
    public int getParticipantsCount() {
        return participants != null ? participants.size() : 0;
    }
}
