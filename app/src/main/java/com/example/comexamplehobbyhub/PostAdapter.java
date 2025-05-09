package com.example.comexamplehobbyhub;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {
    private List<Post> postList;
    private Map<String, Integer> userXpCache = new HashMap<>();
    private Map<String, UserData> userDataCache = new HashMap<>();

    private static class UserData {
        String nickname;
        String profileImage;

        UserData(String nickname, String profileImage) {
            this.nickname = nickname;
            this.profileImage = profileImage;
        }
    }

    public PostAdapter(List<Post> postList) {
        this.postList = postList;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_item, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = postList.get(position);
        holder.postTime.setText(post.getPostTime());
        holder.postContent.setText(post.getPostContent());

        holder.likeCount.setText(String.valueOf(post.getLikeCount() > 0 ? post.getLikeCount() : 0));

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserId = currentUser != null ? currentUser.getUid() : null;

        String userId = post.getUserId();
        holder.userName.setTag(userId);
        holder.userAvatar.setTag(userId);
        holder.userRankMedal.setTag(userId);

        ImageView medalView = holder.userRankMedal;
        if (userXpCache.containsKey(userId)) {
            updateMedalImage(medalView, userXpCache.get(userId));
        } else {
            medalView.setTag(userId);
            FirebaseFirestore.getInstance().collection("users").document(userId)
                    .get()
                    .addOnSuccessListener(document -> {
                        Long xp = document.getLong("xp");
                        userXpCache.put(userId, xp != null ? xp.intValue() : 0);
                        if (userId.equals(medalView.getTag())) {
                            updateMedalImage(medalView, userXpCache.get(userId));
                        }
                    });
        }

        if (userDataCache.containsKey(userId)) {
            UserData userData = userDataCache.get(userId);
            holder.userName.setText(userData.nickname);
            loadAvatar(holder.userAvatar, userData.profileImage);
        } else {
            fetchUserData(userId, holder);
        }

        List<String> likes = post.getLikes() != null ? post.getLikes() : Collections.emptyList();
        boolean isLiked = currentUserId != null && likes.contains(currentUserId);
        holder.btnLike.setImageResource(isLiked ? R.drawable.ic_likedd : R.drawable.ic_likee);
        holder.likeCount.setText(String.valueOf(post.getLikeCount()));

        holder.btnLike.setOnClickListener(v -> {
            if (currentUserId == null) {
                Toast.makeText(v.getContext(), "Please login to like posts", Toast.LENGTH_SHORT).show();
                return;
            }
            handleLikeClick(post, currentUserId, holder);
        });

        if (post.getPostImage() != null && !post.getPostImage().isEmpty()) {
            holder.postImage.setVisibility(View.VISIBLE);
            Glide.with(holder.itemView.getContext())
                    .load(post.getPostImage())
                    .into(holder.postImage);
        } else {
            holder.postImage.setVisibility(View.GONE);
        }

        holder.btnEditPost.setVisibility(
                currentUserId != null && currentUserId.equals(post.getUserId())
                        ? View.VISIBLE
                        : View.GONE
        );

        holder.btnEditPost.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), EditPostActivity.class);
            intent.putExtra("postId", post.getPostId());
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }




    static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView userName, postTime, postContent;
        ImageView userAvatar, postImage, userRankMedal;
        ImageButton btnEditPost;

        ImageButton btnLike;
        TextView likeCount;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.userName);
            postTime = itemView.findViewById(R.id.postTime);
            postContent = itemView.findViewById(R.id.postContent);
            userAvatar = itemView.findViewById(R.id.userAvatar);
            postImage = itemView.findViewById(R.id.postImage);
            btnEditPost = itemView.findViewById(R.id.btnEditPost);
            userRankMedal = itemView.findViewById(R.id.userRankMedal);
            btnLike = itemView.findViewById(R.id.btnLike);
            likeCount = itemView.findViewById(R.id.likeCount);
        }
    }
    private void updateMedalImage(ImageView medalView, int xp) {
        if (xp < 100) {
            medalView.setImageResource(R.drawable.bronze_medal);
        } else if (xp < 300) {
            medalView.setImageResource(R.drawable.silver_medal);
        } else if (xp >= 300 & xp <= 10000) {
            medalView.setImageResource(R.drawable.gold_medal);
        } else if (xp >= 10000) {
            medalView.setImageResource(R.drawable.manager);
    }
    }

    private void handleLikeClick(Post post, String currentUserId, PostViewHolder holder) {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;

        DocumentReference postRef = FirebaseFirestore.getInstance().collection("posts").document(post.getPostId());

        List<String> likes = post.getLikes() != null ? post.getLikes() : new ArrayList<>();
        boolean newIsLiked = !likes.contains(currentUserId);

        int newCount = newIsLiked ? post.getLikeCount() + 1 : post.getLikeCount() - 1;
        holder.likeCount.setText(String.valueOf(newCount));
        holder.btnLike.setImageResource(newIsLiked ? R.drawable.ic_likedd : R.drawable.ic_likee);

        FirebaseFirestore.getInstance().runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(postRef);
            List<String> updatedLikes = snapshot.contains("likes") ?
                    (List<String>) snapshot.get("likes") : new ArrayList<>();

            if (newIsLiked) {
                if (!updatedLikes.contains(currentUserId)) {
                    updatedLikes.add(currentUserId);
                }
            } else {
                updatedLikes.remove(currentUserId);
            }

            transaction.update(postRef, "likes", updatedLikes);
            transaction.update(postRef, "likeCount", newCount);
            return null;
        }).addOnFailureListener(e -> {
            holder.likeCount.setText(String.valueOf(post.getLikeCount()));
            holder.btnLike.setImageResource(likes.contains(currentUserId) ?
                    R.drawable.ic_likedd : R.drawable.ic_likee);
            Toast.makeText(holder.itemView.getContext(), "Error updating like", Toast.LENGTH_SHORT).show();
        });
    }

    private void fetchUserData(String userId, PostViewHolder holder) {
        FirebaseFirestore.getInstance().collection("users").document(userId)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String nickname = document.getString("nickname");
                        String profileImage = document.getString("profileImage");
                        Long xp = document.getLong("xp");
                        int xpValue = xp != null ? xp.intValue() : 0;

                        userDataCache.put(userId, new UserData(nickname, profileImage));
                        userXpCache.put(userId, xpValue);

                        if (userId.equals(holder.userName.getTag())) {
                            holder.userName.setText(nickname != null ? nickname : "Аноним");
                            loadAvatar(holder.userAvatar, profileImage);
                        }
                        if (userId.equals(holder.userRankMedal.getTag())) {
                            updateMedalImage(holder.userRankMedal, xpValue);
                        }
                    }
                });
    }

    private void loadAvatar(ImageView imageView, String url) {
        if (url != null && !url.isEmpty()) {
            Glide.with(imageView.getContext())
                    .load(url)
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .error(R.drawable.ic_profile_placeholder)
                    .into(imageView);
        } else {
            imageView.setImageResource(R.drawable.ic_profile_placeholder);
        }
    }
}