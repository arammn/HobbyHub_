package com.example.comexamplehobbyhub;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    private List<User> userList;
    private Context context;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    public UserAdapter(Context context, List<User> userList) {
        this.userList = userList;
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
        this.mAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = userList.get(position);
        holder.usernameTextView.setText(user.getUsername());
        holder.emailTextView.setText(user.getEmail());
        holder.hobbyTextView.setText(user.getHobby());

        holder.addChatButton.setOnClickListener(v -> createChat(user.getUid(), user.getUsername()));
    }

    private void createChat(String receiverId, String receiverUsername) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(context, "Error: User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        String senderId = currentUser.getUid();
        String chatId = senderId + "_" + receiverId;  // Unique chat ID

        CollectionReference chatsRef = db.collection("chats");

        // Check if chat already exists
        chatsRef.document(chatId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Toast.makeText(context, "Chat already exists!", Toast.LENGTH_SHORT).show();
            } else {
                // Create new chat
                Map<String, Object> chatData = new HashMap<>();
                chatData.put("chatId", chatId);
                chatData.put("participants", List.of(senderId, receiverId));
                chatData.put("lastMessage", "Chat started...");
                chatData.put("timestamp", System.currentTimeMillis());

                chatsRef.document(chatId).set(chatData)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(context, "Chat created!", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(context, "Error creating chat: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView usernameTextView, emailTextView, hobbyTextView;
        ImageButton addChatButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameTextView = itemView.findViewById(R.id.usernameTextView);
            emailTextView = itemView.findViewById(R.id.emailTextView);
            hobbyTextView = itemView.findViewById(R.id.hobbyTextView);
            addChatButton = itemView.findViewById(R.id.addChatButton);
        }
    }
}