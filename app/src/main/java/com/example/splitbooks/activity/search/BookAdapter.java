package com.example.splitbooks.activity.search;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.splitbooks.DTO.response.BookResponse;
import com.example.splitbooks.R;

import java.util.List;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {

    public interface OnBookClickListener {
        void onBookClick(BookResponse.Item book);
    }

    private final List<BookResponse.Item> books;
    private final OnBookClickListener listener;

    public BookAdapter(List<BookResponse.Item> books, OnBookClickListener listener) {
        this.books = books;
        this.listener = listener;
    }


    public void updateBooks(List<BookResponse.Item> newBooks) {
        books.clear();
        books.addAll(newBooks);
        notifyDataSetChanged();
    }

    public void addBooks(List<BookResponse.Item> moreBooks) {
        int oldSize = books.size();
        books.addAll(moreBooks);
        notifyItemRangeInserted(oldSize, moreBooks.size());
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_books_activity, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        BookResponse.Item book = books.get(position);
        BookResponse.VolumeInfo info = book.getVolumeInfo();

        holder.title.setText(info.getTitle());
        holder.author.setText(info.getAuthors() != null ?
                String.join(", ", info.getAuthors()) : "Unknown Author");

        if (info.getImageLinks() != null && info.getImageLinks().getThumbnail() != null) {
            Glide.with(holder.itemView.getContext())
                    .load(info.getImageLinks().getThumbnail())
                    .placeholder(R.drawable.book_education_library_2_svgrepo_com)
                    .into(holder.thumbnail);
        } else {
            holder.thumbnail.setImageResource(R.drawable.book_education_library_2_svgrepo_com);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBookClick(book);
            }
        });
    }

    @Override
    public int getItemCount() {
        return books.size();
    }

    static class BookViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnail;
        TextView title, author;

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnail = itemView.findViewById(R.id.book_thumbnail);
            title = itemView.findViewById(R.id.book_title);
            author = itemView.findViewById(R.id.book_author);
        }
    }
}