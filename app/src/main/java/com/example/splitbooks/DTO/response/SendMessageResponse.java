package com.example.splitbooks.DTO.response;

import lombok.Data;

@Data
public class SendMessageResponse {
    private Long messageId;
    private String content;
    private String senderUsername;
    private Long senderId;
    private String timestamp;
}
