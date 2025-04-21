package com.example.comexamplehobbyhub;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CreatePostActivity extends AppCompatActivity {
    private EditText editPostContent;
    private ImageButton btnPost;
    private ImageView imgPost;
    private Uri imageUri;
    private FirebaseFirestore db;
    private StorageReference storageReference;
    private ImageButton btnComeBackPost;
    private ProgressDialog progressDialog;

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final long POST_COOLDOWN = 60 * 1000; // 1 минута в миллисекундах

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        editPostContent = findViewById(R.id.editPostContent);
        btnComeBackPost = findViewById(R.id.btnComeBackPost);
        btnPost = findViewById(R.id.btnPost);
        imgPost = findViewById(R.id.imgPost);

        db = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference("post_images");
        btnComeBackPost.setOnClickListener(v -> finish());

        imgPost.setOnClickListener(v -> openFileChooser());

        btnPost.setOnClickListener(v -> {
            if (canPost()) {
                uploadPost();
            } else {
                Toast.makeText(this, "Wait 1 minute before creating a new post.!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            Glide.with(this).load(imageUri).into(imgPost);
        }
    }

    private boolean canPost() {
        SharedPreferences prefs = getSharedPreferences("PostPrefs", MODE_PRIVATE);
        long lastPostTime = prefs.getLong("lastPostTime", 0);
        long currentTime = System.currentTimeMillis();

        return (currentTime - lastPostTime) >= POST_COOLDOWN;
    }

    private void savePostTime() {
        SharedPreferences prefs = getSharedPreferences("PostPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong("lastPostTime", System.currentTimeMillis());
        editor.apply();
    }

    private void uploadPost() {
        String postContent = editPostContent.getText().toString().trim();
        if (TextUtils.isEmpty(postContent)) {
            Toast.makeText(this, "Enter the text of the post", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog = ProgressDialog.show(this, "Creating a post", "Please wait...", true, false);

        String postTime = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String nickname = documentSnapshot.getString("nickname");
                        String profileImage = documentSnapshot.getString("profileImage");

                        if (imageUri != null) {
                            StorageReference fileReference = storageReference.child(System.currentTimeMillis() + ".jpg");
                            fileReference.putFile(imageUri)
                                    .continueWithTask(task -> fileReference.getDownloadUrl())
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            savePostToFirestore(nickname, profileImage, postContent, postTime, task.getResult().toString());
                                        } else {
                                            progressDialog.dismiss();
                                            Toast.makeText(this, "Image upload error", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            savePostToFirestore(nickname, profileImage, postContent, postTime, null);
                        }
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(this, "Error receiving profile data", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Profile upload error!", Toast.LENGTH_SHORT).show();
                });
    }

    private void savePostToFirestore(String nickname, String profileImage, String postContent, String postTime, String imageUrl) {
        String postId = db.collection("posts").document().getId(); // Generate postId
        Map<String, Object> post = new HashMap<>();
        post.put("userId", FirebaseAuth.getInstance().getCurrentUser().getUid());
        post.put("userName", nickname != null ? nickname : "Аноним");
        post.put("userAvatar", profileImage != null && !profileImage.isEmpty() ? profileImage : "");
        post.put("postTime", postTime);
        post.put("postContent", postContent);
        post.put("postImage", imageUrl);
        post.put("postId", postId);

        db.collection("posts").document(postId).set(post) // Set postId in Firestore
                .addOnSuccessListener(documentReference -> {
                    savePostTime();
                    progressDialog.dismiss();
                    Toast.makeText(CreatePostActivity.this, "The post has been created!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(CreatePostActivity.this, "Error!", Toast.LENGTH_SHORT).show();
                });
    }

}
