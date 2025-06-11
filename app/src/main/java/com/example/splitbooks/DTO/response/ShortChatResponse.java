package com.example.splitbooks.DTO.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ShortChatResponse {
    Long chatId;
    String chatName;
    String chatProfileUrl;
    boolean groupChat;
    String lastUpdated;
}
