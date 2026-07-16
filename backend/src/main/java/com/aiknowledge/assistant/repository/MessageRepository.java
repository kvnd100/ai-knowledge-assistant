package com.aiknowledge.assistant.repository;

import com.aiknowledge.assistant.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Long> {

    long countByConversationUserId(Long userId);
}
