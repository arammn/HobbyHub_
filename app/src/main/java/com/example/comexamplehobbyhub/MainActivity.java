package com.example.comexamplehobbyhub;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ImageButton btnHome, btnEvents, btnChats, btnProfile, btnHobbies;

    private ImageView rankIcon;
    private ProgressBar xpProgressBar;
    private Handler handler = new Handler();
    private Runnable xpUpdater;

    private TextView currencyCounter;
    private Runnable updateRunnable;

    @SuppressLint("MissingInflatedId")
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

        rankIcon = findViewById(R.id.rankIcon);
        xpProgressBar = findViewById(R.id.xpProgressBar);

        startXpRankUpdater();

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
    protected void onStart() {
        super.onStart();
        currencyCounter = findViewById(R.id.currencyCounter);

        updateRunnable = new Runnable() {
            @Override
            public void run() {
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser != null) {
                    String uid = currentUser.getUid();
                    FirebaseFirestore.getInstance().collection("users")
                            .document(uid)
                            .get()
                            .addOnSuccessListener(document -> {
                                if (document.exists()) {
                                    long coins = document.getLong("sparkCoins") != null ? document.getLong("sparkCoins") : 0;
                                    currencyCounter.setText(String.valueOf(coins));
                                }
                            });
                }
                handler.postDelayed(this, 1000); // Repeat every second
            }
        };
        handler.post(updateRunnable);
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

    @Override
    protected void onStop() {
        super.onStop();
        handler.removeCallbacks(updateRunnable);
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

    private void startXpRankUpdater() {
        xpUpdater = new Runnable() {
            @Override
            public void run() {
                if (mAuth.getCurrentUser() != null) {
                    db.collection("users").document(mAuth.getCurrentUser().getUid())
                            .get()
                            .addOnSuccessListener(documentSnapshot -> {
                                if (documentSnapshot.exists()) {
                                    Long xp = documentSnapshot.getLong("xp");
                                    if (xp != null) {
                                        updateRankUI(xp.intValue());
                                    }
                                }
                            });
                }
                handler.postDelayed(this, 1000); // Repeat every second
            }
        };
        handler.post(xpUpdater);
    }

    private void updateRankUI(int xp) {
        String rank;
        int maxXp;
        int progress;

        if (xp < 100) {
            rank = "Bronze";
            maxXp = 100;
            rankIcon.setImageResource(R.drawable.bronze_medal);
        } else if (xp < 300) {
            rank = "Silver";
            maxXp = 200;
            rankIcon.setImageResource(R.drawable.silver_medal);
            xp -= 100;
        } else {
            rank = "Gold";
            maxXp = 200;
            rankIcon.setImageResource(R.drawable.gold_medal);
            xp -= 300;
        }

        progress = (int) ((xp / (float) maxXp) * 100);
        xpProgressBar.setProgress(progress);
    }
}
