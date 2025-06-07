package com.example.splitbooks.activity.search;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.splitbooks.DTO.response.ReviewResponse;
import com.example.splitbooks.R;
import com.example.splitbooks.DTO.request.ReviewItem;
import com.example.splitbooks.network.JwtManager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {
    public interface OnReviewReplyClickListener {
        void onReplyClick(ReviewResponse review);
        void onDeleteClick(ReviewResponse review);
    }

    private List<ReviewItem> reviewItems = new ArrayList<>();

    private OnReviewReplyClickListener replyClickListener;

    public ReviewAdapter(List<ReviewResponse> reviews, OnReviewReplyClickListener listener) {
        this.replyClickListener = listener;
        setReviews(reviews);
    }

    public void setReviews(List<ReviewResponse> reviews) {
        this.reviewItems = flattenReviews(reviews);
        notifyDataSetChanged();
    }

    private List<ReviewItem> flattenReviews(List<ReviewResponse> reviews) {
        List<ReviewItem> flatList = new ArrayList<>();
        for (ReviewResponse review : reviews) {
            addReviewWithReplies(review, 0, flatList);
        }
        return flatList;
    }

    private void addReviewWithReplies(ReviewResponse review, int level, List<ReviewItem> flatList) {
        flatList.add(new ReviewItem(review, level));
        if (review.getReplies() != null) {
            for (ReviewResponse reply : review.getReplies()) {
                addReviewWithReplies(reply, level + 1, flatList);
            }
        }
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        ReviewItem item = reviewItems.get(position);
        ReviewResponse review = item.review;

        holder.tvUsername.setText(review.getUsername());
        holder.tvComment.setText(review.getReviewText());
        holder.tvRating.setText("Rating: " + review.getRating());
        Long currentProfileId = JwtManager.getMyProfileId(holder.itemView.getContext());

        if (review.getProfileId().equals(currentProfileId)) {
            holder.btnDelete.setVisibility(View.VISIBLE);
            holder.btnDelete.setOnClickListener(v -> {
                if (replyClickListener != null) {
                    replyClickListener.onDeleteClick(review);
                }
            });
        } else {
            holder.btnDelete.setVisibility(View.GONE);
        }
        try {
            LocalDateTime dateTime = LocalDateTime.parse(review.getCreatedAt());
            String formattedDate = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            holder.tvDate.setText(formattedDate);
        } catch (Exception e) {
            holder.tvDate.setText(review.getCreatedAt());  // fallback: raw string
        }

        // Set indentation padding (40px per indent level)
        int indentPx = item.indentLevel * 40;
        holder.itemView.setPadding(indentPx,
                holder.itemView.getPaddingTop(),
                holder.itemView.getPaddingRight(),
                holder.itemView.getPaddingBottom());

        holder.btnReply.setOnClickListener(v -> {
            if (replyClickListener != null) {
                replyClickListener.onReplyClick(review);
            }
        });
    }

    @Override
    public int getItemCount() {
        return reviewItems.size();
    }

    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView tvUsername, tvComment, tvRating, tvDate ;
        Button btnReply,btnDelete;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvComment = itemView.findViewById(R.id.tvComment);
            tvRating = itemView.findViewById(R.id.tvRating);
            tvDate = itemView.findViewById(R.id.tvDate);
            btnReply = itemView.findViewById(R.id.btnReply);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }

}