package com.example.splitbooks.activity.chats;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.splitbooks.DTO.response.ShortChatResponse;
import com.example.splitbooks.R;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private List<ShortChatResponse> chatList = new ArrayList<>();
    private final OnChatClickListener listener;

    public interface OnChatClickListener {
        void onChatClick(ShortChatResponse chat);
    }

    public ChatAdapter(OnChatClickListener listener) {
        this.listener = listener;
    }

    public void setChatList(List<ShortChatResponse> newChats) {
        this.chatList = newChats;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ShortChatResponse chat = chatList.get(position);
        holder.bind(chat);

        Log.d("ChatAdapter", "Loading image URL: " + chat.getChatProfileUrl());

        String imageUrl = chat.getChatProfileUrl();
        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.default_avatar)
                    .error(R.drawable.default_avatar)
                    .circleCrop()
                    .into(holder.imageViewChatType);
        } else {

            int fallbackImage = chat.isGroupChat() ? R.drawable.group_chat_svgrepo_com : R.drawable.default_avatar;
            holder.imageViewChatType.setImageResource(fallbackImage);
        }

    }



    @Override
    public int getItemCount() {
        return chatList.size();
    }

    class ChatViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageViewChatType;
        private final TextView textViewChatName;
        private final TextView textViewLastUpdated;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewChatType = itemView.findViewById(R.id.imageViewChatType);
            textViewChatName = itemView.findViewById(R.id.textViewChatName);
            textViewLastUpdated = itemView.findViewById(R.id.textViewLastUpdated);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onChatClick(chatList.get(position));
                }
            });
        }

        public void bind(ShortChatResponse chat) {
            textViewChatName.setText(chat.getChatName());

            if (chat.getLastUpdated() != null && !chat.getLastUpdated().isEmpty()) {
                try {
                    LocalDateTime lastUpdatedDateTime = LocalDateTime.parse(chat.getLastUpdated());
                    String formattedLastUpdated = formatLastUpdatedAgoOrDate(lastUpdatedDateTime);
                    textViewLastUpdated.setText(formattedLastUpdated);
                } catch (Exception e) {
                    textViewLastUpdated.setText("");
                }
            } else {
                textViewLastUpdated.setText("");
            }
        }
    }


    public void appendChatList(List<ShortChatResponse> moreChats) {
        int insertPosition = chatList.size();
        chatList.addAll(moreChats);
        notifyItemRangeInserted(insertPosition, moreChats.size());
    }
    private String formatLastUpdatedAgoOrDate(LocalDateTime lastUpdated) {
        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(lastUpdated, now);

        long minutes = duration.toMinutes();
        long hours = duration.toHours();

        if (minutes < 1) {
            return "just now";
        } else if (minutes < 60) {
            return minutes + " mins ago";
        } else if (hours < 24) {
            return hours + " hours ago";
        } else {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d, HH:mm");
            return lastUpdated.format(formatter);
        }
    }

}
