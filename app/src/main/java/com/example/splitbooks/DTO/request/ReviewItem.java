package com.example.splitbooks.DTO.request;

import com.example.splitbooks.DTO.response.ReviewResponse;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ReviewItem {
    public ReviewResponse review;
    public int indentLevel;


}
