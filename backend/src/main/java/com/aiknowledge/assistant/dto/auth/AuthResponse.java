package com.aiknowledge.assistant.dto.auth;

import com.aiknowledge.assistant.dto.user.UserResponse;

public record AuthResponse(
        String token,
        UserResponse user
) {}
