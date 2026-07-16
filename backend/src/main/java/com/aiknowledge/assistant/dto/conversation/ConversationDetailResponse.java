package com.aiknowledge.assistant.dto.conversation;

import com.aiknowledge.assistant.entity.Conversation;
import com.aiknowledge.assistant.entity.ConversationType;

import java.time.Instant;
import java.util.List;

public record ConversationDetailResponse(
        Long id,
        String title,
        ConversationType type,
        Long documentId,
        String documentFilename,
        List<MessageResponse> messages,
        Instant createdAt,
        Instant updatedAt
) {
    public static ConversationDetailResponse from(Conversation conversation) {
        var document = conversation.getDocument();
        return new ConversationDetailResponse(
                conversation.getId(),
                conversation.getTitle(),
                conversation.getType(),
                document != null ? document.getId() : null,
                document != null ? document.getFilename() : null,
                conversation.getMessages().stream().map(MessageResponse::from).toList(),
                conversation.getCreatedAt(),
                conversation.getUpdatedAt());
    }
}
