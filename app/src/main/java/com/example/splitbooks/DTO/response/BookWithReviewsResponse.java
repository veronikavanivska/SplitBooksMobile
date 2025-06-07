package com.example.splitbooks.DTO.response;



import lombok.Data;

import java.util.List;

@Data
public class BookWithReviewsResponse {
    private BookDetailResponse book;
    private List<ReviewResponse> reviews;
}
