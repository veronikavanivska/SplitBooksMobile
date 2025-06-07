    package com.example.splitbooks.activity.search;

    import android.content.Intent;
    import android.os.Bundle;
    import android.widget.Toast;

    import androidx.appcompat.app.AppCompatActivity;
    import androidx.recyclerview.widget.LinearLayoutManager;
    import androidx.recyclerview.widget.RecyclerView;

    import com.example.splitbooks.DTO.response.BookResponse;
    import com.example.splitbooks.R;
    import com.example.splitbooks.network.ApiClient;
    import com.example.splitbooks.network.ApiService;
    import com.example.splitbooks.network.JwtManager;

    import java.util.ArrayList;

    import retrofit2.Call;
    import retrofit2.Callback;
    import retrofit2.Response;

    public class ProfileBokksActivity extends AppCompatActivity {

        private RecyclerView recyclerView;
        private BookAdapter adapter;
        private Long profileId;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_profile_books);

            profileId = JwtManager.getMyProfileId(getApplicationContext());
            recyclerView = findViewById(R.id.recyclerView);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));

            adapter = new BookAdapter(new ArrayList<>(), book -> {
                Intent intent = new Intent(this, BookDetailActivity.class);
                intent.putExtra("volumeId", book.getId());
                startActivity(intent);
            });

            recyclerView.setAdapter(adapter);


            fetchProfileBooks();
        }

        private void fetchProfileBooks() {
            ApiService apiService = ApiClient.getApiService(getApplicationContext());
            Call<BookResponse> call = apiService.getBooksByProfileId(profileId);
            call.enqueue(new Callback<BookResponse>() {
                @Override
                public void onResponse(Call<BookResponse> call, Response<BookResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        adapter.updateBooks(response.body().getItems());
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
    }