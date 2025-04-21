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

import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private Context context;
    private List<Event> eventList;

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

        holder.tvEventName.setText(event.getName());
        holder.tvParticipants.setText("Participants: " + event.getParticipants());
        holder.tvLocation.setText("Location: " + event.getLatitude() + ", " + event.getLongitude());

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
        TextView tvEventName, tvParticipants, tvLocation;
        ImageButton btnNavigate;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEventName = itemView.findViewById(R.id.tvEventName);
            tvParticipants = itemView.findViewById(R.id.tvParticipants);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            btnNavigate = itemView.findViewById(R.id.btnNavigate);
        }
    }
}
