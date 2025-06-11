    package com.example.splitbooks.activity.search;

    import android.app.ActivityOptions;
    import android.content.Intent;
    import android.graphics.Canvas;
    import android.graphics.Color;
    import android.graphics.Paint;
    import android.graphics.PorterDuff;
    import android.graphics.RectF;
    import android.graphics.drawable.Drawable;
    import android.os.Bundle;
    import android.transition.Scene;
    import android.view.View;
    import android.widget.ImageView;
    import android.widget.TextView;
    import android.widget.Toast;

    import androidx.annotation.NonNull;
    import androidx.appcompat.app.AppCompatActivity;
    import androidx.core.content.ContextCompat;
    import androidx.recyclerview.widget.ItemTouchHelper;
    import androidx.recyclerview.widget.LinearLayoutManager;
    import androidx.recyclerview.widget.RecyclerView;

    import com.example.splitbooks.DTO.response.BookResponse;
    import com.example.splitbooks.R;
    import com.example.splitbooks.activity.chats.AllChatsActivity;
    import com.example.splitbooks.activity.home.HomePageActivity;
    import com.example.splitbooks.activity.profile.PublicProfileActivity;
    import com.example.splitbooks.network.ApiClient;
    import com.example.splitbooks.network.ApiService;
    import com.example.splitbooks.network.JwtManager;
    import com.google.android.material.appbar.MaterialToolbar;
    import com.google.android.material.bottomnavigation.BottomNavigationView;

    import java.util.ArrayList;
    import java.util.List;

    import okhttp3.ResponseBody;
    import retrofit2.Call;
    import retrofit2.Callback;
    import retrofit2.Response;

    public class ProfileBokksActivity extends AppCompatActivity {

        private RecyclerView recyclerView;
        private TextView tvEmptyLibrary;
        private BookAdapter adapter;
        private MaterialToolbar back;

        private BottomNavigationView bottomNavigation;
        private Long profileId;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_profile_books);

            profileId = getIntent().getLongExtra("profileId", -1L);
            back = findViewById(R.id.back_arrow_follow);
            recyclerView = findViewById(R.id.recyclerViewMyBooks);
            tvEmptyLibrary = findViewById(R.id.tvEmptyLibrary);


            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            adapter = new BookAdapter(new ArrayList<>(), (bookItem, sharedImageView) -> {
                Intent intent = new Intent(ProfileBokksActivity.this, BookDetailActivity.class);
                intent.putExtra("volumeId", bookItem.getId());

                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(
                        ProfileBokksActivity.this,
                        sharedImageView,
                        "book_cover_transition"
                );

                startActivity(intent, options.toBundle());
            });
            recyclerView.setAdapter(adapter);

            ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
                @Override
                public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                    return false;
                }
                @Override
                public int getSwipeDirs(@NonNull RecyclerView recyclerView,
                                        @NonNull RecyclerView.ViewHolder viewHolder) {

                    if (!profileId.equals(JwtManager.getMyProfileId(getApplicationContext()))) {
                        return 0;
                    }
                    return super.getSwipeDirs(recyclerView, viewHolder);
                }

                @Override
                public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                    int position = viewHolder.getAdapterPosition();
                    BookResponse.Item book = adapter.getBookAtPosition(position);

                    if (profileId.equals(JwtManager.getMyProfileId(getApplicationContext()))) {
                        deleteBookFromLibrary(book.getId(), position);
                    } else {
                        adapter.notifyItemChanged(position);
                        Toast.makeText(ProfileBokksActivity.this, "You can't delete books from other profiles", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                        @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                        int actionState, boolean isCurrentlyActive) {
                    View itemView = viewHolder.itemView;

                    if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE && dX < 0) {
                        Paint paint = new Paint();
                        paint.setColor(Color.parseColor("#B00020"));
                        paint.setAntiAlias(true);

                        float cornerRadius = 30f;

                        RectF background = new RectF(
                                itemView.getRight() + dX,
                                itemView.getTop(),
                                itemView.getRight(),
                                itemView.getBottom()
                        );
                        c.drawRoundRect(background, cornerRadius, cornerRadius, paint);

                        Drawable icon = ContextCompat.getDrawable(recyclerView.getContext(), R.drawable.trash_svgrepo_com);
                        if (icon != null) {
                            int iconSizeDp = 48;
                            float density = recyclerView.getContext().getResources().getDisplayMetrics().density;
                            int iconSizePx = (int) (iconSizeDp * density);
                            int itemHeight = itemView.getHeight();

                            int iconTop = itemView.getTop() + (itemHeight - iconSizePx) / 2;
                            int iconBottom = iconTop + iconSizePx;

                            int marginDp = 16;
                            int marginPx = (int) (marginDp * density);

                            int iconRight = itemView.getRight() - marginPx;
                            int iconLeft = iconRight - iconSizePx;

                            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                            icon.draw(c);
                        }
                    }

                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                }
            };
            new ItemTouchHelper(simpleCallback).attachToRecyclerView(recyclerView);



            back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });



            back.setTitle(profileId.equals(JwtManager.getMyProfileId(getApplicationContext())) ? "My Library" : "Library");
            tvEmptyLibrary.setText(profileId.equals(JwtManager.getMyProfileId(getApplicationContext())) ? "You haven't added any books yet." : "User hasn't added any books yet.");

            bottomNavigation = findViewById(R.id.search_bottom_navigation);

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
                    onResume();
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

            fetchProfileBooks();
        }

        @Override
        protected void onResume() {
            super.onResume();
            fetchProfileBooks();
        }

        private void fetchProfileBooks() {
            ApiService apiService = ApiClient.getApiService(getApplicationContext());
            Call<BookResponse> call = apiService.getBooksByProfileId(profileId);
            call.enqueue(new Callback<BookResponse>() {
                @Override
                public void onResponse(Call<BookResponse> call, Response<BookResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<BookResponse.Item> items = response.body().getItems();
                        if (items == null || items.isEmpty()) {
                            tvEmptyLibrary.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                        } else {
                            tvEmptyLibrary.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                            adapter.updateBooks(items);
                        }
                    } else {
                        Toast.makeText(ProfileBokksActivity.this, "Failed to load books", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<BookResponse> call, Throwable t) {
                    Toast.makeText(ProfileBokksActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
        private void deleteBookFromLibrary(String volumeId, int position) {
            ApiService apiService = ApiClient.getApiService(getApplicationContext());
            Call<ResponseBody> call = apiService.removeBookFromLibrary(volumeId);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        adapter.removeBook(position);
                        fetchProfileBooks();
                        Toast.makeText(ProfileBokksActivity.this, "Book removed", Toast.LENGTH_SHORT).show();
                    } else {
                        adapter.notifyItemChanged(position);
                        Toast.makeText(ProfileBokksActivity.this, "Error: " + response.message(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    adapter.notifyItemChanged(position);
                    Toast.makeText(ProfileBokksActivity.this, "Failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

    }