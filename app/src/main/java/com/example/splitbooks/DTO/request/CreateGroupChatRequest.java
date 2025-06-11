package com.example.splitbooks.DTO.request;

import com.example.splitbooks.DTO.response.GroupChatType;

import java.util.List;

import lombok.Data;

@Data
public class CreateGroupChatRequest {
    private String groupName;
    private List<Long> participantIds;
    private GroupChatType groupChatType;


}