package com.aiknowledge.assistant.dto.conversation;

import jakarta.validation.constraints.Size;

public record CreateConversationRequest(
        @Size(max = 200, message = "Title must be at most 200 characters")
        String title
) {}
