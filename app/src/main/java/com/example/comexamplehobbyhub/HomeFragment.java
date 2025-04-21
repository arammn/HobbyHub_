package com.example.comexamplehobbyhub;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
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

        db = FirebaseFirestore.getInstance();
        loadPosts();



        return view;

    }

    private void loadPosts() {
        db.collection("posts")
                .orderBy("postTime")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        postList.clear();
                        for (DocumentSnapshot document : task.getResult()) {
                            try {
                                Post post = document.toObject(Post.class);
                                if (post != null) {
                                    post.getUserAvatar();
                                    String userAvatar = document.getString("userAvatar");
                                    post.setUserAvatar(userAvatar); // Set the avatar URL

                                    postList.add(post);
                                }
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


}
