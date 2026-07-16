package com.aiknowledge.assistant.dto.conversation;

public record SendMessageResponse(
        Long conversationId,
        String conversationTitle,
        MessageResponse userMessage,
        MessageResponse assistantMessage
) {}
