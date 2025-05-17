package com.example.comexamplehobbyhub;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private Context context;
    private List<Event> eventList;

    private String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

    public EventAdapter(Context context, List<Event> eventList) {
        this.context = context;
        this.eventList = eventList;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.event_item, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventList.get(position);
        if (event == null) return;

        if (event.getCreatorId() == null) return;
        if (event.getParticipants() == null) event.setParticipants(new ArrayList<>());


        holder.tvEventName.setText(event.getName());

        boolean isCreator = event.getCreatorId().equals(currentUserId);
        holder.btnSettings.setVisibility(isCreator ? View.VISIBLE : View.GONE);
        holder.btnJoin.setVisibility(isCreator ? View.GONE : View.VISIBLE);

        // Обновление состояния кнопки Join/Leave
        holder.btnJoin.setImageResource(event.isUserParticipant(currentUserId)
                ? R.drawable.logout
                : R.drawable.create_event);

        holder.tvParticipants.setText("Participants: " + event.getParticipantsCount());
        // В onBindViewHolder
        Date eventDate = event.getEventDate();
        if (eventDate != null) {
            long currentTime = System.currentTimeMillis();
            long eventTime = eventDate.getTime();
            long diff = eventTime - currentTime;

            if (diff < 0) {
                holder.tvDaysLeft.setText("Event expired");
            } else {
                long days = TimeUnit.MILLISECONDS.toDays(diff);
                holder.tvDaysLeft.setText(String.format("Days left: %d", days + 1));
            }
        } else {
            holder.tvDaysLeft.setText("Date not set");
        }

        // Обработка нажатия Join/Leave
        holder.btnJoin.setOnClickListener(v -> {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference eventRef = db.collection("events").document(event.getId());

            if(event.getParticipants().contains(currentUserId)) {
                eventRef.update("participants", FieldValue.arrayRemove(currentUserId));
            } else {
                eventRef.update("participants", FieldValue.arrayUnion(currentUserId));
            }
        });

        // Обработка нажатия Settings
        holder.btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(context, EventSettingsActivity.class);
            intent.putExtra("eventId", event.getId());
            context.startActivity(intent);
        });

        holder.btnNavigate.setOnClickListener(v -> {
            String uri = "geo:" + event.getLatitude() + "," + event.getLongitude() + "?q=" + event.getLatitude() + "," + event.getLongitude();
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            intent.setPackage("com.google.android.apps.maps");
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView tvEventName, tvParticipants;
        ImageButton btnNavigate, btnSettings, btnJoin;
        TextView tvDaysLeft;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEventName = itemView.findViewById(R.id.tvEventName);
            tvParticipants = itemView.findViewById(R.id.tvParticipants);
            tvDaysLeft = itemView.findViewById(R.id.tvDaysLeft);
            btnNavigate = itemView.findViewById(R.id.btnNavigate);
            btnSettings = itemView.findViewById(R.id.btnSettings);
            btnJoin = itemView.findViewById(R.id.btnJoin);
        }
    }
}
