package com.example.splitbooks.activity.chats;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.splitbooks.DTO.response.ProfileType;
import com.example.splitbooks.DTO.response.ShortProfileResponse;
import com.example.splitbooks.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SelectableProfileAdapter extends RecyclerView.Adapter<SelectableProfileAdapter.ViewHolder> {

    private List<ShortProfileResponse> profiles = new ArrayList<>();
    private Set<Long> selectedProfileIds = new HashSet<>();


    public interface OnProfileSelectedListener {
        void onProfileSelected(ShortProfileResponse profile);
        void onProfileDeselected(ShortProfileResponse profile);
    }

    private OnProfileSelectedListener listener;

    public void setOnProfileSelectedListener(OnProfileSelectedListener listener) {
        this.listener = listener;
    }

    public void setProfiles(List<ShortProfileResponse> profiles) {
        this.profiles = profiles != null ? profiles : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setSelectedProfiles(List<ShortProfileResponse> selectedProfiles) {
        selectedProfileIds.clear();
        if (selectedProfiles != null) {
            for (ShortProfileResponse profile : selectedProfiles) {
                selectedProfileIds.add(profile.getId());
            }
        }
        notifyDataSetChanged();
    }

    public List<Long> getSelectedProfileIds() {
        return new ArrayList<>(selectedProfileIds);
    }

    public boolean isSelected(Long profileId) {
        return selectedProfileIds.contains(profileId);
    }

    @NonNull
    @Override
    public SelectableProfileAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_short_profile_selectable, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SelectableProfileAdapter.ViewHolder holder, int position) {
        ShortProfileResponse profile = profiles.get(position);
        holder.nameText.setText(profile.getUsername());

        Glide.with(holder.itemView.getContext())
                .load(profile.getAvatarUrl())
                .placeholder(R.drawable.default_avatar)
                .circleCrop()
                .into(holder.avatar);


        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(selectedProfileIds.contains(profile.getId()));

        if (profile.getType() == ProfileType.ANONYMOUS) {
            holder.itemView.setBackgroundColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.colorGrayLight)
            );
        } else {
            holder.itemView.setBackgroundColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.colorWhite)
            );
        }
        View.OnClickListener toggleSelection = v -> {
            boolean currentlySelected = selectedProfileIds.contains(profile.getId());
            if (currentlySelected) {
                selectedProfileIds.remove(profile.getId());
                holder.checkBox.setChecked(false);
                if (listener != null) listener.onProfileDeselected(profile);
            } else {
                selectedProfileIds.add(profile.getId());
                holder.checkBox.setChecked(true);
                if (listener != null) listener.onProfileSelected(profile);
            }
        };

        holder.checkBox.setOnClickListener(toggleSelection);
        holder.itemView.setOnClickListener(toggleSelection);
    }

    @Override
    public int getItemCount() {
        return profiles.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameText;
        ImageView avatar;
        CheckBox checkBox;

        ViewHolder(View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.short_profile_username);
            avatar = itemView.findViewById(R.id.short_profile_avatar);
            checkBox = itemView.findViewById(R.id.short_profile_checkbox);
        }
    }
}
