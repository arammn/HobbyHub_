package com.example.comexamplehobbyhub;

import android.content.Context;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
    private List<ChatMessage> chatMessages;
    private String currentUserId;
    private Context context;
    private String chatId;

    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    public ChatAdapter(List<ChatMessage> chatMessages, String currentUserId, Context context, String chatId) {
        this.chatMessages = chatMessages;
        this.currentUserId = currentUserId;
        this.context = context;
        this.chatId = chatId;
    }

    @Override
    public int getItemViewType(int position) {
        return chatMessages.get(position).getSenderId().equals(currentUserId) ? VIEW_TYPE_RECEIVED : VIEW_TYPE_SENT;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_SENT) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_sent, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_received, parent, false);
        }
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMessage message = chatMessages.get(position);
        holder.messageText.setText(message.getMessage());
        holder.senderNameText.setText(message.getSenderName());

        // Load profile image for the sender
        loadProfileImage(holder.profileImage, message.getSenderId());

        // Long press for own messages
        if (message.getSenderId().equals(currentUserId)) {
            holder.itemView.setOnLongClickListener(v -> {
                showPopup(v, message);
                return true;
            });
        } else {
            holder.itemView.setOnLongClickListener(null);
        }
    }

    private void loadProfileImage(ImageView profileImageView, String senderId) {
        DocumentReference userRef = FirebaseFirestore.getInstance().collection("users").document(senderId);
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String profileImageUrl = documentSnapshot.getString("profileImage");
                if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                    Glide.with(context).load(profileImageUrl).into(profileImageView);
                } else {
                    profileImageView.setImageResource(R.drawable.ic_profile_placeholder);
                }
            } else {
                profileImageView.setImageResource(R.drawable.ic_profile_placeholder);
            }
        }).addOnFailureListener(e -> profileImageView.setImageResource(R.drawable.ic_profile_placeholder));
    }

    private void showPopup(View anchorView, ChatMessage message) {
        View popupView = LayoutInflater.from(context).inflate(R.layout.message_popup_menu, null);
        PopupWindow popupWindow = new PopupWindow(popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true);

        popupWindow.setElevation(10);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);

        ImageButton btnEdit = popupView.findViewById(R.id.btnEdit);
        ImageButton btnDelete = popupView.findViewById(R.id.btnDelete);

        btnEdit.setOnClickListener(v -> {
            popupWindow.dismiss();
            showEditDialog(message);
        });

        btnDelete.setOnClickListener(v -> {
            popupWindow.dismiss();
            deleteMessage(message);
        });

        // Get anchor (message view) position
        int[] anchorLocation = new int[2];
        anchorView.getLocationOnScreen(anchorLocation);
        int anchorX = anchorLocation[0];
        int anchorY = anchorLocation[1];

        // Measure popup
        popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int popupWidth = popupView.getMeasuredWidth();
        int popupHeight = popupView.getMeasuredHeight();
        int anchorHeight = anchorView.getHeight();

        // Vertical center of the message bubble
        int yOffset = anchorY + (anchorHeight / 2) - (popupHeight / 2);

        // Position to the left or right of the message
        boolean isMyMessage = message.getSenderId().equals(currentUserId);
        int xOffset;
        if (isMyMessage) {
            // Show to the right of sent message
            xOffset = anchorX + anchorView.getWidth();
        } else {
            // Show to the left of received message
            xOffset = anchorX - popupWidth;
        }

        popupWindow.showAtLocation(anchorView, Gravity.NO_GRAVITY, xOffset, yOffset);
    }

    private void showEditDialog(ChatMessage message) {
        EditText editText = new EditText(context);
        editText.setInputType(InputType.TYPE_CLASS_TEXT);
        editText.setText(message.getMessage());

        new android.app.AlertDialog.Builder(context)
                .setTitle("Edit Message")
                .setView(editText)
                .setPositiveButton("Save", (dialog, which) -> {
                    String updatedText = editText.getText().toString().trim();
                    if (!updatedText.isEmpty()) {
                        FirebaseFirestore.getInstance()
                                .collection("chats").document(chatId)
                                .collection("messages")
                                .whereEqualTo("timestamp", message.getTimestamp())
                                .get().addOnSuccessListener(query -> {
                                    if (!query.isEmpty()) {
                                        query.getDocuments().get(0).getReference()
                                                .update("message", updatedText);
                                        Toast.makeText(context, "Message updated", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteMessage(ChatMessage message) {
        FirebaseFirestore.getInstance()
                .collection("chats").document(chatId)
                .collection("messages")
                .whereEqualTo("timestamp", message.getTimestamp())
                .get().addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        query.getDocuments().get(0).getReference().delete();
                        Toast.makeText(context, "Message deleted", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView messageText, senderNameText;
        ImageView profileImage;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            senderNameText = itemView.findViewById(R.id.senderNameText);
            messageText = itemView.findViewById(R.id.messageText);
            profileImage = itemView.findViewById(R.id.profileImage);
        }
    }
}
