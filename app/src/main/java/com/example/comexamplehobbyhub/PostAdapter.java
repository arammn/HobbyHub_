package com.example.comexamplehobbyhub;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {
    private List<Post> postList;

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
        holder.userName.setText(post.getUserName());
        holder.postTime.setText(post.getPostTime());
        holder.postContent.setText(post.getPostContent());

        // Load user avatar, if available, else use default
        String avatarUrl = post.getUserAvatar();
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(avatarUrl)
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .error(R.drawable.ic_profile_placeholder)
                    .into(holder.userAvatar);

        } else {
            holder.userAvatar.setImageResource(R.drawable.ic_profile_placeholder);  // Default avatar
        }

        // Handling post image
        if (post.getPostImage() != null && !post.getPostImage().isEmpty()) {
            holder.postImage.setVisibility(View.VISIBLE);
            Glide.with(holder.itemView.getContext())
                    .load(post.getPostImage())
                    .into(holder.postImage);
        } else {
            holder.postImage.setVisibility(View.GONE);
        }

        // Show edit button only if the current user is the post creator
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (post.getUserId().equals(currentUserId)) {
            holder.btnEditPost.setVisibility(View.VISIBLE);
            holder.btnEditPost.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), EditPostActivity.class);
                intent.putExtra("postId", post.getPostId());
                v.getContext().startActivity(intent);
            });
        } else {
            holder.btnEditPost.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView userName, postTime, postContent;
        ImageView userAvatar, postImage;
        ImageButton btnEditPost; // Added edit button

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.userName);
            postTime = itemView.findViewById(R.id.postTime);
            postContent = itemView.findViewById(R.id.postContent);
            userAvatar = itemView.findViewById(R.id.userAvatar);
            postImage = itemView.findViewById(R.id.postImage);
            btnEditPost = itemView.findViewById(R.id.btnEditPost); // Initialize edit button
        }
    }
}
