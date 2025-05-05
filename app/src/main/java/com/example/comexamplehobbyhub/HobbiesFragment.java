package com.example.comexamplehobbyhub;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class HobbiesFragment extends Fragment {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private TextView noUsersTextView;
    private List<User> userList;

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_hobbies, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        noUsersTextView = view.findViewById(R.id.noUsersTextView);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        userList = new ArrayList<>();
        userAdapter = new UserAdapter(getContext(), userList);
        recyclerView.setAdapter(userAdapter);

        fetchCurrentUserHobby();

        updateCurrencyCounter(view);

        return view;
    }

    private void fetchCurrentUserHobby() {
        String userId = mAuth.getCurrentUser().getUid();
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String userHobby = documentSnapshot.getString("hobby");
                        if (userHobby != null) {
                            fetchUsersWithSameHobby(userHobby);
                        } else {
                            Toast.makeText(getContext(), "No hobby found!", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error fetching hobby", Toast.LENGTH_SHORT).show());
    }

    private void fetchUsersWithSameHobby(String hobby) {
        CollectionReference usersRef = db.collection("users");
        usersRef.whereEqualTo("hobby", hobby).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    userList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String uid = document.getString("uid");
                        String email = document.getString("email");
                        String username = document.getString("username");
                        String profileImage = document.getString("profileImage");

                        if (!uid.equals(mAuth.getCurrentUser().getUid())) {
                            userList.add(new User(uid, email, username, hobby, profileImage));
                        }
                    }
                    userAdapter.notifyDataSetChanged();

                    if (userList.isEmpty()) {
                        recyclerView.setVisibility(View.GONE);
                        noUsersTextView.setVisibility(View.VISIBLE);
                    } else {
                        recyclerView.setVisibility(View.VISIBLE);
                        noUsersTextView.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error fetching users", Toast.LENGTH_SHORT).show());
    }

    private void updateCurrencyCounter(View view) {
        TextView currencyCounter = view.findViewById(R.id.currencyCounter);
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance().collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Long coins = documentSnapshot.getLong("sparkCoins");
                    if (coins != null) {
                        currencyCounter.setText(String.valueOf(coins));
                    }
                });
    }

}
