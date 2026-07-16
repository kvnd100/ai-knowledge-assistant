package com.aiknowledge.assistant.dto.conversation;

import com.aiknowledge.assistant.entity.Conversation;
import com.aiknowledge.assistant.entity.ConversationType;

import java.time.Instant;

public record ConversationSummaryResponse(
        Long id,
        String title,
        ConversationType type,
        Long documentId,
        String documentFilename,
        Instant createdAt,
        Instant updatedAt
) {
    public static ConversationSummaryResponse from(Conversation conversation) {
        var document = conversation.getDocument();
        return new ConversationSummaryResponse(
                conversation.getId(),
                conversation.getTitle(),
                conversation.getType(),
                document != null ? document.getId() : null,
                document != null ? document.getFilename() : null,
                conversation.getCreatedAt(),
                conversation.getUpdatedAt());
    }
}
