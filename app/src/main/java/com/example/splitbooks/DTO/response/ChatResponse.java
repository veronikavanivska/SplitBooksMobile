package com.example.splitbooks.DTO.response;


import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class ChatResponse {
    private Long chatId;
    private String groupName;
    private boolean isGroup;
    private ChatType chatType;
    private GroupChatType groupChatType;
    private String chatPhotoUrl;
    private List<ParticipantInfo> participants;
    private LocalDateTime lastUpdated;

    @Data
    public static class ParticipantInfo {
        private Long profileId;
        private String username;
        private String avatarUrl;
        private ProfileType profileType;
    }
}