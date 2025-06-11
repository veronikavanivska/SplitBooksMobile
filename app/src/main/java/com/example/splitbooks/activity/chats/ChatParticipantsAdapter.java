package com.example.splitbooks.activity.chats;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.splitbooks.DTO.response.ChatResponse;
import com.example.splitbooks.DTO.response.ProfileType;
import com.example.splitbooks.R;

import java.util.List;

public class ChatParticipantsAdapter extends RecyclerView.Adapter<ChatParticipantsAdapter.ViewHolder> {

    private List<ChatResponse.ParticipantInfo> participants;

    public ChatParticipantsAdapter(List<ChatResponse.ParticipantInfo> participants) {
        this.participants = participants;
    }

    @NonNull
    @Override
    public ChatParticipantsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_participant, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatParticipantsAdapter.ViewHolder holder, int position) {
        ChatResponse.ParticipantInfo participant = participants.get(position);
        holder.username.setText(participant.getUsername());
        if (participant.getProfileType() == ProfileType.ANONYMOUS) {
            holder.itemView.setBackgroundColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.colorGrayLight)
            );
        } else {
            holder.itemView.setBackgroundColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.colorWhite)
            );
        }
        Glide.with(holder.avatar.getContext())
                .load(participant.getAvatarUrl())
                .placeholder(R.drawable.default_avatar)
                .circleCrop()
                .into(holder.avatar);
    }

    @Override
    public int getItemCount() {
        return participants.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView avatar;
        TextView username;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.participant_avatar);
            username = itemView.findViewById(R.id.participant_username);
        }
    }
}
