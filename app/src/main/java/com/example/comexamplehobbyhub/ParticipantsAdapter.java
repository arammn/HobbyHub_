package com.example.comexamplehobbyhub;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ParticipantsAdapter extends RecyclerView.Adapter<ParticipantsAdapter.ViewHolder> {
    private List<User> participants = new ArrayList<>();

    public ParticipantsAdapter(List<User> participants) {
        if (participants != null) {
            this.participants = participants;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_participant, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = participants.get(position);
        holder.tvUserName.setText(user.getUsername());

        // Load avatar with Glide (example)
        /*
        Glide.with(holder.itemView.getContext())
             .load(user.getAvatarUrl())
             .placeholder(R.drawable.ic_default_avatar)
             .into(holder.ivAvatar);
        */
    }

    @Override
    public int getItemCount() {
        return participants.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName;
        ImageView ivAvatar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tv_user_name);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
        }
    }
}