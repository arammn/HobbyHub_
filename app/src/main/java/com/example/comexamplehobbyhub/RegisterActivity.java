package com.example.comexamplehobbyhub;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextPassword, editTextConfirmPassword, editTextUsername;
    private ImageButton btnRegister;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView textViewHobby;
    private String selectedHobby = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        editTextUsername = findViewById(R.id.editTextUsername);
        btnRegister = findViewById(R.id.btnRegister);
        textViewHobby = findViewById(R.id.textViewHobby);

        btnRegister.setOnClickListener(v -> checkUsernameAvailability());
        textViewHobby.setOnClickListener(v -> showHobbySelectionDialog());
    }

    private void checkUsernameAvailability() {
        String username = editTextUsername.getText().toString().trim();
        if (username.isEmpty()) {
            Toast.makeText(this, "Please enter a username", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!username.matches("^[a-z0-9_]+$")) {
            Toast.makeText(this, "Username must contain only lowercase letters, numbers, or underscores", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users")
                .whereEqualTo("username", username)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            Toast.makeText(this, "Username is already taken", Toast.LENGTH_SHORT).show();
                        } else {
                            registerUser(username);
                        }
                    } else {
                        Toast.makeText(this, "Error checking username: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void registerUser(String username) {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedHobby.isEmpty()) {
            Toast.makeText(RegisterActivity.this, "Please select a hobby", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            user.sendEmailVerification().addOnCompleteListener(verificationTask -> {
                                if (verificationTask.isSuccessful()) {
                                    saveUserToFirestore(user, username);
                                    Toast.makeText(this, "Verification email sent. Check spam folder!", Toast.LENGTH_LONG).show();
                                    mAuth.signOut();
                                    startActivity(new Intent(this, LoginActivity.class));
                                    finish();
                                } else {
                                    Toast.makeText(this, "Failed to send verification email: " + verificationTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    } else {
                        Toast.makeText(this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveUserToFirestore(FirebaseUser user, String username) {
        String userId = user.getUid();
        String email = user.getEmail();

        Map<String, Object> userData = new HashMap<>();
        userData.put("uid", userId);
        userData.put("email", email);
        userData.put("username", username);
        userData.put("hobby", selectedHobby);

        db.collection("users").document(userId).set(userData)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "User added to Firestore", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Error saving user: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void showHobbySelectionDialog() {
        // Organized hobby categories with more options
        String[] categories = {
                "🎮 Gaming",
                "🎵 Music",
                "⚽ Sports",
                "📚 Reading",
                "🎨 Arts & Crafts",
                "🍳 Cooking",
                "💃 Dancing",
                "🌿 Outdoor",
                "🧠 Learning",
                "📸 Photography"
        };

        String[][] hobbies = {
                {"Video Games", "Board Games", "Puzzle Games", "VR Gaming", "eSports"},
                {"Playing Instrument", "Singing", "Music Production", "DJing", "Concerts"},
                {"Football", "Basketball", "Tennis", "Swimming", "Yoga", "Running"},
                {"Fiction", "Non-fiction", "Sci-fi", "Fantasy", "Biographies"},
                {"Painting", "Drawing", "Pottery", "Knitting", "Woodworking", "3D Printing"},
                {"Baking", "Grilling", "Mixology", "Food Blogging", "International Cuisine"},
                {"Ballet", "Hip-hop", "Salsa", "Ballroom", "Contemporary"},
                {"Hiking", "Camping", "Gardening", "Fishing", "Bird Watching"},
                {"Languages", "Coding", "History", "Science", "Philosophy"},
                {"Portrait", "Landscape", "Street", "Wildlife", "Astrophotography"}
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Your Hobby Category");
        builder.setItems(categories, (dialog, categoryIndex) -> {
            // Show sub-hobbies for selected category
            AlertDialog.Builder subHobbyBuilder = new AlertDialog.Builder(this);
            subHobbyBuilder.setTitle("Select Specific Hobby");
            subHobbyBuilder.setItems(hobbies[categoryIndex], (subDialog, hobbyIndex) -> {
                selectedHobby = categories[categoryIndex].substring(2) + ": " + hobbies[categoryIndex][hobbyIndex];
                textViewHobby.setText(selectedHobby);
            });
            subHobbyBuilder.setNegativeButton("Cancel", null);
            subHobbyBuilder.show();
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
}