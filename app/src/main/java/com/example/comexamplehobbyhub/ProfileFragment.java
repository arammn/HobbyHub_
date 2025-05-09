package com.example.comexamplehobbyhub;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ProfileFragment extends Fragment {
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final String IMAGEBB_API_KEY = "a4be3edcc0b56bd91e11052f8edf2557";

    private ImageView imgProfile;
    private EditText editNickname;
    private ImageButton btnSave;
    private Uri imageUri;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private ProgressDialog progressDialog;
    private TextView txtEmail;
    private String currentImageUrl;

    private ImageButton btnLogout;
    private EditText editUsername;
    private Spinner spinnerHobby;

    private String previousUsername = null;
    private String[] hobbies = {"Gaming", "Music", "Sports", "Reading", "Painting", "Cooking", "Dancing"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        imgProfile = view.findViewById(R.id.imgProfile);
        editNickname = view.findViewById(R.id.editNickname);
        btnSave = view.findViewById(R.id.btnSave);
        txtEmail = view.findViewById(R.id.txtEmail);
        btnLogout = view.findViewById(R.id.btnLogout);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            txtEmail.setText(user.getEmail());
            loadUserProfile(user.getUid());
        }

        editUsername = view.findViewById(R.id.editUsername);
        spinnerHobby = view.findViewById(R.id.spinnerHobby);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, hobbies) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                ((TextView) view).setTextColor(Color.WHITE);
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView textView = (TextView) view;
                textView.setTextColor(Color.WHITE);
                view.setBackgroundResource(R.drawable.design8);
                return view;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerHobby.setAdapter(adapter);

        imgProfile.setOnClickListener(v -> openFileChooser());
        btnSave.setOnClickListener(v -> saveUserProfile());

        btnLogout.setOnClickListener(v -> {
            auth.signOut();
            startActivity(new Intent(getActivity(), LoginActivity.class));
            getActivity().finish();
        });

        return view;
    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            imgProfile.setImageURI(imageUri);
        }
    }

    private void saveUserProfile() {
        String nickname = editNickname.getText().toString().trim();
        String username = editUsername.getText().toString().trim();
        String hobby = spinnerHobby.getSelectedItem().toString();

        if (TextUtils.isEmpty(nickname) || TextUtils.isEmpty(username)) {
            Toast.makeText(getContext(), "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog = ProgressDialog.show(getContext(), "Сохранение", "Пожалуйста, подождите...", true, false);

        String userId = auth.getCurrentUser().getUid();
        DocumentReference userRef = db.collection("users").document(userId);

        db.collection("users")
                .whereEqualTo("username", username)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    boolean usernameTaken = false;
                    for (var doc : queryDocumentSnapshots.getDocuments()) {
                        if (!doc.getId().equals(userId)) {
                            usernameTaken = true;
                            break;
                        }
                    }

                    if (usernameTaken) {
                        progressDialog.dismiss();
                        Toast.makeText(getContext(), "Имя пользователя занято", Toast.LENGTH_SHORT).show();
                    } else {
                        if (imageUri != null) {
                            uploadToImageBB(imageUri, url -> {
                                currentImageUrl = url;
                                updateUserData(userRef, nickname, username, hobby, url);
                            });
                        } else {
                            updateUserData(userRef, nickname, username, hobby, currentImageUrl);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "Ошибка проверки имени пользователя", Toast.LENGTH_SHORT).show();
                });
    }

    private void uploadToImageBB(Uri uri, OnImageUploadComplete callback) {
        String base64Image = encodeImageToBase64(uri);
        if (base64Image == null) {
            Toast.makeText(getContext(), "Ошибка обработки изображения", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = "https://api.imgbb.com/1/upload?key=" + IMAGEBB_API_KEY;

        OkHttpClient client = new OkHttpClient();
        RequestBody formBody = new FormBody.Builder()
                .add("image", base64Image)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                requireActivity().runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "Ошибка загрузки изображения", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    requireActivity().runOnUiThread(() -> {
                        progressDialog.dismiss();
                        Toast.makeText(getContext(), "Сервер вернул ошибку", Toast.LENGTH_SHORT).show();
                    });
                    return;
                }

                String res = response.body().string();
                try {
                    JSONObject json = new JSONObject(res);
                    String imageUrl = json.getJSONObject("data").getString("url");
                    requireActivity().runOnUiThread(() -> callback.onComplete(imageUrl));
                } catch (JSONException e) {
                    requireActivity().runOnUiThread(() -> {
                        progressDialog.dismiss();
                        Toast.makeText(getContext(), "Ошибка парсинга ответа", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private String encodeImageToBase64(Uri imageUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos);
            byte[] imageBytes = baos.toByteArray();
            return Base64.encodeToString(imageBytes, Base64.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private interface OnImageUploadComplete {
        void onComplete(String imageUrl);
    }

    private void updateUserData(DocumentReference userRef, String nickname, String username, String hobby, String imageUrl) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("nickname", nickname);
        userData.put("username", username);
        userData.put("hobby", hobby);
        userData.put("profileImage", imageUrl != null ? imageUrl : "");

        userRef.set(userData, com.google.firebase.firestore.SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "Профиль обновлен!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "Ошибка обновления!", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadUserProfile(String userId) {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String nickname = documentSnapshot.getString("nickname");
                        String username = documentSnapshot.getString("username");
                        String hobby = documentSnapshot.getString("hobby");
                        currentImageUrl = documentSnapshot.getString("profileImage");

                        editNickname.setText(nickname);
                        editUsername.setText(username);
                        previousUsername = username;

                        if (hobby != null) {
                            for (int i = 0; i < hobbies.length; i++) {
                                if (hobbies[i].equals(hobby)) {
                                    spinnerHobby.setSelection(i);
                                    break;
                                }
                            }
                        }

                        if (currentImageUrl != null && !currentImageUrl.isEmpty()) {
                            Glide.with(this).load(currentImageUrl).into(imgProfile);
                        } else {
                            imgProfile.setImageResource(R.drawable.ic_profile_placeholder);
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Ошибка загрузки профиля", Toast.LENGTH_SHORT).show());
    }
}
