package com.example.comexamplehobbyhub;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    private RecyclerView postsRecyclerView;
    private PostAdapter postAdapter;
    private List<Post> postList;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        postsRecyclerView = view.findViewById(R.id.postsRecyclerView);
        postsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        postList = new ArrayList<>();
        postAdapter = new PostAdapter(postList);
        postsRecyclerView.setAdapter(postAdapter);




        View btnCreatePost = view.findViewById(R.id.btnCreatePost);
        btnCreatePost.setOnClickListener(v -> startActivity(new Intent(getContext(), CreatePostActivity.class)));

        View btnQuests = view.findViewById(R.id.btnQuests);
        btnQuests.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), QuestsActivity.class);
            startActivity(intent);
        });

        db = FirebaseFirestore.getInstance();
        loadPosts();

        updateCurrencyCounter(view);

        return view;

    }

    private void loadPosts() {
        db.collection("posts")
                .orderBy("postTime", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        postList.clear();
                        for (DocumentSnapshot document : task.getResult()) {
                            try {
                                Post post = new Post();

                                post.setUserId(document.getString("userId"));
                                post.setPostTime(document.getString("postTime"));
                                post.setPostContent(document.getString("postContent"));
                                post.setPostImage(document.getString("postImage"));
                                post.setPostId(document.getId());

                                // Добавьте эти строки
                                post.setLikes((List<String>) document.get("likes"));
                                post.setLikeCount(document.getLong("likeCount") != null ?
                                        document.getLong("likeCount").intValue() : 0);

                                postList.add(post);
                            } catch (Exception e) {
                                Log.e("HomeFragment", "Post parsing error: ", e);
                            }
                        }
                        postAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(getContext(), "Error loading posts", Toast.LENGTH_SHORT).show();
                        Log.e("HomeFragment", "Firebase error: ", task.getException());
                    }
                });
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
