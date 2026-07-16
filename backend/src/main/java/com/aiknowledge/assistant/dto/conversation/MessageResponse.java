package com.aiknowledge.assistant.dto.conversation;

import com.aiknowledge.assistant.entity.Message;
import com.aiknowledge.assistant.entity.MessageRole;

import java.time.Instant;

public record MessageResponse(
        Long id,
        MessageRole role,
        String content,
        Instant createdAt
) {
    public static MessageResponse from(Message message) {
        return new MessageResponse(message.getId(), message.getRole(), message.getContent(), message.getCreatedAt());
    }
}
