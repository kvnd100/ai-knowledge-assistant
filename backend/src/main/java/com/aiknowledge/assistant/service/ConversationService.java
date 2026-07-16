package com.aiknowledge.assistant.service;

import com.aiknowledge.assistant.dto.conversation.ConversationDetailResponse;
import com.aiknowledge.assistant.dto.conversation.ConversationSummaryResponse;
import com.aiknowledge.assistant.dto.conversation.CreateConversationRequest;
import com.aiknowledge.assistant.dto.conversation.MessageResponse;
import com.aiknowledge.assistant.dto.conversation.SendMessageRequest;
import com.aiknowledge.assistant.dto.conversation.SendMessageResponse;
import com.aiknowledge.assistant.entity.Conversation;
import com.aiknowledge.assistant.entity.ConversationType;
import com.aiknowledge.assistant.entity.Message;
import com.aiknowledge.assistant.entity.MessageRole;
import com.aiknowledge.assistant.entity.User;
import com.aiknowledge.assistant.exception.NotFoundException;
import com.aiknowledge.assistant.repository.ConversationRepository;
import com.aiknowledge.assistant.repository.MessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ConversationService {

    public static final String DEFAULT_TITLE = "New conversation";

    private static final Logger log = LoggerFactory.getLogger(ConversationService.class);

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final AiService aiService;

    public ConversationService(ConversationRepository conversationRepository,
                               MessageRepository messageRepository,
                               AiService aiService) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.aiService = aiService;
    }

    @Transactional(readOnly = true)
    public List<ConversationSummaryResponse> list(User user) {
        return conversationRepository.findAllWithDocumentByUserId(user.getId()).stream()
                .map(ConversationSummaryResponse::from)
                .toList();
    }

    @Transactional
    public ConversationSummaryResponse create(User user, CreateConversationRequest request) {
        String title = request != null && request.title() != null && !request.title().isBlank()
                ? request.title().trim()
                : DEFAULT_TITLE;
        Conversation conversation = conversationRepository.save(
                new Conversation(user, title, ConversationType.CHAT, null));
        return ConversationSummaryResponse.from(conversation);
    }

    @Transactional(readOnly = true)
    public ConversationDetailResponse get(User user, Long conversationId) {
        return ConversationDetailResponse.from(loadOwned(user, conversationId));
    }

    @Transactional
    public void delete(User user, Long conversationId) {
        Conversation conversation = conversationRepository.findByIdAndUserId(conversationId, user.getId())
                .orElseThrow(() -> new NotFoundException("Conversation not found"));
        conversationRepository.delete(conversation);
        log.info("User id={} deleted conversation id={}", user.getId(), conversationId);
    }

    /**
     * Persists the user's message, asks the AI for a reply, and persists that too.
     * Deliberately NOT one big transaction: the user's message is committed before the
     * AI call so it is never lost if the provider fails, and no DB connection is held
     * during the multi-second AI round trip.
     */
    public SendMessageResponse sendMessage(User user, Long conversationId, SendMessageRequest request) {
        Conversation conversation = loadOwned(user, conversationId);
        boolean firstExchange = conversation.getMessages().isEmpty();

        Message userMessage = messageRepository.save(
                new Message(conversation, MessageRole.USER, request.content().trim()));
        conversation.getMessages().add(userMessage);

        String reply = aiService.reply(conversation);
        Message assistantMessage = messageRepository.save(
                new Message(conversation, MessageRole.ASSISTANT, reply));

        if (firstExchange && DEFAULT_TITLE.equals(conversation.getTitle())) {
            autoTitle(conversation, userMessage.getContent(), reply);
        }
        touch(conversation);

        return new SendMessageResponse(
                conversation.getId(),
                conversation.getTitle(),
                MessageResponse.from(userMessage),
                MessageResponse.from(assistantMessage));
    }

    private void autoTitle(Conversation conversation, String userContent, String reply) {
        try {
            conversation.setTitle(aiService.generateTitle(userContent, reply));
        } catch (Exception ex) {
            log.warn("Auto-title generation failed for conversation id={}: {}",
                    conversation.getId(), ex.getMessage());
        }
    }

    private Conversation loadOwned(User user, Long conversationId) {
        return conversationRepository.findWithMessagesByIdAndUserId(conversationId, user.getId())
                .orElseThrow(() -> new NotFoundException("Conversation not found"));
    }

    private void touch(Conversation conversation) {
        conversation.touch();
        conversationRepository.save(conversation);
    }
}
