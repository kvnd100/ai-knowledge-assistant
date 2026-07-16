package com.aiknowledge.assistant.service;

import com.aiknowledge.assistant.dto.conversation.ConversationSummaryResponse;
import com.aiknowledge.assistant.dto.dashboard.DashboardStatsResponse;
import com.aiknowledge.assistant.entity.User;
import com.aiknowledge.assistant.repository.ConversationRepository;
import com.aiknowledge.assistant.repository.DocumentRepository;
import com.aiknowledge.assistant.repository.MessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DashboardService {

    private static final int RECENT_CONVERSATIONS_LIMIT = 5;

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final DocumentRepository documentRepository;

    public DashboardService(ConversationRepository conversationRepository,
                            MessageRepository messageRepository,
                            DocumentRepository documentRepository) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.documentRepository = documentRepository;
    }

    @Transactional(readOnly = true)
    public DashboardStatsResponse stats(User user) {
        List<ConversationSummaryResponse> recent = conversationRepository
                .findAllWithDocumentByUserId(user.getId()).stream()
                .limit(RECENT_CONVERSATIONS_LIMIT)
                .map(ConversationSummaryResponse::from)
                .toList();
        return new DashboardStatsResponse(
                conversationRepository.countByUserId(user.getId()),
                messageRepository.countByConversationUserId(user.getId()),
                documentRepository.countByUserId(user.getId()),
                user.getCreatedAt(),
                recent);
    }
}
