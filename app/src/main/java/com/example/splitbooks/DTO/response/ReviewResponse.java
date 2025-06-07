package com.example.splitbooks.DTO.response;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class ReviewResponse {
    private Long reviewId;
    private String volumeId;
    private String reviewText;
    private int rating;
    private String username;
    private String createdAt;
    private Long parentReviewId;
    private Long profileId;

    private List<ReviewResponse> replies = new ArrayList<>();
}
