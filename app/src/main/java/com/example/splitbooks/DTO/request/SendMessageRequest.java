package com.example.splitbooks.DTO.request;

import lombok.Data;

@Data
public class SendMessageRequest {
    private Long chatId;
    private String content;
}

