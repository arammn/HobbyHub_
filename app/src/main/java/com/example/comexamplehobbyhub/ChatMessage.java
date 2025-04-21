package com.example.comexamplehobbyhub;

public class ChatMessage {
    private String senderId;
    private String senderName;
    private String message;
    private long timestamp;
    private String senderProfileImage; // Add this field

    public ChatMessage() {}

    public ChatMessage(String senderId, String senderName, String message, long timestamp, String senderProfileImage) {
        this.senderId = senderId;
        this.senderName = senderName;
        this.message = message;
        this.timestamp = timestamp;
        this.senderProfileImage = senderProfileImage; // Initialize this field
    }

    public String getSenderId() {
        return senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getMessage() {
        return message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getSenderProfileImage() {
        return senderProfileImage; // Getter for profile image URL
    }
}
