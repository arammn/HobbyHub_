package com.example.comexamplehobbyhub;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.net.ssl.HttpsURLConnection;
import org.json.JSONObject;
import java.io.OutputStreamWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class CreatePostActivity extends AppCompatActivity {
    private EditText editPostContent;
    private ImageButton btnPost;
    private ImageView imgPost;
    private Uri imageUri;
    private FirebaseFirestore db;
    private ImageButton btnComeBackPost;
    private ProgressDialog progressDialog;

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final long POST_COOLDOWN = 60 * 1000;
    private static final String IMGBB_API_KEY = "7f1f79ad80819b1741ba26725bf48a1e";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        editPostContent = findViewById(R.id.editPostContent);
        imgPost = findViewById(R.id.imgPost);
        btnComeBackPost = findViewById(R.id.btnComeBackPost);
        btnPost = findViewById(R.id.btnPost);
        db = FirebaseFirestore.getInstance();

        btnComeBackPost.setOnClickListener(v -> finish());

        imgPost.setOnClickListener(v -> openFileChooser());

        btnPost.setOnClickListener(v -> {
            if (canPost()) {
                uploadPost();
            } else {
                Toast.makeText(this, "Wait 1 minute before creating a new post!", Toast.LENGTH_SHORT).show();
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
                            uploadImageToImgbb(imageUri, imageUrl -> {
                                savePostToFirestore(nickname, profileImage, postContent, postTime, imageUrl);
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

    private void uploadImageToImgbb(Uri imageUri, ImageUploadCallback callback) {
        new Thread(() -> {
            try {
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int len;
                while ((len = inputStream.read(buffer)) != -1) {
                    byteArrayOutputStream.write(buffer, 0, len);
                }
                inputStream.close();
                String encodedImage = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.NO_WRAP);

                URL url = new URL("https://api.imgbb.com/1/upload?key=" + IMGBB_API_KEY);
                HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setDoOutput(true);

                String postData = "image=" + Uri.encode(encodedImage); // Important to encode
                OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
                writer.write(postData);
                writer.flush();
                writer.close();

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    JSONObject jsonObject = new JSONObject(response.toString());
                    String imageUrl = jsonObject.getJSONObject("data").getString("url");

                    runOnUiThread(() -> callback.onImageUploaded(imageUrl));
                } else {
                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        Toast.makeText(this, "Image upload failed (server error)", Toast.LENGTH_SHORT).show();
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Image upload failed (exception)", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }


    private interface ImageUploadCallback {
        void onImageUploaded(String imageUrl);
    }

    private void savePostToFirestore(String nickname, String profileImage, String postContent, String postTime, String imageUrl) {
        String postId = db.collection("posts").document().getId();
        Map<String, Object> post = new HashMap<>();
        post.put("userId", FirebaseAuth.getInstance().getCurrentUser().getUid());
        post.put("userName", nickname != null ? nickname : "Аноним");
        post.put("userAvatar", profileImage != null && !profileImage.isEmpty() ? profileImage : "");
        post.put("postTime", postTime);
        post.put("postContent", postContent);
        post.put("postImage", imageUrl != null ? imageUrl : "");
        post.put("postId", postId);
        post.put("likes", new ArrayList<>());
        post.put("likeCount", 0);

        db.collection("posts").document(postId).set(post)
                .addOnSuccessListener(documentReference -> {
                    savePostTime();
                    progressDialog.dismiss();
                    Toast.makeText(CreatePostActivity.this, "The post has been created!", Toast.LENGTH_SHORT).show();
                    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    db.collection("users").document(uid)
                            .update("xp", FieldValue.increment(5));
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(CreatePostActivity.this, "Error!", Toast.LENGTH_SHORT).show();
                });
    }
}
