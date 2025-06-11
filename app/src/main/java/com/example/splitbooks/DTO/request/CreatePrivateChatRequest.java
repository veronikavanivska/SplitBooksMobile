package com.example.splitbooks.DTO.request;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreatePrivateChatRequest {
    private Long otherParticipantId;

}