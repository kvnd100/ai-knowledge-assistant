package com.aiknowledge.assistant.dto.document;

import com.aiknowledge.assistant.entity.Document;

import java.time.Instant;

public record DocumentResponse(
        Long id,
        String filename,
        String contentType,
        long sizeBytes,
        int extractedTextLength,
        Long conversationId,
        Instant createdAt
) {
    public static DocumentResponse from(Document document, Long conversationId) {
        return new DocumentResponse(
                document.getId(),
                document.getFilename(),
                document.getContentType(),
                document.getSizeBytes(),
                document.getExtractedText().length(),
                conversationId,
                document.getCreatedAt());
    }
}
