package com.example.comexamplehobbyhub;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.airbnb.lottie.LottieAnimationView;
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

    private LinearLayout progressBarContainer;
    private boolean isProgressBarVisible = false;

    private LottieAnimationView fireworkAnimation;
    private String previousRank = "";
    private String currentRank = "";

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
        progressBarContainer = findViewById(R.id.progressBarContainer);

        fireworkAnimation = findViewById(R.id.fireworkAnimation);
        fireworkAnimation.setAnimation(R.raw.firework_animation);
        fireworkAnimation.setSpeed(1.5f);

        rankIcon.setOnClickListener(v -> toggleProgressBarVisibility());

        startXpRankUpdater();

        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return;
        }

        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );

        setUserOnlineStatus(true);

        loadFragment(new HomeFragment());

        btnHome.setOnClickListener(v -> loadFragment(new HomeFragment()));
        btnHobbies.setOnClickListener(v -> loadFragment(new HobbiesFragment()));
        btnEvents.setOnClickListener(v -> loadFragment(new EventsFragment()));
        btnChats.setOnClickListener(v -> loadFragment(new ChatsFragment()));
        btnProfile.setOnClickListener(v -> loadFragment(new ProfileFragment()));
    }

    private void toggleProgressBarVisibility() {
        if (isProgressBarVisible) {
            Animation slideOut = AnimationUtils.loadAnimation(this, R.anim.slide_out_right);
            slideOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}

                @Override
                public void onAnimationEnd(Animation animation) {
                    progressBarContainer.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {}
            });
            progressBarContainer.startAnimation(slideOut);
        } else {
            progressBarContainer.setVisibility(View.VISIBLE);
            Animation slideIn = AnimationUtils.loadAnimation(this, R.anim.slide_in_right);
            progressBarContainer.startAnimation(slideIn);
        }
        isProgressBarVisible = !isProgressBarVisible;
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
                handler.postDelayed(this, 1000);
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
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(xpUpdater);
    }

    private void updateRankUI(int xp) {
        String newRank;
        int maxXp;
        int progress;

        if (xp < 100) {
            newRank = "Bronze";
            maxXp = 100;
            rankIcon.setImageResource(R.drawable.bronze_medal);
        } else if (xp < 300) {
            newRank = "Silver";
            maxXp = 200;
            rankIcon.setImageResource(R.drawable.silver_medal);
            xp -= 100;
        } else {
            newRank = "Gold";
            maxXp = 200;
            rankIcon.setImageResource(R.drawable.gold_medal);
            xp -= 300;
        }

        if (!newRank.equals(currentRank)) {
            previousRank = currentRank;
            currentRank = newRank;

            if (!previousRank.isEmpty()) {
                showRankUpAnimation();
            }
        }

        progress = (int) ((xp / (float) maxXp) * 100);
        xpProgressBar.setProgress(progress);
    }

    private void showRankUpAnimation() {
        fireworkAnimation.setVisibility(View.VISIBLE);
        fireworkAnimation.playAnimation();

        fireworkAnimation.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {}

            @Override
            public void onAnimationEnd(Animator animation) {
                fireworkAnimation.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {}

            @Override
            public void onAnimationRepeat(Animator animation) {}
        });


        String message = "Congratulations! New rank: " + currentRank + "!";
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
