package com.example.splitbooks;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.splitbooks.DTO.response.ShortProfileResponse;

import java.util.List;

public class FollowListAdapter extends RecyclerView.Adapter<FollowListAdapter.ViewHolder> {

    private List<ShortProfileResponse> profiles;

    public FollowListAdapter(List<ShortProfileResponse> profiles) {
        this.profiles = profiles;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_short_profile, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ShortProfileResponse profile = profiles.get(position);
        holder.username.setText(profile.getUsername());
        holder.itemView.setOnClickListener(v->{
            Context context = v.getContext();
            Intent intent = new Intent(context, OtherProfileActivity.class);
            intent.putExtra("profileId", profile.getId());
            context.startActivity(intent);
        });

        Glide.with(holder.itemView.getContext())
                .load(profile.getAvatarUrl())
                .placeholder(R.drawable.default_avatar)
                .circleCrop()
                .into(holder.avatar);
    }

    @Override
    public int getItemCount() {
        return profiles.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView avatar;
        TextView username;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.short_profile_avatar);
            username = itemView.findViewById(R.id.short_profile_username);
        }
    }
}
