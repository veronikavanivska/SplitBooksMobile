package com.example.splitbooks.DTO.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BooksSearchRequest {
    private SearchType searchType = SearchType.BY_TITLE;
    private String searchQuery;
}
