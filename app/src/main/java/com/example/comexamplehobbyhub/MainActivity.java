package com.example.comexamplehobbyhub;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ImageButton btnHome, btnEvents, btnChats, btnProfile, btnHobbies;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        btnHome = findViewById(R.id.btnHome);
        btnEvents = findViewById(R.id.btnEvents);
        btnChats = findViewById(R.id.btnChats);
        btnProfile = findViewById(R.id.btnProfile);
        btnHobbies = findViewById(R.id.btnHobbies);

        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return;
        }

        setUserOnlineStatus(true); // Set online when app opens

        loadFragment(new HomeFragment());

        btnHome.setOnClickListener(v -> loadFragment(new HomeFragment()));
        btnHobbies.setOnClickListener(v -> loadFragment(new HobbiesFragment()));
        btnEvents.setOnClickListener(v -> loadFragment(new EventsFragment()));
        btnChats.setOnClickListener(v -> loadFragment(new ChatsFragment()));
        btnProfile.setOnClickListener(v -> loadFragment(new ProfileFragment()));
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUserOnlineStatus(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        setUserOnlineStatus(false);
    }

    private void setUserOnlineStatus(boolean isOnline) {
        if (mAuth.getCurrentUser() != null) {
            db.collection("users").document(mAuth.getCurrentUser().getUid())
                    .update("isOnline", isOnline);
        }
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment);
        transaction.commit();
    }
}
