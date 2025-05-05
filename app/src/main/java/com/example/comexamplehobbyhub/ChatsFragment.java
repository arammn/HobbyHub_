package com.example.comexamplehobbyhub;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class ChatsFragment extends Fragment {

    private RecyclerView recyclerView;
    private ChatListAdapter chatListAdapter;
    private List<Chat> chatList;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chats, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        chatList = new ArrayList<>();
        chatListAdapter = new ChatListAdapter(chatList, chat -> {
            Intent intent = new Intent(getContext(), ChatActivity.class);
            intent.putExtra("chatId", chat.getChatId());
            intent.putExtra("chatPartnerId", chat.getChatPartnerId());
            intent.putExtra("chatPartnerName", chat.getChatPartnerName());
            startActivity(intent);
        });

        recyclerView.setAdapter(chatListAdapter);
        loadChats();

        return view;
    }

    private void loadChats() {
        db.collection("chats").whereArrayContains("participants", currentUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    chatList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String chatId = document.getId();
                        List<String> participants = (List<String>) document.get("participants");
                        if (participants != null && participants.size() == 2) {
                            String chatPartnerId = participants.get(0).equals(currentUserId) ? participants.get(1) : participants.get(0);

                            DocumentReference userRef = db.collection("users").document(chatPartnerId);
                            userRef.get().addOnSuccessListener(userDoc -> {
                                if (userDoc.exists()) {
                                    String chatPartnerName = userDoc.getString("username");
                                    chatList.add(new Chat(chatId, chatPartnerId, chatPartnerName));
                                    chatListAdapter.notifyDataSetChanged();
                                }
                            });
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error loading chats", Toast.LENGTH_SHORT).show());
    }
}
