    package com.example.splitbooks.activity.search;

    import android.app.ActivityOptions;
    import android.content.Intent;
    import android.os.Bundle;
    import android.text.Editable;
    import android.text.TextWatcher;
    import android.util.Log;
    import android.view.View;
    import android.view.inputmethod.EditorInfo;
    import android.widget.AdapterView;
    import android.widget.ArrayAdapter;
    import android.widget.AutoCompleteTextView;
    import android.widget.EditText;
    import android.widget.ImageView;
    import android.widget.LinearLayout;
    import android.widget.Spinner;
    import android.widget.TextView;
    import android.widget.Toast;

    import androidx.annotation.NonNull;
    import androidx.annotation.Nullable;
    import androidx.appcompat.app.AppCompatActivity;
    import androidx.core.content.ContextCompat;
    import androidx.recyclerview.widget.LinearLayoutManager;
    import androidx.recyclerview.widget.RecyclerView;

    import com.example.splitbooks.DTO.request.BooksSearchRequest;
    import com.example.splitbooks.DTO.request.SearchType;
    import com.example.splitbooks.DTO.response.BookResponse;
    import com.example.splitbooks.R;
    import com.example.splitbooks.activity.chats.AllChatsActivity;
    import com.example.splitbooks.activity.home.HomePageActivity;
    import com.example.splitbooks.activity.profile.PublicProfileActivity;
    import com.example.splitbooks.network.ApiClient;
    import com.example.splitbooks.network.ApiService;
    import com.example.splitbooks.network.JwtManager;
    import com.google.android.material.bottomnavigation.BottomNavigationView;

    import java.util.ArrayList;
    import java.util.List;

    import retrofit2.Call;
    import retrofit2.Callback;
    import retrofit2.Response;

    public class SearchBookActivity extends AppCompatActivity {

        private EditText editSearch;
        private Spinner spinnerSearchType;
        private RecyclerView recyclerView;
        private BookAdapter bookAdapter;
        private ImageView btnClearSearch;
        private boolean isLoading = false;
        private boolean isLastPage = false;
        private int currentStartIndex = 0;
        private static final int PAGE_SIZE = 20;
        private String lastQuery = "";
        private SearchType currentSearchType = SearchType.BY_TITLE;

        private TextView btnSearchBooks, btnMyLibrary, textQuote ;
        private BottomNavigationView bottomNavigation;
        private ApiService apiService;
        private Long profileId;
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.search_books_layout);


            btnSearchBooks = findViewById(R.id.btnSearchBooks);
            btnMyLibrary = findViewById(R.id.btnMyLibrary);
            editSearch = findViewById(R.id.editSearch);
            spinnerSearchType = findViewById(R.id.spinnerSearchType);
            recyclerView = findViewById(R.id.recyclerViewBooks);
            btnClearSearch = findViewById(R.id.btnClearSearch);
            apiService = ApiClient.getApiService(getApplicationContext());
            textQuote = findViewById(R.id.textQuote);
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
                    Intent intent = new Intent(this, SearchProfilesActivity.class);
                    startActivity(intent);
                    finish();
                    return true;
                }else if(id == R.id.action_library){
                    onResume();
                    return true;
                }else if(id == R.id.action_chats){
                    Intent intent = new Intent(this, AllChatsActivity.class);
                    startActivity(intent);
                    finish();
                    return true;
                }
                return false;
            });

            profileId = JwtManager.getMyProfileId(getApplicationContext());
            ArrayAdapter<SearchType> adapter = new ArrayAdapter<>(
                    this, android.R.layout.simple_spinner_item, SearchType.values());
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerSearchType.setAdapter(adapter);
            spinnerSearchType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    currentSearchType = (SearchType) parent.getItemAtPosition(position);

                    if (!lastQuery.isEmpty()) {
                        performBookSearch(lastQuery, true);
                    }
                }
                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });

            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            bookAdapter = new BookAdapter(new ArrayList<>(), (bookItem, sharedImageView) -> {
                Intent intent = new Intent(this, BookDetailActivity.class);
                intent.putExtra("volumeId", bookItem.getId());

                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(
                        this,
                        sharedImageView,
                        "book_cover_transition"
                );

                startActivity(intent, options.toBundle());
            });
            recyclerView.setAdapter(bookAdapter);


            btnSearchBooks.setSelected(true);

            btnMyLibrary.setOnClickListener(v -> {
                btnMyLibrary.setSelected(true);
                btnSearchBooks.setSelected(false);
                Intent intent = new Intent(this, ProfileBokksActivity.class);
                intent.putExtra("profileId", profileId);
                startActivity(intent);


            });

            btnSearchBooks.setOnClickListener(v -> {
                btnMyLibrary.setSelected(false);
                Intent intent = new Intent(this, SearchBookActivity.class);

                startActivity(intent);

            });

            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                    if (dy > 0) {
                        LinearLayoutManager lm = (LinearLayoutManager) rv.getLayoutManager();
                        if (lm == null) return;

                        int visibleCount = lm.getChildCount();
                        int totalCount = lm.getItemCount();
                        int firstVisible = lm.findFirstVisibleItemPosition();

                        if (!isLoading && !isLastPage) {
                            if ((visibleCount + firstVisible) >= totalCount - 3) {
                                performBookSearch(lastQuery, false);
                            }
                        }
                    }
                }
            });

            editSearch.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    String query = s.toString().trim();
                    btnClearSearch.setVisibility(query.isEmpty() ? View.GONE : View.VISIBLE);
                    if (!query.isEmpty()) {
                        recyclerView.setVisibility(View.VISIBLE);
                        textQuote.setVisibility(View.GONE);
                        performBookSearch(query, true);
                    } else {
                        recyclerView.setVisibility(View.GONE);
                        textQuote.setVisibility(View.VISIBLE);
                        lastQuery = "";
                        currentStartIndex = 0;
                        isLastPage = false;
                        bookAdapter.updateBooks(new ArrayList<>());
                    }
                }
            });
            btnClearSearch.setOnClickListener(v -> {
                recyclerView.setVisibility(View.GONE);
                textQuote.setVisibility(View.VISIBLE);
                editSearch.setText("");
                editSearch.clearFocus();
            });
        }

        private void performBookSearch(String query, boolean isNewSearch) {
            if (isLoading) return;
            if (isLastPage && !isNewSearch) return;

            if (isNewSearch) {
                currentStartIndex = 0;
                isLastPage = false;
                lastQuery = query;
                bookAdapter.updateBooks(new ArrayList<>());
            }

            isLoading = true;
            Log.d("SearchBook", "Searching: query=" + query + ", startIndex=" + currentStartIndex);


            BooksSearchRequest request = new BooksSearchRequest();
            request.setSearchType(currentSearchType);
            request.setSearchQuery(query);

            apiService.searchBooks(request, currentStartIndex, PAGE_SIZE).enqueue(new Callback<BookResponse>() {
                @Override
                public void onResponse(Call<BookResponse> call, Response<BookResponse> response) {
                    isLoading = false;
                    if (response.isSuccessful() && response.body() != null) {
                        List<BookResponse.Item> items = response.body().getItems();

                        if (items != null && !items.isEmpty()) {
                            if (isNewSearch) {
                                bookAdapter.updateBooks(items);
                            } else {
                                bookAdapter.addBooks(items);
                            }
                            currentStartIndex += PAGE_SIZE;

                            if (items == null ){
                                isLastPage = true;
                            }else isLastPage = false;
                        } else {

                            if (isNewSearch) {
                                bookAdapter.updateBooks(new ArrayList<>());
                            }
                            isLastPage = true;
                        }
                    } else {
                        Toast.makeText(SearchBookActivity.this, "No books found.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<BookResponse> call, Throwable t) {
                    isLoading = false;
                    Toast.makeText(SearchBookActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
