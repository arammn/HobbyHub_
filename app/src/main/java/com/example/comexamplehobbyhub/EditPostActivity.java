package com.example.comexamplehobbyhub;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class EditPostActivity extends AppCompatActivity {
    private EditText editPostContent;
    private ImageButton btnUpdatePost;

    private ImageButton btnDeletePost;
    private String postId, currentUserId;

    private ImageButton btnComeBack;
    private FirebaseFirestore db;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_post);

        editPostContent = findViewById(R.id.editPostContent);
        btnUpdatePost = findViewById(R.id.btnUpdatePost);
        btnDeletePost = findViewById(R.id.btnDeletePost);
        btnComeBack = findViewById(R.id.btnComeBack);
        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        postId = getIntent().getStringExtra("postId");

        loadPostData();

        btnDeletePost.setOnClickListener(v -> deletePost());
        btnUpdatePost.setOnClickListener(v -> updatePost());
        btnComeBack.setOnClickListener(v -> finish()); // Close activity and go back
    }

    private void deletePost() {
        if (postId == null) {
            Toast.makeText(this, "Post ID is missing", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("posts").document(postId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(EditPostActivity.this, "Post deleted", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(EditPostActivity.this, MainActivity.class));
                    finish(); // Close edit activity
                })
                .addOnFailureListener(e ->
                        Toast.makeText(EditPostActivity.this, "Failed to delete", Toast.LENGTH_SHORT).show()
                );
    }

    private void loadPostData() {
        db.collection("posts").document(postId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String postContent = documentSnapshot.getString("postContent");
                        String userId = documentSnapshot.getString("userId");

                        if (userId.equals(currentUserId)) {
                            editPostContent.setText(postContent);
                        } else {
                            Toast.makeText(this, "You are not allowed to edit this post.", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error loading post data", Toast.LENGTH_SHORT).show());
    }

    private void updatePost() {
        String updatedContent = editPostContent.getText().toString().trim();
        if (TextUtils.isEmpty(updatedContent)) {
            Toast.makeText(this, "Post content cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog = ProgressDialog.show(this, "Updating Post", "Please wait...", true, false);

        db.collection("posts").document(postId)
                .update("postContent", updatedContent)
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Post updated successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Error updating post", Toast.LENGTH_SHORT).show();
                });
    }
}
