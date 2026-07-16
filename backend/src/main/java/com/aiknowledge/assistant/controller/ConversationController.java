package com.aiknowledge.assistant.controller;

import com.aiknowledge.assistant.dto.conversation.ConversationDetailResponse;
import com.aiknowledge.assistant.dto.conversation.ConversationSummaryResponse;
import com.aiknowledge.assistant.dto.conversation.CreateConversationRequest;
import com.aiknowledge.assistant.dto.conversation.SendMessageRequest;
import com.aiknowledge.assistant.dto.conversation.SendMessageResponse;
import com.aiknowledge.assistant.entity.User;
import com.aiknowledge.assistant.service.ConversationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/conversations")
@Tag(name = "Conversations", description = "Persistent AI chat: create conversations, send messages, resume history")
@SecurityRequirement(name = "bearerAuth")
public class ConversationController {

    private final ConversationService conversationService;

    public ConversationController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    @GetMapping
    @Operation(summary = "List the authenticated user's conversations, most recently active first")
    public List<ConversationSummaryResponse> list(@AuthenticationPrincipal User user) {
        return conversationService.list(user);
    }

    @PostMapping
    @Operation(summary = "Start a new chat conversation")
    public ResponseEntity<ConversationSummaryResponse> create(@AuthenticationPrincipal User user,
                                                              @Valid @RequestBody(required = false) CreateConversationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(conversationService.create(user, request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a conversation with its full message history")
    public ConversationDetailResponse get(@AuthenticationPrincipal User user, @PathVariable Long id) {
        return conversationService.get(user, id);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a conversation and its messages")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal User user, @PathVariable Long id) {
        conversationService.delete(user, id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/messages")
    @Operation(summary = "Send a message and receive the AI assistant's reply")
    public SendMessageResponse sendMessage(@AuthenticationPrincipal User user,
                                           @PathVariable Long id,
                                           @Valid @RequestBody SendMessageRequest request) {
        return conversationService.sendMessage(user, id, request);
    }
}
