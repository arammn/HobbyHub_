package com.example.comexamplehobbyhub;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessages;
    private EditText messageEditText;
    private ImageButton sendButton, backButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String currentUserId, chatPartnerId, chatId, chatPartnerName;
    private TextView chatPartnerNameTextView, statusTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);



        recyclerView = findViewById(R.id.recyclerView);
        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);
        backButton = findViewById(R.id.backButton);
        chatPartnerNameTextView = findViewById(R.id.chatPartnerNameTextView);
        statusTextView = findViewById(R.id.statusTextView);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        chatPartnerId = getIntent().getStringExtra("chatPartnerId");
        chatPartnerName = getIntent().getStringExtra("chatPartnerName");

        chatPartnerNameTextView.setText(chatPartnerName);
        backButton.setOnClickListener(v -> onBackPressed());

        chatId = currentUserId.compareTo(chatPartnerId) < 0 ?
                currentUserId + "_" + chatPartnerId : chatPartnerId + "_" + currentUserId;

        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessages, currentUserId, this, chatId);
        recyclerView.setAdapter(chatAdapter);

        recyclerView.setAdapter(chatAdapter);

        loadMessages();
        listenToPartnerStatus();

        sendButton.setOnClickListener(v -> sendMessage());
    }

    private void loadMessages() {
        db.collection("chats").document(chatId).collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;

                    chatMessages.clear();
                    for (DocumentSnapshot doc : value.getDocuments()) {
                        chatMessages.add(doc.toObject(ChatMessage.class));
                    }
                    chatAdapter.notifyDataSetChanged();
                    recyclerView.scrollToPosition(chatMessages.size() - 1);
                });
    }

    private void sendMessage() {
        String messageText = messageEditText.getText().toString().trim();
        if (TextUtils.isEmpty(messageText)) return;

        db.collection("users").document(currentUserId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists() && documentSnapshot.contains("username")) {
                String senderName = documentSnapshot.getString("username");

                Map<String, Object> message = new HashMap<>();
                message.put("senderId", currentUserId);
                message.put("senderName", senderName);
                message.put("message", messageText);
                message.put("timestamp", System.currentTimeMillis());

                db.collection("chats").document(chatId).collection("messages").add(message)
                        .addOnFailureListener(e -> Log.e("ChatActivity", "Error sending message", e));

                messageEditText.setText("");
            } else {
                Log.e("ChatActivity", "Username not found in Firestore");
            }
        }).addOnFailureListener(e -> Log.e("ChatActivity", "Error fetching user data", e));
    }

    private void listenToPartnerStatus() {
        db.collection("users").document(chatPartnerId)
                .addSnapshotListener((snapshot, error) -> {
                    if (snapshot != null && snapshot.exists()) {
                        Boolean isOnline = snapshot.getBoolean("isOnline");
                        if (isOnline != null && isOnline) {
                            statusTextView.setText("Online");
                            statusTextView.setTextColor(Color.parseColor("#4CAF50"));
                        } else {
                            statusTextView.setText("Offline");
                            statusTextView.setTextColor(Color.parseColor("#888888"));
                        }
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateOnlineStatus(true);
    }

    @Override
    protected void onStop() {
        super.onStop();
        updateOnlineStatus(false);
    }

    private void updateOnlineStatus(boolean isOnline) {
        db.collection("users").document(currentUserId).update("isOnline", isOnline);
    }
}