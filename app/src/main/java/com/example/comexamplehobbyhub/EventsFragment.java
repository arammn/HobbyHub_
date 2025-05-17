package com.example.comexamplehobbyhub;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class EventsFragment extends Fragment {

    private RecyclerView recyclerView;
    private EventAdapter eventAdapter;
    private List<Event> eventList;
    private FirebaseFirestore db;
    private ProgressBar progressBar;
    private TextView tvNoEvents;
    private ImageButton btnCreateEvent;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_events, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewEvents);
        progressBar = view.findViewById(R.id.progressBar);
        tvNoEvents = view.findViewById(R.id.tvNoEvents);
        btnCreateEvent = view.findViewById(R.id.btnCreateEvent);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        eventList = new ArrayList<>();
        eventAdapter = new EventAdapter(getContext(), eventList);
        recyclerView.setAdapter(eventAdapter);

        db = FirebaseFirestore.getInstance();
        loadEvents();

        btnCreateEvent.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CreateEventFragment.class);
            startActivity(intent);
        });

        return view;
    }

    private void loadEvents() {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("events")
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        eventList.clear();
                        for (DocumentSnapshot doc : task.getResult()) {
                            try {
                                Event event = doc.toObject(Event.class);
                                if (event != null) {
                                    event.setId(doc.getId());
                                    if (event.getParticipants() == null) {
                                        event.setParticipants(new ArrayList<>());
                                    }
                                    eventList.add(event);
                                }
                            } catch (Exception e) {
                                Log.e("EventsFragment", "Error parsing event", e);
                            }
                        }
                        updateUI();
                    } else {
                        String error = task.getException() != null
                                ? task.getException().getMessage()
                                : "Unknown error";
                        showError("Failed to load events: " + error);
                    }
                });
    }

    private void showError(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void updateUI() {
        if (eventList.isEmpty()) {
            tvNoEvents.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvNoEvents.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
        eventAdapter.notifyDataSetChanged();
    }
}
