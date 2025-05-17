package com.example.comexamplehobbyhub;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class EventSettingsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ParticipantsAdapter adapter;
    private List<User> participants = new ArrayList<>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_settings);

        String eventId = getIntent().getStringExtra("eventId");

        recyclerView = findViewById(R.id.recycler_participants);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ParticipantsAdapter(participants);
        recyclerView.setAdapter(adapter);

        loadParticipants(eventId);
    }

    private void loadParticipants(String eventId) {
        FirebaseFirestore.getInstance().collection("events")
                .document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Event event = documentSnapshot.toObject(Event.class);
                    if (event != null && event.getParticipants() != null) {
                        fetchUserDetails(event.getParticipants());
                    } else {
                        showEmptyState();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showEmptyState() {
        findViewById(R.id.empty_view).setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }

    private void fetchUserDetails(List<String> userIds) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        for(String userId : userIds) {
            db.collection("users").document(userId)
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        User user = snapshot.toObject(User.class);
                        participants.add(user);
                        adapter.notifyDataSetChanged();
                    });
        }
    }
}