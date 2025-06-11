package com.example.splitbooks.activity.chats;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.splitbooks.DTO.response.SendMessageResponse;
import com.example.splitbooks.R;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    private List<SendMessageResponse> messages;
    private Long currentUserId;

    public MessageAdapter(List<SendMessageResponse> messages, Long currentUserId) {
        this.messages = messages;
        this.currentUserId = currentUserId;
    }

    @Override
    public int getItemViewType(int position) {
        SendMessageResponse message = messages.get(position);
        Log.d("MessageAdapter", "Msg pos=" + position + ", senderId=" + message.getSenderId() + ", currentUserId=" + currentUserId);
        if (message.getSenderId() != null && message.getSenderId().equals(currentUserId)) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_send, parent, false);
            return new SentMessageHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_received, parent, false);
            return new ReceivedMessageHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        SendMessageResponse message = messages.get(position);
        if (holder.getItemViewType() == VIEW_TYPE_SENT) {
            ((SentMessageHolder) holder).bind(message);
        } else {
            ((ReceivedMessageHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class SentMessageHolder extends RecyclerView.ViewHolder {
        TextView textViewMessage, textViewTimestamp, textViewSender;

        SentMessageHolder(@NonNull View itemView) {
            super(itemView);
            textViewMessage = itemView.findViewById(R.id.textViewMessage);
            textViewTimestamp = itemView.findViewById(R.id.textViewTimestamp);
            textViewSender = itemView.findViewById(R.id.textViewSender);
        }

        void bind(SendMessageResponse message) {
            textViewMessage.setText(message.getContent());
            textViewSender.setText(message.getSenderUsername());
            textViewTimestamp.setText(formatTimestamp(message.getTimestamp()));
        }
    }


    static class ReceivedMessageHolder extends RecyclerView.ViewHolder {
        TextView textViewMessage, textViewTimestamp, textViewSender;

        ReceivedMessageHolder(@NonNull View itemView) {
            super(itemView);
            textViewMessage = itemView.findViewById(R.id.textViewMessage);
            textViewTimestamp = itemView.findViewById(R.id.textViewTimestamp);
            textViewSender = itemView.findViewById(R.id.textViewSender);
        }

        void bind(SendMessageResponse message) {
            textViewMessage.setText(message.getContent());
            textViewSender.setText(message.getSenderUsername());
            textViewTimestamp.setText(formatTimestamp(message.getTimestamp()));
        }
    }


    private static String formatTimestamp(String isoTimestamp) {
        if (isoTimestamp == null) return "";
        if (isoTimestamp.length() >= 16) {
            return isoTimestamp.substring(11, 16);
        }
        return isoTimestamp;
    }
}
