package com.aiknowledge.assistant.repository;

import com.aiknowledge.assistant.entity.Conversation;
import com.aiknowledge.assistant.entity.ConversationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    @Query("select c from Conversation c left join fetch c.document where c.user.id = :userId order by c.updatedAt desc")
    List<Conversation> findAllWithDocumentByUserId(@Param("userId") Long userId);

    Optional<Conversation> findByIdAndUserId(Long id, Long userId);

    @Query("select c from Conversation c left join fetch c.messages left join fetch c.document "
            + "where c.id = :id and c.user.id = :userId")
    Optional<Conversation> findWithMessagesByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    List<Conversation> findAllByUserIdAndType(Long userId, ConversationType type);

    long countByUserId(Long userId);
}
