package com.example.comexamplehobbyhub;

public class Chat {
    private String chatId;
    private String chatPartnerId;
    private String chatPartnerName;

    public Chat(String chatId, String chatPartnerId, String chatPartnerName) {
        this.chatId = chatId;
        this.chatPartnerId = chatPartnerId;
        this.chatPartnerName = chatPartnerName;
    }

    public String getChatId() {
        return chatId;
    }

    public String getChatPartnerId() {
        return chatPartnerId;
    }

    public String getChatPartnerName() {
        return chatPartnerName;
    }
}