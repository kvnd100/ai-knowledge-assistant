package com.aiknowledge.assistant.dto.dashboard;

import com.aiknowledge.assistant.dto.conversation.ConversationSummaryResponse;

import java.time.Instant;
import java.util.List;

public record DashboardStatsResponse(
        long conversationCount,
        long messageCount,
        long documentCount,
        Instant memberSince,
        List<ConversationSummaryResponse> recentConversations
) {}
