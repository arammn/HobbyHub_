package com.example.comexamplehobbyhub;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class CreateEventFragment extends AppCompatActivity {

    private static final int LOCATION_PICKER_REQUEST = 100;
    private EditText eventNameInput, eventParticipantsInput, eventLatitudeInput, eventLongitudeInput;
    private ImageButton createEventButton, pickLocationButton;
    private FirebaseFirestore db;

    private static final long EVENT_COOLDOWN = 60 * 1000; // 1 минута

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_create_event);

        eventNameInput = findViewById(R.id.eventNameInput);
        eventParticipantsInput = findViewById(R.id.eventParticipantsInput);
        eventLatitudeInput = findViewById(R.id.eventLatitudeInput);
        eventLongitudeInput = findViewById(R.id.eventLongitudeInput);
        createEventButton = findViewById(R.id.createEventButton);
        pickLocationButton = findViewById(R.id.pickLocationButton);

        ImageButton backButton = findViewById(R.id.backButtonn);
        backButton.setOnClickListener(v -> onBackPressed());


        db = FirebaseFirestore.getInstance();

        pickLocationButton.setOnClickListener(v -> {
            Intent intent = new Intent(CreateEventFragment.this, MapPickerActivity.class);
            startActivityForResult(intent, LOCATION_PICKER_REQUEST);
        });

        createEventButton.setOnClickListener(v -> {
            if (canCreateEvent()) {
                createEvent();
            } else {
                Toast.makeText(this, "Wait 1 minute before creating a new event!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOCATION_PICKER_REQUEST && resultCode == RESULT_OK && data != null) {
            double latitude = data.getDoubleExtra("latitude", 0);
            double longitude = data.getDoubleExtra("longitude", 0);
            eventLatitudeInput.setText(String.valueOf(latitude));
            eventLongitudeInput.setText(String.valueOf(longitude));
        }
    }

    private boolean canCreateEvent() {
        SharedPreferences prefs = getSharedPreferences("EventPrefs", MODE_PRIVATE);
        long lastEventTime = prefs.getLong("lastEventTime", 0);
        return (System.currentTimeMillis() - lastEventTime) >= EVENT_COOLDOWN;
    }

    private void saveEventTime() {
        SharedPreferences prefs = getSharedPreferences("EventPrefs", MODE_PRIVATE);
        prefs.edit().putLong("lastEventTime", System.currentTimeMillis()).apply();
    }

    private void createEvent() {
        String name = eventNameInput.getText().toString().trim();
        String participantsStr = eventParticipantsInput.getText().toString().trim();
        String latitudeStr = eventLatitudeInput.getText().toString().trim();
        String longitudeStr = eventLongitudeInput.getText().toString().trim();

        if (name.isEmpty() || participantsStr.isEmpty() || latitudeStr.isEmpty() || longitudeStr.isEmpty()) {
            Toast.makeText(this, "Fill in all the fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int participants = Integer.parseInt(participantsStr);
        double latitude = Double.parseDouble(latitudeStr);
        double longitude = Double.parseDouble(longitudeStr);

        Map<String, Object> event = new HashMap<>();
        event.put("name", name);
        event.put("participants", participants);
        event.put("latitude", latitude);
        event.put("longitude", longitude);

        db.collection("events").add(event)
                .addOnSuccessListener(documentReference -> {
                    saveEventTime();
                    Toast.makeText(this, "Event created!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
