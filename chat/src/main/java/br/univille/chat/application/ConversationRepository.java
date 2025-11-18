package br.univille.chat.application;

import br.univille.chat.domain.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ConversationRepository extends JpaRepository<Conversation, UUID> {
    // Busca uma conversa que contenha *exatamente* os dois IDs de usuário fornecidos
    @Query(value = "SELECT c.* FROM conversation c JOIN conversation_participants cp ON c.id = cp.conversation_id WHERE cp.user_id = :user1Id " +
            "AND c.id IN (SELECT conversation_id FROM conversation_participants WHERE user_id = :user2Id) " +
            "GROUP BY c.id HAVING COUNT(cp.user_id) = 2", nativeQuery = true)
    Optional<Conversation> findConversationByParticipants(@Param("user1Id") UUID user1Id, @Param("user2Id") UUID user2Id);
}