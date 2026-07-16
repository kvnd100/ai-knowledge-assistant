package com.aiknowledge.assistant.dto.conversation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SendMessageRequest(
        @NotBlank(message = "Message content is required")
        @Size(max = 8000, message = "Message must be at most 8000 characters")
        String content
) {}
