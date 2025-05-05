package com.example.comexamplehobbyhub;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class QuestsActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String userId;
    private TextView loginQuestStatus, eventQuestStatus;
    private ImageButton claimLoginBtn, claimEventBtn;
    private TextView eventCooldownTimer;
    private ImageView loginCheckmark, eventCheckmark;

    private boolean loginClaiming = false;
    private boolean eventClaiming = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quests);

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        loginQuestStatus = findViewById(R.id.loginQuestStatus);
        eventQuestStatus = findViewById(R.id.eventQuestStatus);
        claimLoginBtn = findViewById(R.id.claimLoginButton);
        claimEventBtn = findViewById(R.id.claimEventButton);
        eventCooldownTimer = findViewById(R.id.eventCooldownTimer);
        loginCheckmark = findViewById(R.id.loginCheckmark);
        eventCheckmark = findViewById(R.id.eventCheckmark);

        claimLoginBtn.setOnClickListener(v -> {
            if (!loginClaiming) {
                loginClaiming = true;
                claimLoginBtn.setEnabled(false);
                handleDailyLoginReward();
            }
        });

        claimEventBtn.setOnClickListener(v -> {
            if (!eventClaiming) {
                eventClaiming = true;
                claimEventBtn.setEnabled(false);
                handleEventCreationReward();
            }
        });

        fetchServerTimeAndCheckQuests();
    }

    private void fetchServerTimeAndCheckQuests() {
        DocumentReference tempRef = db.collection("serverTime").document("now");
        tempRef.set(Collections.singletonMap("timestamp", FieldValue.serverTimestamp()))
                .addOnSuccessListener(unused ->
                        tempRef.get().addOnSuccessListener(snapshot -> {
                            Timestamp serverTimestamp = snapshot.getTimestamp("timestamp");
                            if (serverTimestamp != null) {
                                checkQuestStatus(serverTimestamp.toDate());
                            }
                        }))
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to fetch server time.", Toast.LENGTH_SHORT).show());
    }

    private void checkQuestStatus(Date serverDate) {
        DocumentReference userRef = db.collection("users").document(userId);
        userRef.get().addOnSuccessListener(doc -> {
            Timestamp lastLogin = doc.getTimestamp("lastLoginDate");
            Timestamp nextEventRewardDate = doc.getTimestamp("nextEventRewardDate");

            boolean canLogin = lastLogin == null || !sameDay(lastLogin.toDate(), serverDate);
            loginQuestStatus.setText(canLogin ? "Available to claim!" : "Already claimed today.");
            claimLoginBtn.setEnabled(canLogin);
            claimLoginBtn.setVisibility(canLogin ? View.VISIBLE : View.GONE);
            loginCheckmark.setVisibility(canLogin ? View.GONE : View.VISIBLE);
            loginClaiming = false;

            boolean canClaimEvent = nextEventRewardDate == null || serverDate.after(nextEventRewardDate.toDate());

            if (canClaimEvent) {
                eventQuestStatus.setText("Available to claim!");
                eventCooldownTimer.setText("");
                eventCheckmark.setVisibility(View.GONE);
            } else {
                long millisLeft = nextEventRewardDate.toDate().getTime() - serverDate.getTime();
                long daysLeft = TimeUnit.MILLISECONDS.toDays(millisLeft) + 1;
                eventQuestStatus.setText("Claimable every 3 days.");
                eventCooldownTimer.setText("⏳ Days left: " + daysLeft);
                eventCheckmark.setVisibility(View.VISIBLE);
            }


            claimEventBtn.setEnabled(canClaimEvent);
            claimEventBtn.setVisibility(canClaimEvent ? View.VISIBLE : View.GONE);
            eventClaiming = false;

        }).addOnFailureListener(e ->
                Toast.makeText(this, "Failed to fetch user data.", Toast.LENGTH_SHORT).show());


    }

    private boolean sameDay(Date d1, Date d2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(d1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(d2);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    private void handleDailyLoginReward() {
        DocumentReference tempRef = db.collection("serverTime").document("now");
        tempRef.set(Collections.singletonMap("timestamp", FieldValue.serverTimestamp()))
                .addOnSuccessListener(unused ->
                        tempRef.get().addOnSuccessListener(snapshot -> {
                            Timestamp serverTimestamp = snapshot.getTimestamp("timestamp");
                            if (serverTimestamp == null) return;
                            Date serverDate = serverTimestamp.toDate();

                            db.collection("users").document(userId)
                                    .get()
                                    .addOnSuccessListener(doc -> {
                                        Timestamp lastLogin = doc.getTimestamp("lastLoginDate");
                                        if (lastLogin != null && sameDay(lastLogin.toDate(), serverDate)) {
                                            Toast.makeText(this, "❌ Already claimed today.", Toast.LENGTH_SHORT).show();
                                            fetchServerTimeAndCheckQuests();
                                            return;
                                        }

                                        db.collection("users").document(userId)
                                                .update(
                                                        "sparkCoins", FieldValue.increment(10),
                                                        "lastLoginDate", serverTimestamp
                                                )
                                                .addOnSuccessListener(unused2 -> {
                                                    Toast.makeText(this, "✅ Daily login reward claimed!", Toast.LENGTH_SHORT).show();
                                                    fetchServerTimeAndCheckQuests();
                                                })
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(this, "❌ Failed to update reward.", Toast.LENGTH_SHORT).show();
                                                    loginClaiming = false;
                                                    claimLoginBtn.setEnabled(true);
                                                });
                                    });
                        }));
    }

    private void handleEventCreationReward() {
        db.collection("events")
                .whereEqualTo("creatorId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    DocumentReference tempRef = db.collection("serverTime").document("now");
                    tempRef.set(Collections.singletonMap("timestamp", FieldValue.serverTimestamp()))
                            .addOnSuccessListener(unused ->
                                    tempRef.get().addOnSuccessListener(snapshot -> {
                                        Timestamp serverTimestamp = snapshot.getTimestamp("timestamp");
                                        if (serverTimestamp == null) return;
                                        Date serverDate = serverTimestamp.toDate();

                                        db.collection("users").document(userId).get()
                                                .addOnSuccessListener(userDoc -> {
                                                    Timestamp nextAllowed = userDoc.getTimestamp("nextEventRewardDate");
                                                    if (nextAllowed != null && !serverDate.after(nextAllowed.toDate())) {
                                                        Toast.makeText(this, "⏳ Wait before claiming again.", Toast.LENGTH_SHORT).show();
                                                        fetchServerTimeAndCheckQuests();
                                                        return;
                                                    }

                                                    boolean hasRecentEvent = false;
                                                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                                                        Timestamp createdAt = doc.getTimestamp("createdAt");
                                                        if (createdAt != null) {
                                                            long daysAgo = TimeUnit.MILLISECONDS.toDays(serverDate.getTime() - createdAt.toDate().getTime());
                                                            if (daysAgo <= 3) {
                                                                hasRecentEvent = true;
                                                                break;
                                                            }
                                                        }
                                                    }

                                                    if (!hasRecentEvent) {
                                                        Toast.makeText(this, "❌ Create an event in the last 3 days first.", Toast.LENGTH_SHORT).show();
                                                        eventClaiming = false;
                                                        claimEventBtn.setEnabled(true);
                                                        return;
                                                    }

                                                    Calendar nextDate = Calendar.getInstance();
                                                    nextDate.setTime(serverDate);
                                                    nextDate.add(Calendar.DAY_OF_YEAR, 3);

                                                    db.collection("users").document(userId)
                                                            .update(
                                                                    "sparkCoins", FieldValue.increment(20),
                                                                    "lastEventCreationDate", serverTimestamp,
                                                                    "nextEventRewardDate", nextDate.getTime()
                                                            )
                                                            .addOnSuccessListener(aVoid -> {
                                                                Toast.makeText(this, "🎉 Event reward claimed!", Toast.LENGTH_SHORT).show();
                                                                fetchServerTimeAndCheckQuests();
                                                            })
                                                            .addOnFailureListener(e -> {
                                                                Toast.makeText(this, "❌ Failed to update reward.", Toast.LENGTH_SHORT).show();
                                                                eventClaiming = false;
                                                                claimEventBtn.setEnabled(true);
                                                            });
                                                });
                                    }));
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "❌ Failed to check events.", Toast.LENGTH_SHORT).show();
                    eventClaiming = false;
                    claimEventBtn.setEnabled(true);
                });
    }
}
