package com.example.splitbooks.activity.search;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.splitbooks.DTO.request.ReviewRequest;
import com.example.splitbooks.DTO.response.BookDetailResponse;
import com.example.splitbooks.DTO.response.BookWithReviewsResponse;
import com.example.splitbooks.DTO.response.ReviewResponse;
import com.example.splitbooks.R;
import com.example.splitbooks.activity.chats.AllChatsActivity;
import com.example.splitbooks.activity.home.HomePageActivity;
import com.example.splitbooks.activity.profile.PublicProfileActivity;
import com.example.splitbooks.network.ApiClient;
import com.example.splitbooks.network.ApiService;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookDetailActivity extends AppCompatActivity {

    private ImageView ivCover;
    private String volumeId;
    private TextView tvTitle, tvAuthor, tvDescription;
    private RecyclerView rvReviews;
    private ReviewAdapter reviewAdapter;
    private Button btnAddReview;
    private MaterialButton btn_back;
    private Button btnAddToLibrary;
    private boolean isInLibrary = false;
    private BottomNavigationView bottomNavigation;
    private com.airbnb.lottie.LottieAnimationView lottieLoader;

    ScrollView scrollView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_details);

        ivCover = findViewById(R.id.avatar_image);
        tvTitle = findViewById(R.id.tvTitle);
        tvAuthor = findViewById(R.id.tvAuthor);
        tvDescription = findViewById(R.id.tvDescription);
        rvReviews = findViewById(R.id.rvReviews);
        btnAddReview = findViewById(R.id.btnAddReview);
        bottomNavigation = findViewById(R.id.other_bottom_navigation);
        btnAddReview.setOnClickListener(v -> showAddReviewDialog(null));
        btn_back = findViewById(R.id.btn_back);
        btnAddToLibrary = findViewById(R.id.btnAddToLibrary);
        lottieLoader = findViewById(R.id.lottieLoader);
        scrollView = findViewById(R.id.scrollViewContent);
        lottieLoader = findViewById(R.id.lottieLoader);
        scrollView = findViewById(R.id.scrollViewContent);



        rvReviews.setLayoutManager(new LinearLayoutManager(this));
        reviewAdapter = new ReviewAdapter(new ArrayList<>(), new ReviewAdapter.OnReviewReplyClickListener() {
            @Override
            public void onReplyClick(ReviewResponse review) {
                showAddReviewDialog(review.getReviewId());
            }

            @Override
            public void onDeleteClick(ReviewResponse review) {
                showDeleteDialog(review.getReviewId());
            }
        });

        rvReviews.setAdapter(reviewAdapter);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getOnBackPressedDispatcher().onBackPressed();
            }
        });

        volumeId = getIntent().getStringExtra("volumeId");


        if (volumeId != null) {
            loadBookDetails(volumeId);
        } else {
            Toast.makeText(this, "Invalid book ID", Toast.LENGTH_SHORT).show();
            finish();
        }

        btnAddToLibrary.setOnClickListener(v -> {
            if (!isInLibrary) {
                addBookToLibrary();
            }else {
                deleteBookFromLibrary(volumeId);
            }
        });


        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if(id == R.id.action_profile){
                Intent intent = new Intent(this, PublicProfileActivity.class);
                startActivity(intent);
                finish();
                return true;
            }else if(id == R.id.action_home){
                Intent intent = new Intent(this, HomePageActivity.class);
                startActivity(intent);
                finish();
                return true;
            }else if(id == R.id.action_search){
                Intent intent = new Intent(this, SearchProfilesActivity.class);
                startActivity(intent);
                finish();
                return true;
            }else if(id == R.id.action_library){
                Intent intent = new Intent(this, SearchBookActivity.class);
                startActivity(intent);
                finish();
                return true;
            }else if(id == R.id.action_chats){
                Intent intent = new Intent(this, AllChatsActivity.class);
                startActivity(intent);
                finish();
                return true;
            }
            return false;
        });


    }

    private void showLoading() {
        lottieLoader.setVisibility(View.VISIBLE);
        scrollView.setVisibility(View.GONE);
    }

    private void hideLoading() {
        lottieLoader.setVisibility(View.GONE);
        scrollView.setVisibility(View.VISIBLE);
    }
    private void loadBookDetails(String volumeId) {
        showLoading();
        ApiService apiService = ApiClient.getApiService(getApplicationContext());
        Call<BookWithReviewsResponse> call = apiService.getBookById(volumeId);
        call.enqueue(new Callback<BookWithReviewsResponse>() {
            @Override
            public void onResponse(Call<BookWithReviewsResponse> call, Response<BookWithReviewsResponse> response) {
                hideLoading();
                if (response.isSuccessful() && response.body() != null) {

                    BookDetailResponse book = response.body().getBook();
                    List<ReviewResponse> reviews = response.body().getReviews();

                    tvTitle.setText(book.getVolumeInfo().getTitle());
                    List<String> authors = book.getVolumeInfo().getAuthors();
                    String autorsText = TextUtils.join(", ", authors);
                    tvAuthor.setText(autorsText);

                    String rawDescription = book.getVolumeInfo().getDescription();
                    Spanned cleanTeks = rawDescription == null  ? null :  Html.fromHtml(rawDescription, Html.FROM_HTML_MODE_LEGACY) ;
                    tvDescription.setText(cleanTeks != null ? cleanTeks : "No description available");
                    isInLibrary = book.getVolumeInfo().getIsInLibrary();
                    updateLibraryButton();
                    String thumbnail = book.getVolumeInfo().getImageLinks() != null
                            ? book.getVolumeInfo().getImageLinks().getThumbnail()
                            : null;

                    if (thumbnail != null) {
                        Glide.with(BookDetailActivity.this)
                                .load(thumbnail)
                                .into(ivCover);
                    } else {
                        ivCover.setImageResource(R.drawable.book_education_library_2_svgrepo_com);
                    }

                    reviewAdapter.setReviews(reviews);

                } else {
                    Toast.makeText(BookDetailActivity.this, "Failed to load book details", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BookWithReviewsResponse> call, Throwable t) {
                Toast.makeText(BookDetailActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @SuppressLint("ResourceAsColor")
    private void updateLibraryButton() {
        if (isInLibrary) {
            btnAddToLibrary.setText("Delete from Library");
            btnAddToLibrary.setEnabled(true);
            btnAddToLibrary.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));

        } else {
            btnAddToLibrary.setText("Add to Library");
            btnAddToLibrary.setEnabled(true);
            btnAddToLibrary.setBackgroundColor(getResources().getColor(R.color.dark_green));

        }
    }
    private void showAddReviewDialog(@Nullable Long parentReviewId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(parentReviewId == null ? "Write a Review" : "Reply to Review");


        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_review, null);

        EditText etComment = dialogView.findViewById(R.id.etComment);
        RatingBar ratingBar = dialogView.findViewById(R.id.ratingBar);

        builder.setView(dialogView);

        builder.setPositiveButton("Submit", (dialog, which) -> {
            String comment = etComment.getText().toString().trim();
            int rating = (int) ratingBar.getRating();

            if (comment.isEmpty()) {
                Toast.makeText(this, "Please write a comment", Toast.LENGTH_SHORT).show();
                return;
            }
            if (rating == 0) {
                Toast.makeText(this, "Please give a rating", Toast.LENGTH_SHORT).show();
                return;
            }

            submitReview(comment, rating, parentReviewId);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.white);
    }
    private void showDeleteDialog(Long reviewId) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Review")
                .setMessage("Are you sure you want to delete this review?")
                .setPositiveButton("Yes", (dialog, which) -> deleteReview(reviewId))
                .setNegativeButton("No", null)
                .show()
                .getWindow().setBackgroundDrawableResource(android.R.color.white);
    }

    private void submitReview(String reviewText, int rating, @Nullable Long parentReviewId) {

        ReviewRequest request = new ReviewRequest();
        request.setVolumeId(volumeId);
        request.setReviewText(reviewText);
        request.setRating(rating);
        request.setParentReviewId(parentReviewId);

        ApiService apiService = ApiClient.getApiService(getApplicationContext());
        Call<ReviewResponse> call = apiService.addReview(volumeId, request);

        call.enqueue(new Callback<ReviewResponse>() {
            @Override
            public void onResponse(Call<ReviewResponse> call, Response<ReviewResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(BookDetailActivity.this, "Review added", Toast.LENGTH_SHORT).show();

                    loadBookDetails(volumeId);

                } else {
                    Toast.makeText(BookDetailActivity.this, "Failed to add review", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ReviewResponse> call, Throwable t) {
                Toast.makeText(BookDetailActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteReview(Long reviewId) {
        ApiService apiService = ApiClient.getApiService(getApplicationContext());
        apiService.deleteReview(reviewId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(BookDetailActivity.this, "Review deleted", Toast.LENGTH_SHORT).show();
                    loadBookDetails(volumeId);
                } else {
                    Toast.makeText(BookDetailActivity.this, "Failed to delete review", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(BookDetailActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addBookToLibrary() {
        ApiService apiService = ApiClient.getApiService(getApplicationContext());
        Call<ResponseBody> call = apiService.addBookToLibrary(volumeId);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(BookDetailActivity.this, "Book added to library", Toast.LENGTH_SHORT).show();
                    isInLibrary = true;
                    updateLibraryButton();

                } else {
                    Toast.makeText(BookDetailActivity.this, "Failed to add book", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(BookDetailActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void deleteBookFromLibrary(String volumeId) {
        ApiService apiService = ApiClient.getApiService(getApplicationContext());
        Call<ResponseBody> call = apiService.removeBookFromLibrary(volumeId);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    isInLibrary = false;
                    updateLibraryButton();
                    Toast.makeText(BookDetailActivity.this, "Book removed", Toast.LENGTH_SHORT).show();
                } else {

                    Toast.makeText(BookDetailActivity.this, "Error: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(BookDetailActivity.this, "Failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }




}

