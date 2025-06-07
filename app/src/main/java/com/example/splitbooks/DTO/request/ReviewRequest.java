package com.example.splitbooks.DTO.request;

import lombok.Data;

@Data
public class ReviewRequest {
    private String volumeId;
    private String reviewText;
    private int rating;
    private Long parentReviewId;
}
